package com.suhang.layoutfindercompiler.dagger;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.suhang.layoutfindercompiler.TypeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;

/**
 * Created by 苏杭 on 2017/5/17 17:34.
 */

public class SubComponentClass {
    private ComponentProcessor.ComponentParam mParam;
    private Messager mMessager;

    public SubComponentClass(Messager messager, ComponentProcessor.ComponentParam param) {
        mParam = param;
        mMessager = messager;
    }


    private AnnotationSpec getComponent() {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(TypeUtil.SUBCOMPONENT);
        List<ClassName> modules = mParam.mModules;
        if (modules != null) {
            String[] ms = new String[modules.size()];
            if (modules.size() == 1) {
                ClassName className = modules.get(0);
                String pack = className.packageName() + "." + className.simpleName();
                ms[0] = pack;
                builder.addMember("modules", "$T.class", className);
            } else {
                CodeBlock.Builder codeBuilder = CodeBlock.builder();
                for (int i = 0; i < modules.size(); i++) {
                    ClassName className = modules.get(i);
                    String pack = className.packageName() + "." + className.simpleName();
                    ms[i] = pack;
                    if (i == 0) {
                        codeBuilder.add("{$T.class,", className);
                    } else if (i == modules.size() - 1) {
                        codeBuilder.add("$T.class}", className);
                    } else {
                        codeBuilder.add("$T.class,", className);
                    }
                }
                builder.addMember("modules", codeBuilder.build());
            }
        }
        return builder.build();
    }

    private List<MethodSpec> getSubcomponent() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (Map.Entry<ClassName, List<ClassName>> entry : mParam.mSubcomponents.entrySet()) {
            ClassName key = entry.getKey();
            List<ClassName> value = entry.getValue();
            MethodSpec.Builder builder = MethodSpec.methodBuilder("provider" + key.simpleName()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(key);
            for (int i = 0; i < value.size(); i++) {
                ClassName className = value.get(i);
                builder.addParameter(className, "module" + i);
            }
            methodSpecs.add(builder.build());
        }
        return methodSpecs;
    }

    private List<MethodSpec> getInject() {
        List<MethodSpec> methodSpecs = new ArrayList<>();
        for (ClassName inject : mParam.mInjects) {
            MethodSpec.Builder builder = MethodSpec.methodBuilder("inject").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            builder.addParameter(inject, "param");
            methodSpecs.add(builder.build());
        }
        return methodSpecs;
    }

    public JavaFile gen() {
        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(mParam.name)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getComponent())
                .addAnnotation(mParam.scope);
        List<MethodSpec> methodSpecs = getSubcomponent();
        for (MethodSpec methodSpec : methodSpecs) {
            builder.addMethod(methodSpec);
        }
        List<MethodSpec> inject = getInject();
        for (MethodSpec methodSpec : inject) {
            builder.addMethod(methodSpec);
        }
        return JavaFile.builder(mParam.packname, builder.build()).build();
    }
}
