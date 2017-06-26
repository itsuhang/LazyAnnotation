package com.suhang.layoutfindercompiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.suhang.layoutfinderannotation.GenComponent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by 苏杭 on 2017/6/12 22:03.
 */
@AutoService(Processor.class)
public class DaggerFinderProcessor extends AbstractProcessor {
    Filer mFiler;
    Elements mElements;
    Messager mMessager;
    private Types mTypeUtils;

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
        types.add(GenComponent.class.getCanonicalName());
        return types;
    }

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

    private Map<TypeMirror, List<TypeMirror>> listMap = new HashMap<>();

    private JavaFile processDagger(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(GenComponent.class)) {
            String packname = mElements.getPackageOf(element).getQualifiedName().toString();
            TypeElement typeElement = (TypeElement) element;
            String className = typeElement.getSimpleName().toString() + "Component";
            GenComponent genComponent = element.getAnnotation(GenComponent.class);
            TypeElement scope = getScope(genComponent);
            List<TypeName> modules = getModules(genComponent);
            Param param = new Param(scope, modules, packname, className, typeElement);
            return genDagger(param);
//            MethodSpec spec = MethodSpec.methodBuilder("activityModule").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(TypeUtil.MODULE, "module").returns(ClassName.get(packname+"."+typeElement.getSimpleName().toString()+"Component", "Builder")).build();
//            MethodSpec spec1 = MethodSpec.methodBuilder("build").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(ClassName.get(packname, typeElement.getSimpleName().toString() + "Component")).build();
//            TypeSpec builder = TypeSpec.interfaceBuilder("Builder").addAnnotation(TypeUtil.SUBCOMPONENTP_BUILDER).addModifiers(Modifier.STATIC,Modifier.ABSTRACT, Modifier.PUBLIC).addMethod(spec).addMethod(spec1).build();
//            AnnotationSpec a = AnnotationSpec.builder(TypeUtil.SUBCOMPONENT).addMember("modules", "$T.class", TypeUtil.MODULE).build();
//            TypeSpec typeSpec = TypeSpec.interfaceBuilder(typeElement.getSimpleName().toString() + "Component").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
//                    .addAnnotation(a)
//                    .addAnnotation(TypeUtil.SCOPE)
//                    .addSuperinterface(ParameterizedTypeName.get(TypeUtil.INJECT, ClassName.get(typeElement)))
//                    .addType(builder).build();
//            return JavaFile.builder(packname,typeSpec).build();
        }
//        for (Map.Entry<TypeMirror, List<TypeMirror>> entry : listMap.entrySet()) {
//            mMessager.printMessage(Diagnostic.Kind.ERROR,entry.getKey()+"   "+entry.getValue());
//        }
        return null;
    }

    private JavaFile genDagger(Param param) {
        MethodSpec.Builder getModuleBuilder = MethodSpec.methodBuilder("setModule").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(ClassName.get(param.packname + "." + param.className, "Builder"));
        AnnotationSpec.Builder subComponentBuilder = AnnotationSpec.builder(TypeUtil.SUBCOMPONENT);
        CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
        if (param.modules.size() == 1) {
            codeBlockBuilder = codeBlockBuilder.add("$T.class", param.modules.get(0));
        } else {
            for (int i = 0; i < param.modules.size(); i++) {
                TypeName typeName = param.modules.get(i);
                getModuleBuilder.addParameter(typeName, "module" + i);
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
        TypeSpec typeSpec = TypeSpec.interfaceBuilder(param.className).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addAnnotation(subComponent)
                .addAnnotation(ClassName.get(param.scope))
                .addSuperinterface(ParameterizedTypeName.get(TypeUtil.MEMBERSINJECTOR, ClassName.get(param.element)))
                .build();
        return JavaFile.builder(param.packname, typeSpec).build();
    }


    class Param {
        Param(TypeElement scope, List<TypeName> modules, String packname, String className, TypeElement element) {
            this.scope = scope;
            this.modules = modules;
            this.packname = packname;
            this.className = className;
            this.element = element;
        }

        TypeElement scope;
        List<TypeName> modules;
        String packname;
        String className;
        TypeElement element;
    }


    private TypeElement getScope(GenComponent genComponent) {
        try {
            genComponent.scope();
        } catch (MirroredTypeException e) {
            return (TypeElement) mTypeUtils.asElement(e.getTypeMirror());
        }
        return null;
    }

    private List<TypeName> getModules(GenComponent genComponent) {
        try {
            genComponent.modules();
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
        JavaFile javaFile = processDagger(roundEnv);
        if (javaFile != null) {
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
}
