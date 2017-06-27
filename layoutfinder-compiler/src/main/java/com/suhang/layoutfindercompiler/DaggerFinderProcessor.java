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
import com.suhang.layoutfinderannotation.GenParentComponent;
import com.suhang.layoutfinderannotation.GenRootComponent;
import com.suhang.layoutfinderannotation.GenSubComponent;

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
    private Map<Integer, List<Param>> groups = new HashMap<>();
    private Map<String, List<TypeName>> modules = new HashMap<>();

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
    private MethodSpec genHelperContent(List<Param> root, List<Param> parent, List<Param> child) {
        if (root.size() == 1) {
            Param rootParam = root.get(0);
            ClassName className = ClassName.get(rootParam.packname, "Dagger" + rootParam.className);
            ClassName rootComponent = ClassName.get(rootParam.packname, rootParam.className);
            FieldSpec rootField = FieldSpec.builder(rootComponent, rootParam.className.toLowerCase(), Modifier.PUBLIC).build();
            helperTypeBuilder.addField(rootField);
            MethodSpec.Builder rootMethodBuilder = MethodSpec.methodBuilder("get" + rootParam.className).addModifiers(Modifier.PUBLIC).returns(rootComponent);
            rootMethodBuilder.addParameter(TypeName.get(rootParam.element.asType()), "target");
            count = 0;
            CodeBlock.Builder builder = CodeBlock.builder().add("$N = $T.builder().", rootField, className);
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
            builder.add("$N.injectMembers($N);\n", rootField, "target");
            builder.add("return $N;\n", rootField);
            return rootMethodBuilder.addCode(builder.build()).build();
        }
        return null;
    }

    private void genMethodBody(CodeBlock.Builder builder, TypeName module, MethodSpec.Builder rootMethodBuilder) {
        builder = builder.add("setModule(new $T", module);
//        mMessager.printMessage(Diagnostic.Kind.ERROR, builder.build().toString());
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

        FieldSpec instance = FieldSpec.builder(className, "instance").addModifiers(Modifier.PRIVATE, Modifier.STATIC).build();
        MethodSpec construct = MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build();

        FieldSpec instance1 = FieldSpec.builder(className, "INSTANCE", Modifier.STATIC, Modifier.PRIVATE, Modifier.FINAL).initializer("new $T()", className).build();
        TypeSpec holder = TypeSpec.classBuilder("Holder").addModifiers(Modifier.STATIC, Modifier.PRIVATE).addField(instance1).build();
        MethodSpec getInstance = MethodSpec.methodBuilder("getInstance").addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(className)
                .addStatement("return $N.$N", holder, instance1)
                .build();
        return TypeSpec.classBuilder(HELPER_CLASS).addModifiers(Modifier.PUBLIC).addType(holder).addMethod(getInstance).addMethod(construct).addField(instance);
    }

    /**
     * 解析注解
     *
     * @param roundEnv
     * @return
     */
    private void processDagger(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenSubComponent.class)) {
            String packname = mElements.getPackageOf(element).getQualifiedName().toString();
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString() + "Component";
            GenSubComponent genSubComponent = element.getAnnotation(GenSubComponent.class);
            int tag = genSubComponent.tag();
            TypeElement scope = getScope(genSubComponent);
            List<TypeName> modules = getModules(genSubComponent);
            List<Param> params = groups.get(tag);
            if (params == null) {
                params = new ArrayList<>();
            }
            Param param = new Param(scope, modules, packname, className, typeElement, Param.CHILD, null);
            params.add(param);
            groups.put(tag, params);
        }
    }

    private void processDaggerParent(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenParentComponent.class)) {
            String packname = mElements.getPackageOf(element).getQualifiedName().toString();
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString() + "Component";
            GenParentComponent genSubComponent = element.getAnnotation(GenParentComponent.class);
            int tag = genSubComponent.tag();
            TypeElement scope = getScope(genSubComponent);
            List<TypeName> modules = getModules(genSubComponent);
            List<Param> params = groups.get(tag);
            if (params == null) {
                params = new ArrayList<>();
            }
            Param param = new Param(scope, modules, packname, className, typeElement, Param.PARENT, new HashMap<String, ClassName>());
            params.add(param);
            groups.put(tag, params);
        }
    }

    private void processDaggerRoot(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenRootComponent.class)) {
            String packname = mElements.getPackageOf(element).getQualifiedName().toString();
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString() + "Component";
            GenRootComponent genSubComponent = element.getAnnotation(GenRootComponent.class);
            int tag = genSubComponent.tag();
            TypeElement scope = getScope(genSubComponent);
            List<TypeName> modules = getModules(genSubComponent);
            List<Param> params = groups.get(tag);
            if (params == null) {
                params = new ArrayList<>();
            }
            Param param = new Param(scope, modules, packname, className, typeElement, Param.ROOT, new HashMap<String, ClassName>());
            params.add(param);
            groups.put(tag, params);
        }
    }

    /**
     * 生成dagger子组件
     *
     * @param param
     * @return
     */
    private void genDaggerChild(Param param) {
        TypeSpec.Builder classBuilder = getClassBuilder(param, false);
        if (!TypeName.get(param.scope.asType()).equals(TypeName.OBJECT)) {
            classBuilder.addAnnotation(ClassName.get(param.scope));
        }
        mMap.put(param.className, JavaFile.builder(param.packname, classBuilder.build()).build());
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
        for (Map.Entry<String, ClassName> entry : param.childs.entrySet()) {
            classBuilder.addMethod(MethodSpec.methodBuilder("provider" + entry.getKey()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(entry.getValue()).build());
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
        for (Map.Entry<String, ClassName> entry : param.childs.entrySet()) {
            classBuilder.addMethod(MethodSpec.methodBuilder("provider" + entry.getKey()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .returns(entry.getValue()).build());
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


    private void getGroup() {
        for (List<Param> params : groups.values()) {
            List<Param> child = new ArrayList<>();
            List<Param> parent = new ArrayList<>();
            List<Param> root = new ArrayList<>();
            for (Param param : params) {
                switch (param.type) {
                    case Param.CHILD:
                        child.add(param);
                        break;
                    case Param.PARENT:
                        parent.add(param);
                        break;
                    case Param.ROOT:
                        root.add(param);
                        break;
                }
            }
            if (root.size() > 1) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "同一个Tag类型只能有一个Root组件");
            } else if (root.size() == 1 && parent.size() == 1) {
                root.get(0).childs = getClassName(parent);
            } else if (root.size() == 1 && parent.size() == 0) {
                root.get(0).childs = getClassName(child);
            }

            if (parent.size() > 1) {
                mMessager.printMessage(Diagnostic.Kind.ERROR, "同一个Tag类型只能有一个父组件");
            } else if (parent.size() == 1) {
                parent.get(0).childs = getClassName(child);
            }

            for (Param param : child) {
                genDaggerChild(param);
            }

            for (Param param : parent) {
                genDaggerParent(param);
            }

            for (Param param : root) {
                genDaggerRoot(param);
            }
            MethodSpec methodSpec = genHelperContent(root, parent, child);
            helperTypeBuilder.addMethod(methodSpec);
        }
    }

    private Map<String, ClassName> getClassName(List<Param> params) {
        Map<String, ClassName> ls = new HashMap<>();
        for (Param param : params) {
            ls.put(param.className, ClassName.get(param.packname, param.className, "Builder"));
        }
        return ls;
    }

    class Param {
        public static final int ROOT = 100;
        public static final int PARENT = 101;
        public static final int CHILD = 102;

        Param(TypeElement scope, List<TypeName> modules, String packname, String className, TypeElement element, int type, Map<String, ClassName> childs) {
            this.scope = scope;
            this.modules = modules;
            this.packname = packname;
            this.className = className;
            this.element = element;
            this.type = type;
            this.childs = childs;
        }

        int type;
        TypeElement scope;
        List<TypeName> modules;
        String packname;
        String className;
        TypeElement element;
        Map<String, ClassName> childs;
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
     * @param genParentComponent
     * @return
     */
    private TypeElement getScope(GenParentComponent genParentComponent) {
        try {
            genParentComponent.scope();
        } catch (MirroredTypeException e) {
            return (TypeElement) mTypeUtils.asElement(e.getTypeMirror());
        }
        return null;
    }

    /**
     * 获取Module
     *
     * @param genParentComponent
     * @return
     */
    private List<TypeName> getModules(GenParentComponent genParentComponent) {
        try {
            genParentComponent.modules();
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
        groups.clear();
        modules.clear();
        proccessDaggerHelper(roundEnv);
        processDagger(roundEnv);
        processDaggerParent(roundEnv);
        processDaggerRoot(roundEnv);
        proccessDaggerModule(roundEnv);
        getGroup();
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
