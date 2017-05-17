package com.suhang.layoutfindercompiler.dagger;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.suhang.layoutfindercompiler.TypeUtil;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by 苏杭 on 2017/5/17 17:34.
 */

public class ComponentClass {
    private String mPackname;
    private ComponentProcessor.ComponentParam mParam;

    public ComponentClass(String packname, ComponentProcessor.ComponentParam param) {
        mPackname = packname;
        mParam = param;
    }


    private AnnotationSpec getComponent() {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(TypeUtil.COMPONENT);
        TypeElement[] modules = mParam.mModules;
        if (modules != null) {
            if (modules.length == 1) {
                builder.addMember("modules", "$T.class",modules[0]);
            } else {
                CodeBlock.Builder codeBuilder = CodeBlock.builder();
                for (int i = 0; i < modules.length; i++) {
                    TypeElement element = modules[i];
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

    private MethodSpec getSubcomponent() {
        TypeElement[] subcomponent = mParam.mSubcomponent;
        if (subcomponent != null) {
            for (TypeElement element : subcomponent) {
                MethodSpec.methodBuilder("provider_"+element.getSimpleName())
                        .addModifiers(Modifier.ABSTRACT,Modifier.PUBLIC);
            }
        }
        return null;
    }


    public JavaFile gen() {
        TypeSpec typeSpec = TypeSpec.interfaceBuilder(mParam.name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getComponent())
                .build();
        return JavaFile.builder(mPackname, typeSpec).build();
    }
}
