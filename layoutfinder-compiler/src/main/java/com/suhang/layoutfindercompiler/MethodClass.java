package com.suhang.layoutfindercompiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

public class MethodClass {
    private Elements mElements;
    private TypeElement mElement;
    private Map<String, String> mParams;
    public MethodClass(Elements elements,Element element,Map<String, String> params) {
        mElements = elements;
        mElement = (TypeElement) element;
        mParams = params;
    }


    public JavaFile gen() {
        String packageName = mElements.getPackageOf(mElement).getQualifiedName().toString();
        ClassName name = ClassName.get(mElement);
        MethodSpec.Builder build = MethodSpec.methodBuilder("find")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(name, "o")
                .addParameter(ParameterizedTypeName.get(Map.class,String.class,String.class),"params")
                .addParameter(TypeName.get(String.class), "url")
                .returns(TypeUtil.FLOWABLE);
        for (Map.Entry<String, String> entry : mParams.entrySet()) {
            build.beginControlFlow("if($N.equals($S))", "url", entry.getValue());
            build.addStatement("$T fl = $N.$N($N)",TypeUtil.FLOWABLE,"o",entry.getKey(),"params");
            build.addStatement("return $N", "fl");
            build.endControlFlow();
        }
        build.addStatement("return null");
        try {
            TypeSpec layoutFinder = TypeSpec.classBuilder("Method" + "$$Finder")
                    .addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(ParameterizedTypeName.get(TypeUtil.BASEMETHODFINDER,name))
                    .addMethod(build.build())
                    .build();
            return JavaFile.builder(packageName, layoutFinder).build();
        } catch (NullPointerException e) {

        }
        return null;
    }
}