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
import com.suhang.layoutfinderannotation.GenInheritedSubComponent;
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
import javax.lang.model.type.DeclaredType;
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
        types.add(GenInheritedSubComponent.class.getCanonicalName());
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

    /**
     * 判断继承关系
     *
     * @param e
     * @param parent
     * @return
     */
    private boolean isSubType(Element e, Element parent) {
        if (e instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) e;
            TypeMirror mirror = typeElement.getSuperclass();
            if (mirror instanceof DeclaredType) {
                DeclaredType superclass = (DeclaredType) mirror;
                //判断该元素的父类是否为enclosingElement
                if (superclass.asElement().equals(parent)) {
                    return true;
                }
            }
        }
        return false;
    }


    private Map<String, List<TypeElement>> inheritedMap = new HashMap<>();
    private List<TypeElement> inheritedSubs = new ArrayList<>();
    Map<TypeElement, List<TypeElement>> results = new HashMap<>();

    /**
     * 递归得到基类元素及其子类集合
     *
     * @param element
     * @param ls
     */
    private void getParentElement(TypeElement element, List<TypeElement> ls) {
        DeclaredType dt = (DeclaredType) element.getSuperclass();
        TypeElement te = (TypeElement) dt.asElement();
        List<TypeElement> elementList = inheritedMap.get(te.getSimpleName().toString());
        GenInheritedSubComponent annotation = te.getAnnotation(GenInheritedSubComponent.class);
        if (annotation != null && elementList != null && elementList.size() > 0) {
            elementList.addAll(ls);
            getParentElement(te, elementList);
        } else {
            results.put(element, ls);
        }
    }

    /**
     * 得到GenInheritedSubComponent注解的基类,并找出基类的子类
     *
     * @param roundEnv
     */
    private void processDaggerInheritedSub(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenInheritedSubComponent.class)) {
            TypeElement typeElement = (TypeElement) element;
            DeclaredType superclass = (DeclaredType) typeElement.getSuperclass();
            String name = superclass.asElement().getSimpleName().toString();
            List<TypeElement> elements = inheritedMap.get(name);
            if (elements == null) {
                elements = new ArrayList<>();
                if (typeElement.getAnnotation(GenSubComponent.class) == null) {
                    elements.add(typeElement);
                }
            } else {
                if (typeElement.getAnnotation(GenSubComponent.class) == null) {
                    elements.add(typeElement);
                }
            }
            inheritedSubs.add(typeElement);
            inheritedMap.put(name, elements);
        }

        for (TypeElement inheritedSub : inheritedSubs) {
            List<TypeElement> elements = inheritedMap.get(inheritedSub.getSimpleName().toString());
            if (elements != null && elements.size() > 0) {
                getParentElement(inheritedSub, elements);
            }
        }

        for (Map.Entry<TypeElement, List<TypeElement>> entry : results.entrySet()) {
            TypeElement typeElement = entry.getKey();
            String packname = mElements.getPackageOf(typeElement).getQualifiedName().toString();
            String className = typeElement.getSimpleName().toString() + "Component";
            GenInheritedSubComponent genSubComponent = typeElement.getAnnotation(GenInheritedSubComponent.class);
            int childTag = genSubComponent.childTag();
            int tag = genSubComponent.tag();
            boolean shouldInject = genSubComponent.shouldInject();
            TypeElement scope = getScope(genSubComponent);
            List<TypeName> modules = getModules(genSubComponent);
            List<Param> params = parents.get(tag);
            if (params == null) {
                params = new ArrayList<>();
            }
            Param param;
            if (childTag == 0) {
                param = new Param(scope, modules, packname, className, typeElement, null, childTag, shouldInject, getClassNames(entry.getValue()));
            } else {
                param = new Param(scope, modules, packname, className, typeElement, new ArrayList<Param>(), childTag, shouldInject, getClassNames(entry.getValue()));
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

    private List<ClassName> getClassNames(List<TypeElement> elements) {
        List<ClassName> ls = new ArrayList<>();
        for (TypeElement element : elements) {
            ls.add(ClassName.get(element));
        }
        return ls;
    }


    private void processDaggerSub(RoundEnvironment roundEnv) {
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
                param = new Param(scope, modules, packname, className, typeElement, null, childTag, shouldInject, null);
            } else {
                param = new Param(scope, modules, packname, className, typeElement, new ArrayList<Param>(), childTag, shouldInject, null);
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
            Param param = new Param(scope, modules, packname, className, typeElement, parent, childTag, shouldInject, null);
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
        TypeSpec.Builder builder1 = TypeSpec.interfaceBuilder(param.className).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(subComponent)
                .addType(builder.build());
        if (param.inheriedChilds == null) {
            if (param.shouldInject) {
                builder1.addSuperinterface(ParameterizedTypeName.get(TypeUtil.MEMBERSINJECTOR, ClassName.get(param.element)));
            }
        } else {
            for (ClassName inheriedChild : param.inheriedChilds) {
                builder1.addMethod(MethodSpec.methodBuilder("injectMembers").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(inheriedChild, "target").build());
            }
            if (param.shouldInject) {
                builder1.addMethod(MethodSpec.methodBuilder("injectMembers").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(ClassName.get(param.packname, param.element.getSimpleName().toString()), "target").build());
            }
        }
        return builder1;
    }


    private void getHelper() {
        getRootHelper();
        ParameterizedTypeName map = ParameterizedTypeName.get(HashMap.class, Object.class, Integer.class);
        FieldSpec rootFieldAppend = FieldSpec.builder(map, "maps", Modifier.PUBLIC).initializer("new $T()", map).build();
        MethodSpec.Builder unProvider = MethodSpec.methodBuilder("removeComponent").addModifiers(Modifier.PUBLIC);
        unProvider.addParameter(TypeName.OBJECT, "target");
        unProvider.beginControlFlow("if($N.get($N)==null)",rootFieldAppend, "target");
        unProvider.addStatement("return");
        unProvider.endControlFlow();
        unProvider.addStatement("$N.put($N,$N.get($N)-1)", rootFieldAppend, "target", rootFieldAppend, "target");
        unProvider.beginControlFlow("if($N.get($N)==0)",rootFieldAppend, "target");
        unProvider.addStatement("$N.remove($N)", rootFieldAppend, "target");
        for (FieldSpec fieldSpec : fieldSpecs) {
            unProvider.beginControlFlow("if($N==$N)", "target", fieldSpec);
            unProvider.addStatement("$N=null", fieldSpec);
            unProvider.endControlFlow();
        }
        unProvider.endControlFlow();
        for (int i = 0; i < methodSpecs.size(); i++) {
            MethodSpec spec = methodSpecs.get(i);
            //不在此循环里添加,会报空指针!,匪夷所思啊
            if (i == methodSpecs.size() - 1) {
                helperTypeBuilder.addMethod(unProvider.build());
            }
            helperTypeBuilder.addMethod(spec);
        }
        for (int i = 0; i < fieldSpecs.size(); i++) {
            FieldSpec spec = fieldSpecs.get(i);
            if (i == fieldSpecs.size() - 1) {
                helperTypeBuilder.addField(rootFieldAppend);
            }
            helperTypeBuilder.addField(spec);
        }
    }

    private void getRootHelper() {
        for (List<Param> params : roots.values()) {
            for (Param rootParam : params) {
                ClassName className = ClassName.get(rootParam.packname, "Dagger" + rootParam.className);
                ClassName rootComponent = ClassName.get(rootParam.packname, rootParam.className);
                FieldSpec rootField = FieldSpec.builder(rootComponent, rootParam.className.toLowerCase(), Modifier.PUBLIC).build();
                ClassName target = ClassName.get(rootParam.packname, rootParam.element.getSimpleName().toString());
                MethodSpec.Builder rootMethodBuilder = MethodSpec.methodBuilder("get" + rootParam.className).addModifiers(Modifier.PUBLIC).returns(rootComponent);
                if (rootParam.shouldInject) {
                    rootMethodBuilder.addParameter(target, "target");
                }
                count = 0;
                CodeBlock.Builder builder = CodeBlock.builder();
                if (rootParam.childs != null && rootParam.childs.size() > 0) {
                    fieldSpecs.add(rootField);
                    builder.beginControlFlow("if($N==null)",rootField);
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
                if (rootParam.childs != null && rootParam.childs.size() > 0) {
                    builder.endControlFlow();
                }
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
            if (child.inheriedChilds == null) {
                if (child.shouldInject) {
                    rootMethodBuilder.addParameter(ClassName.get(child.packname, child.element.getSimpleName().toString()), "target");
                }
            } else {
                rootMethodBuilder.addParameter(TypeName.OBJECT, "target");
            }
            count = 0;
            CodeBlock.Builder builder = CodeBlock.builder();
            if (child.childs != null && child.childs.size() > 0) {
                fieldSpecs.add(rootField);
                builder.beginControlFlow("if($N==null)",rootField);
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
            if (child.childs != null && child.childs.size() > 0) {
                builder.endControlFlow();
            }
            if (child.childs != null && child.childs.size() > 0) {
                builder.beginControlFlow("if($N.get($N)==null)", "maps", rootField);
                builder.addStatement("$N.put($N,0)", "maps", rootField);
                builder.endControlFlow();
                builder.add("$N.put($N,$N.get($N)+1);\n", "maps", rootField, "maps", rootField);
            }
            if (child.inheriedChilds == null) {
                if (child.shouldInject) {
                    builder.add("$N.injectMembers($N);\n", rootField, "target");
                }
            } else {
                for (ClassName inheriedChild : child.inheriedChilds) {
                    builder.beginControlFlow("if($N instanceof $T)", "target", inheriedChild);
                    builder.addStatement("$T $N = ($T)$N", inheriedChild, inheriedChild.simpleName().toLowerCase(), inheriedChild, "target");
                    builder.addStatement("$N.injectMembers($N)", rootField, inheriedChild.simpleName().toLowerCase());
                    builder.endControlFlow();
                }
                if (child.shouldInject) {
                    ClassName className = ClassName.get(child.packname, child.element.getSimpleName().toString());
                    String name = child.element.getSimpleName().toString().toLowerCase();
                    builder.beginControlFlow("if($N instanceof $T)", "target", className);
                    builder.addStatement("$T $N = ($T)$N", className, name, className, "target");
                    builder.addStatement("$N.injectMembers($N)", rootField, name);
                    builder.endControlFlow();
                }
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
        Param(TypeElement scope, List<TypeName> modules, String packname, String className, TypeElement element, List<Param> childs, int childTag, boolean shouldInject, List<ClassName> inheriedChilds) {
            this.scope = scope;
            this.modules = modules;
            this.packname = packname;
            this.className = className;
            this.element = element;
            this.childs = childs;
            this.childTag = childTag;
            this.shouldInject = shouldInject;
            this.inheriedChilds = inheriedChilds;
        }

        TypeElement scope;
        List<TypeName> modules;
        String packname;
        String className;
        TypeElement element;
        List<Param> childs;
        int childTag;
        boolean shouldInject;
        List<ClassName> inheriedChilds;
    }


    /**
     * 获取域
     *
     * @param genInheritedSubComponent
     * @return
     */
    private TypeElement getScope(GenInheritedSubComponent genInheritedSubComponent) {
        try {
            genInheritedSubComponent.scope();
        } catch (MirroredTypeException e) {
            return (TypeElement) mTypeUtils.asElement(e.getTypeMirror());
        }
        return null;
    }

    /**
     * 获取Module
     *
     * @param genInheritedSubComponent
     * @return
     */
    private List<TypeName> getModules(GenInheritedSubComponent genInheritedSubComponent) {
        try {
            genInheritedSubComponent.modules();
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
        inheritedSubs.clear();
        inheritedMap.clear();
        results.clear();
        proccessDaggerHelper(roundEnv);
        processDaggerInheritedSub(roundEnv);
        processDaggerSub(roundEnv);
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
