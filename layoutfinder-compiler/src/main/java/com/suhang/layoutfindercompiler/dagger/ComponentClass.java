package com.suhang.layoutfindercompiler.dagger;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.suhang.layoutfinderannotation.dagger.GenInject;
import com.suhang.layoutfinderannotation.dagger.Keys;
import com.suhang.layoutfindercompiler.TypeUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by 苏杭 on 2017/5/17 17:34.
 */

public class ComponentClass {
    private ComponentProcessor.ComponentParam mParam;
    private Messager mMessager;

    public ComponentClass(Messager messager, ComponentProcessor.ComponentParam param) {
        mParam = param;
        mMessager = messager;
    }


    private AnnotationSpec getComponent() {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(TypeUtil.COMPONENT);
        TypeElement[] modules = mParam.mModules;
        if (modules != null) {
            String[] ms = new String[modules.length];
            if (modules.length == 1) {
                ClassName className = ClassName.get(modules[0]);
                String pack = className.packageName() + "." + className.simpleName();
                ms[0] = pack;
                builder.addMember("modules", "$T.class",modules[0]);
            } else {
                CodeBlock.Builder codeBuilder = CodeBlock.builder();
                for (int i = 0; i < modules.length; i++) {
                    TypeElement element = modules[i];
                    ClassName className = ClassName.get(element);
                    String pack = className.packageName() + "." + className.simpleName();
                    ms[i] = pack;
                    if (i == 0) {
                        codeBuilder.add("{$T.class,", element);
                    } else if (i == modules.length - 1) {
                        codeBuilder.add("$T.class}", element);
                    } else {
                        codeBuilder.add("$T.class,", element);
                    }
                }
                builder.addMember("modules", codeBuilder.build());
            }
        }
        TypeElement[] dependencies = mParam.mDependencies;
        if (dependencies != null) {
            if (dependencies.length == 1) {
                builder.addMember("dependencies", "$T.class",dependencies[0]);
            } else {
                CodeBlock.Builder codeBuilder = CodeBlock.builder();
                for (int i = 0; i < dependencies.length; i++) {
                    TypeElement element = dependencies[i];
                    if (i == 0) {
                        codeBuilder.add("{$T.class,", element);
                    } else if (i == dependencies.length - 1) {
                        codeBuilder.add("$T.class}", element);
                    } else {
                        codeBuilder.add("$T.class,", element);
                    }
                }
                builder.addMember("dependencies", codeBuilder.build());
            }
        }
        return builder.build();
    }

//    private List<MethodSpec> getSubcomponent() {
//        List<MethodSpec> methodSpecs = new ArrayList<>();
//        TypeElement[] subcomponents = mParam.mSubcomponents;
//        TypeElement[] submodules = mParam.mSubmodules;
//        if (subcomponents != null&&submodules!=null&&subcomponents.length==submodules.length) {
//            for (int i = 0; i < subcomponents.length; i++) {
//                TypeElement component = subcomponents[i];
//                TypeElement module = submodules[i];
//                methodSpecs.add(MethodSpec.methodBuilder("provider_" + component.getSimpleName())
//                        .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
//                        .addParameter(TypeName.get(module.asType()), "module").build());
//            }
//        }
//        return methodSpecs;
//    }


    public JavaFile gen() {
        AnnotationSpec build = AnnotationSpec.builder(GenInject.class).addMember("name", "$S", mParam.packname + "." + mParam.name).build();
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(mParam.name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getComponent())
                .addAnnotation(build);
//        mMessager.printMessage(Diagnostic.Kind.ERROR,mPackname+mParam.name+"    "+className.packageName()+className.simpleName());
//        Keys.genComponents.put(mPackname+"."+mParam.name,)
        return JavaFile.builder(mParam.packname, builder.build()).build();
    }
}
