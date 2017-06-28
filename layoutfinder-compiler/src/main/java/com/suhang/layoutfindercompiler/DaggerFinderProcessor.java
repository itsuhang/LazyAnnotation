package com.suhang.layoutfindercompiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.suhang.layoutfinderannotation.GenDaggerHelper;
import com.suhang.layoutfinderannotation.GenSubComponent;
import com.suhang.layoutfinderannotation.GenRootComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by 苏杭 on 2017/6/12 22:03.
 */
@AutoService(Processor.class)
public class DaggerFinderProcessor extends AbstractProcessor {
    public static final String HELPER_CLASS = "DaggerHelper";
    private String helper_packname;
    Filer mFiler;
    Elements mElements;
    Messager mMessager;
    private Types mTypeUtils;
    private Map<String, JavaFile> mMap = new HashMap<>();
    private Map<Integer, List<Param>> parents = new HashMap<>();
    private Map<Integer, List<Param>> roots = new HashMap<>();
    private Map<String, List<TypeName>> modules = new HashMap<>();
    private List<FieldSpec> fieldSpecs = new ArrayList<>();
    private List<MethodSpec> methodSpecs = new ArrayList<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElements = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
        mTypeUtils = processingEnvironment.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(GenRootComponent.class.getCanonicalName());
        types.add(GenSubComponent.class.getCanonicalName());
        return types;
    }

    /**
     * 通过GenDaggerHelper注解获取DaggerHelper的包名
     *
     * @param roundEnv
     */
    private void proccessDaggerHelper(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenDaggerHelper.class)) {
            helper_packname = mElements.getPackageOf(element).getQualifiedName().toString();
            helperTypeBuilder = genHelperTypeBuilder();
            mMap.put(HELPER_CLASS, JavaFile.builder(helper_packname, helperTypeBuilder.build()).build());
        }
    }

    private TypeSpec.Builder helperTypeBuilder;

    private void proccessDaggerModule(RoundEnvironment roundEnv) {
        TypeElement typeElement = mElements.getTypeElement(TypeUtil.MODULE.packageName() + "." + TypeUtil.MODULE.simpleName());
        for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
            List<TypeName> ls = new ArrayList<>();
            TypeElement type = (TypeElement) element;
            for (Element element1 : type.getEnclosedElements()) {
                if (element1.getKind().equals(ElementKind.CONSTRUCTOR)) {
                    ExecutableElement ee = (ExecutableElement) element1;
                    for (VariableElement variableElement : ee.getParameters()) {
                        ls.add(TypeName.get(variableElement.asType()));
                    }
                }
            }
            modules.put(TypeName.get(element.asType()).toString(), ls);
        }
    }

    private int count = 0;


    private void genMethodBody(CodeBlock.Builder builder, TypeName module, MethodSpec.Builder rootMethodBuilder) {
        builder = builder.add("setModule(new $T", module);
        List<TypeName> names = modules.get(module.toString());
        if (names != null && names.size() > 0) {
            if (names.size() == 1) {
                TypeName name = names.get(0);
                rootMethodBuilder.addParameter(ParameterSpec.builder(name, "param" + count).build());
                builder.add("($N))", "param" + count);
                count++;
            } else {
                for (int i = 0; i < names.size(); i++) {
                    TypeName name = names.get(i);
                    rootMethodBuilder.addParameter(ParameterSpec.builder(name, "param" + count).build());
                    if (i == 0) {
                        builder.add("($N,", "param" + count);
                    } else if (i == names.size() - 1) {
                        builder.add("$N))", "param" + count);
                    } else {
                        builder.add("$N,", "param" + count);
                    }
                    count++;
                }
            }
        } else {
            builder.add("())");
        }
    }

    private TypeSpec.Builder genHelperTypeBuilder() {
        ClassName className = ClassName.get(helper_packname, HELPER_CLASS);
        MethodSpec construct = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();
        FieldSpec instance1 = FieldSpec.builder(className, "INSTANCE", Modifier.STATIC, Modifier.PRIVATE, Modifier.FINAL).initializer("new $T()", className).build();
        TypeSpec holder = TypeSpec.classBuilder("Holder").addModifiers(Modifier.STATIC, Modifier.PRIVATE).addField(instance1).build();
        MethodSpec getInstance = MethodSpec.methodBuilder("getInstance").addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(className)
                .addStatement("return $N.$N", holder, instance1)
                .build();
        return TypeSpec.classBuilder(HELPER_CLASS).addModifiers(Modifier.PUBLIC).addType(holder).addMethod(getInstance).addMethod(construct);
    }

