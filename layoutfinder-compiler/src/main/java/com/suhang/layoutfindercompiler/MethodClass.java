package com.suhang.layoutfindercompiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

public class MethodClass {
    private Elements mElements;
    private TypeElement mElement;
    private Map<String, Param> mPostParams = new HashMap<>();

    public MethodClass(Elements elements, Element element) {
        mElements = elements;
        mElement = (TypeElement) element;
        getPostParams();
        getGetParams();
    }

    private void getPostParams() {
        for (Element element : mElement.getEnclosedElements()) {
            ExecutableElement executableElement = (ExecutableElement) element;
            for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                DeclaredType type = annotationMirror.getAnnotationType();
                if (ClassName.get(type).equals(TypeUtil.POST)) {
                    List<TypeName> ls = new ArrayList<>();
                    for (AnnotationValue annotationValue : annotationMirror.getElementValues().values()) {
                        for (VariableElement variableElement : executableElement.getParameters()) {
                            ls.add(TypeName.get(variableElement.asType()));
                        }
                        mPostParams.put(element.getSimpleName().toString(), new Param(ls, annotationValue.getValue().toString()));
                    }
                }
            }
        }
    }

    private void getGetParams() {
        List<TypeName> ls = new ArrayList<>();
        for (Element element : mElement.getEnclosedElements()) {
            ExecutableElement executableElement = (ExecutableElement) element;
            for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                DeclaredType type = annotationMirror.getAnnotationType();
                if (ClassName.get(type).equals(TypeUtil.GET)) {
                    for (AnnotationValue annotationValue : annotationMirror.getElementValues().values()) {
                        for (VariableElement variableElement : executableElement.getParameters()) {
                            ls.add(TypeName.get(variableElement.asType()));
                        }
                        mPostParams.put(element.getSimpleName().toString(), new Param(ls, annotationValue.getValue().toString()));
                    }
                }
            }
        }
    }

    private MethodSpec createSearch() {
        MethodSpec.Builder buildGet = MethodSpec.methodBuilder("search")
                .addModifiers(Modifier.PRIVATE)
                .addParameter(TypeName.get(String.class), "url")
                .addParameter(TypeName.get(Object[].class), "objects")
                .returns(TypeUtil.FLOWABLE);
        buildGet.addCode("try{\n");
        for (Map.Entry<String, Param> entry : mPostParams.entrySet()) {
            buildGet.beginControlFlow("if($N.equals($S))", "url", entry.getValue().url);
            buildGet.addCode("$T fl = this.$N.$N(", TypeUtil.FLOWABLE, "o", entry.getKey());
            for (int i = 0; i < entry.getValue().ls.size(); i++) {
                TypeName name = entry.getValue().ls.get(i);
                if (entry.getValue().ls.size() == 1) {
                    buildGet.addCode("($T)$N[$L]);\n", name, "objects", i);
                } else if (i == entry.getValue().ls.size() - 1) {
                    buildGet.addCode("($T)$N[$L]);\n", name, "objects", i);
                } else {
                    buildGet.addCode("($T)$N[$L],", name, "objects", i);
                }
            }
            buildGet.addStatement("return $N", "fl");
            buildGet.endControlFlow();
        }
        buildGet.addCode("}catch($T e){\nthrow new $T($S);\n}\n", TypeName.get(Exception.class), TypeName.get(RuntimeException.class), "MethodFinder.find()方法参数数量与Service中方法参数数量不一致,或参数顺序不正确");
        buildGet.addStatement("return null");
        return buildGet.build();
    }

    class Param {
        List<TypeName> ls;
        public String url;

        public Param(List<TypeName> ls, String url) {
            this.ls = ls;
            this.url = url;
        }
    }


    public JavaFile gen() {
        String packageName = mElements.getPackageOf(mElement).getQualifiedName().toString();
        ClassName name = ClassName.get(mElement);
        MethodSpec inject = MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(name, "o")
                .addStatement("this.$N = $N", "o", "o")
                .build();
        MethodSpec postGet = MethodSpec.methodBuilder("find")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(TypeName.get(String.class), "url")
                .addParameter(TypeName.get(Object[].class), "objects")
                .addStatement("return $N($N,$N)", "search", "url", "objects")
                .returns(TypeUtil.FLOWABLE).build();
        TypeSpec layoutFinder = TypeSpec.classBuilder(name.simpleName() + "$$Finder")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(TypeUtil.BASEMETHODFINDER, name))
                .addMethod(inject)
                .addMethod(createSearch())
                .addMethod(postGet)
                .addField(name, "o")
                .build();
        return JavaFile.builder(packageName, layoutFinder).build();
    }
}