//    /**
//     * 解析注解
//     *
//     * @param roundEnv
//     * @return
//     */
//    private void processDagger(RoundEnvironment roundEnv) {
//        for (Element element : roundEnv.getElementsAnnotatedWith(GenSubComponent.class)) {
//            String packname = mElements.getPackageOf(element).getQualifiedName().toString();
//            TypeElement typeElement = (TypeElement) element;
//            String className = typeElement.getSimpleName().toString() + "Component";
//            GenSubComponent genSubComponent = element.getAnnotation(GenSubComponent.class);
//            int tag = genSubComponent.tag();
//            TypeElement scope = getScope(genSubComponent);
//            List<TypeName> modules = getModules(genSubComponent);
//            List<Param> params = subs.get(tag);
//            if (params == null) {
//                params = new ArrayList<>();
//            }
//            Param param = new Param(scope, modules, packname, className, typeElement, null, 0);
//            params.add(param);
//            subs.put(tag, params);
//            genDaggerChild(param);
//        }
//    }

    private void processDaggerParent(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenSubComponent.class)) {
            String packname = mElements.getPackageOf(element).getQualifiedName().toString();
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString() + "Component";
            GenSubComponent genSubComponent = element.getAnnotation(GenSubComponent.class);
            int tag = genSubComponent.tag();
            int childTag = genSubComponent.childTag();
            boolean shouldInject = genSubComponent.shouldInject();
            TypeElement scope = getScope(genSubComponent);
            List<TypeName> modules = getModules(genSubComponent);
            List<Param> params = parents.get(tag);
            if (params == null) {
                params = new ArrayList<>();
            }
            Param param;
            if (childTag == 0) {
                param = new Param(scope, modules, packname, className, typeElement, null, childTag, shouldInject);
            } else {
                param = new Param(scope, modules, packname, className, typeElement, new ArrayList<Param>(), childTag, shouldInject);
            }
            params.add(param);
            parents.put(tag, params);
        }
        for (List<Param> params : parents.values()) {
            for (Param param : params) {
                param.childs = parents.get(param.childTag);
                genDaggerParent(param);
            }
        }
    }

    private void processDaggerRoot(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenRootComponent.class)) {
            String packname = mElements.getPackageOf(element).getQualifiedName().toString();
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString() + "Component";
            GenRootComponent genSubComponent = element.getAnnotation(GenRootComponent.class);
            int tag = genSubComponent.tag();
            int childTag = genSubComponent.childTag();
            boolean shouldInject = genSubComponent.shouldInject();
            TypeElement scope = getScope(genSubComponent);
            List<TypeName> modules = getModules(genSubComponent);
            List<Param> params = roots.get(tag);
            if (params == null) {
                params = new ArrayList<>();
            }
            List<Param> parent = parents.get(childTag);
            Param param = new Param(scope, modules, packname, className, typeElement, parent, childTag, shouldInject);
            params.add(param);
            roots.put(tag, params);
            genDaggerRoot(param);
        }
    }

    /**
     * 生成dagger父组件
     *
     * @param param
     * @return
     */
    private void genDaggerParent(Param param) {
        TypeSpec.Builder classBuilder = getClassBuilder(param, false);
        if (!TypeName.get(param.scope.asType()).equals(TypeName.OBJECT)) {
            classBuilder.addAnnotation(ClassName.get(param.scope));
        }
        if (param.childs != null && param.childs.size() > 0) {
            Map<String, ClassName> childs = getClassName(param.childs);
            for (Map.Entry<String, ClassName> entry : childs.entrySet()) {
                classBuilder.addMethod(MethodSpec.methodBuilder("provider" + entry.getKey()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(entry.getValue()).build());
            }
        }
        mMap.put(param.className, JavaFile.builder(param.packname, classBuilder.build()).build());
    }

    /**
     * 生成dagger父组件
     *
     * @param param
     * @return
     */
    private void genDaggerRoot(Param param) {
        TypeSpec.Builder classBuilder = getClassBuilder(param, true);
        if (!TypeName.get(param.scope.asType()).equals(TypeName.OBJECT)) {
            classBuilder.addAnnotation(ClassName.get(param.scope));
        }
        if (param.childs != null && param.childs.size() > 0) {
            Map<String, ClassName> childs = getClassName(param.childs);
            for (Map.Entry<String, ClassName> entry : childs.entrySet()) {
                classBuilder.addMethod(MethodSpec.methodBuilder("provider" + entry.getKey()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(entry.getValue()).build());
            }
        }
        mMap.put(param.className, JavaFile.builder(param.packname, classBuilder.build()).build());
    }

    private TypeSpec.Builder getClassBuilder(Param param, boolean isRoot) {
        List<MethodSpec> methods = new ArrayList<>();
        AnnotationSpec.Builder subComponentBuilder = AnnotationSpec.builder(isRoot ? TypeUtil.COMPONENT : TypeUtil.SUBCOMPONENT);
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        if (param.modules.size() == 1) {
            codeBlockBuilder = codeBlockBuilder.add("$T.class", param.modules.get(0));
            methods.add(MethodSpec.methodBuilder("setModule").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(param.modules.get(0), "module").returns(ClassName.get(param.packname + "." + param.className, "Builder")).build());
        } else {
            for (int i = 0; i < param.modules.size(); i++) {
                TypeName typeName = param.modules.get(i);
                methods.add(MethodSpec.methodBuilder("setModule").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(param.modules.get(i), "module").returns(ClassName.get(param.packname + "." + param.className, "Builder")).build());
                if (i == param.modules.size() - 1) {
                    codeBlockBuilder.add("$T.class}", typeName);
                } else if (i == 0) {
                    codeBlockBuilder.add("{$T.class,", typeName);
                } else {
                    codeBlockBuilder.add("$T.class,", typeName);
                }
            }
        }
        AnnotationSpec subComponent = subComponentBuilder.addMember("modules", codeBlockBuilder.build()).build();
        MethodSpec buildSpec = MethodSpec.methodBuilder("build").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(ClassName.get(param.packname, param.className)).build();
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder("Builder").addAnnotation(isRoot ? TypeUtil.COMPONENTP_BUILDER : TypeUtil.SUBCOMPONENTP_BUILDER).addModifiers(Modifier.STATIC, Modifier.ABSTRACT, Modifier.PUBLIC).addMethod(buildSpec);
        for (MethodSpec method : methods) {
            builder.addMethod(method);
        }
        return TypeSpec.interfaceBuilder(param.className).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(subComponent)
                .addType(builder.build())
                .addSuperinterface(ParameterizedTypeName.get(TypeUtil.MEMBERSINJECTOR, ClassName.get(param.element)));
    }


    private void getHelper() {
        getRootHelper();
        for (MethodSpec methodSpec : methodSpecs) {
            helperTypeBuilder.addMethod(methodSpec);
        }
        for (FieldSpec fieldSpec : fieldSpecs) {
            helperTypeBuilder.addField(fieldSpec);
        }
    }

    private void getRootHelper() {
        for (List<Param> params : roots.values()) {
            for (Param rootParam : params) {
                ClassName className = ClassName.get(rootParam.packname, "Dagger" + rootParam.className);
                ClassName rootComponent = ClassName.get(rootParam.packname, rootParam.className);
                FieldSpec rootField = FieldSpec.builder(rootComponent, rootParam.className.toLowerCase(), Modifier.PUBLIC).build();
                MethodSpec.Builder rootMethodBuilder = MethodSpec.methodBuilder("get" + rootParam.className).addModifiers(Modifier.PUBLIC).returns(rootComponent);
                if (rootParam.shouldInject) {
                    rootMethodBuilder.addParameter(TypeName.get(rootParam.element.asType()), "target");
                }
                count = 0;
                CodeBlock.Builder builder = CodeBlock.builder();
                if (rootParam.childs != null && rootParam.childs.size() > 0) {
                    fieldSpecs.add(rootField);
                    builder = builder.add("$N = $T.builder().", rootField, className);
                } else {
                    builder = builder.add("$T $N = $T.builder().", rootComponent, rootField, className);
                }
                if (rootParam.modules.size() == 1) {
                    TypeName module = rootParam.modules.get(0);
                    genMethodBody(builder, module, rootMethodBuilder);
                } else if (rootParam.modules.size() > 1) {
                    for (int i = 0; i < rootParam.modules.size(); i++) {
                        TypeName module = rootParam.modules.get(i);
                        genMethodBody(builder, module, rootMethodBuilder);
                        if (i != rootParam.modules.size() - 1) {
                            builder.add(".");
                        }
                    }
                }
                builder.add(".build();\n");
                if (rootParam.shouldInject) {
                    builder.add("$N.injectMembers($N);\n", rootField, "target");
                }
                builder.add("return $N;\n", rootField);
                methodSpecs.add(rootMethodBuilder.addCode(builder.build()).build());
                if (rootParam.childs != null && rootParam.childs.size() > 0) {
                    getChildHelper(rootField, rootParam.childs);
                }
            }
        }
    }

    private void getChildHelper(FieldSpec parent, List<Param> childs) {
        for (Param child : childs) {
            ClassName rootComponent = ClassName.get(child.packname, child.className);
            FieldSpec rootField = FieldSpec.builder(rootComponent, child.className.toLowerCase(), Modifier.PRIVATE).build();
            MethodSpec.Builder rootMethodBuilder = MethodSpec.methodBuilder("get" + child.className).addModifiers(Modifier.PUBLIC).returns(rootComponent);
            if (child.shouldInject) {
                rootMethodBuilder.addParameter(TypeName.get(child.element.asType()), "target");
            }
            count = 0;
            CodeBlock.Builder builder = CodeBlock.builder();
            if (child.childs != null && child.childs.size() > 0) {
                fieldSpecs.add(rootField);
                builder = builder.add("$N = $N.$N().", rootField, parent, "provider" + child.className);
            } else {
                builder = builder.add("$T $N = $N.$N().", rootComponent, rootField, parent, "provider" + child.className);
            }
            if (child.modules.size() == 1) {
                TypeName module = child.modules.get(0);
                genMethodBody(builder, module, rootMethodBuilder);
            } else if (child.modules.size() > 1) {
                for (int i = 0; i < child.modules.size(); i++) {
                    TypeName module = child.modules.get(i);
                    genMethodBody(builder, module, rootMethodBuilder);
                    if (i != child.modules.size() - 1) {
                        builder.add(".");
                    }
                }
            }
            builder.add(".build();\n");
            if (child.shouldInject) {
                builder.add("$N.injectMembers($N);\n", rootField, "target");
            }
            builder.add("return $N;\n", rootField);
            methodSpecs.add(rootMethodBuilder.addCode(builder.build()).build());
            if (child.childs != null && child.childs.size() > 0) {
                getChildHelper(rootField, child.childs);
            }
        }
    }

    /**
     * 得到孩子的ClassName
     *
     * @param params
     * @return
     */
    private Map<String, ClassName> getClassName(List<Param> params) {
        Map<String, ClassName> ls = new HashMap<>();
        for (Param param : params) {
            ls.put(param.className, ClassName.get(param.packname, param.className, "Builder"));
        }
        return ls;
    }

    class Param {
        Param(TypeElement scope, List<TypeName> modules, String packname, String className, TypeElement element, List<Param> childs, int childTag, boolean shouldInject) {
            this.scope = scope;
            this.modules = modules;
            this.packname = packname;
            this.className = className;
            this.element = element;
            this.childs = childs;
            this.childTag = childTag;
            this.shouldInject = shouldInject;
        }

        TypeElement scope;
        List<TypeName> modules;
        String packname;
        String className;
        TypeElement element;
        List<Param> childs;
        int childTag;
        boolean shouldInject;
    }


    /**
     * 获取域
     *
     * @param genSubComponent
     * @return
     */
    private TypeElement getScope(GenSubComponent genSubComponent) {
        try {
            genSubComponent.scope();
        } catch (MirroredTypeException e) {
            return (TypeElement) mTypeUtils.asElement(e.getTypeMirror());
        }
        return null;
    }

    /**
     * 获取Module
     *
     * @param genSubComponent
     * @return
     */
    private List<TypeName> getModules(GenSubComponent genSubComponent) {
        try {
            genSubComponent.modules();
        } catch (MirroredTypesException e) {
            List<TypeName> names = new ArrayList<>();
            for (TypeMirror typeMirror : e.getTypeMirrors()) {
                names.add(TypeName.get(typeMirror));
            }
            return names;
        }
        return null;
    }

    /**
     * 获取域
     *
     * @return
     */
    private TypeElement getScope(GenRootComponent genRootComponent) {
        try {
            genRootComponent.scope();
        } catch (MirroredTypeException e) {
            return (TypeElement) mTypeUtils.asElement(e.getTypeMirror());
        }
        return null;
    }

    /**
     * 获取Module
     *
     * @param genRootComponent
     * @return
     */
    private List<TypeName> getModules(GenRootComponent genRootComponent) {
        try {
            genRootComponent.modules();
        } catch (MirroredTypesException e) {
            List<TypeName> names = new ArrayList<>();
            for (TypeMirror typeMirror : e.getTypeMirrors()) {
                names.add(TypeName.get(typeMirror));
            }
            return names;
        }
        return null;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        mMap.clear();
        parents.clear();
        roots.clear();
        modules.clear();
        methodSpecs.clear();
        fieldSpecs.clear();
        proccessDaggerHelper(roundEnv);
        processDaggerParent(roundEnv);
        processDaggerRoot(roundEnv);
        proccessDaggerModule(roundEnv);
        getHelper();
        if (helperTypeBuilder != null) {
            mMap.put(HELPER_CLASS, JavaFile.builder(helper_packname, helperTypeBuilder.build()).build());
            helperTypeBuilder = null;
        }
        try {
            for (JavaFile javaFile : mMap.values()) {
                javaFile.writeTo(mFiler);
            }
        } catch (IOException e) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return true;
    }
}
