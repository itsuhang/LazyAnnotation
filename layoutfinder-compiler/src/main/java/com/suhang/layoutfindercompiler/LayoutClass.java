package com.suhang.layoutfindercompiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

public class LayoutClass {

    public TypeElement mClassElement;
    public Elements mElements;
    public VariableElement mElement;

    public LayoutClass(TypeElement classElement, Elements elements, Element element) {
        this.mClassElement = classElement;
        this.mElements = elements;
        this.mElement = (VariableElement) element;
    }


    public JavaFile gen() {
        String packageName = mElements.getPackageOf(mClassElement).getQualifiedName().toString();
        ClassName className = ClassName.get(mClassElement);
        //View v = View.inflate(cp.providerContext(),layout,null);
        // cp.mBinding1 = DataBindingUtil.bind(v);
        MethodSpec build = MethodSpec.methodBuilder("find")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(className, "cp")
                .addParameter(TypeName.INT, "layout")
                .addStatement("$T v = $T.inflate($N.providerContext(),$N,null)",TypeUtil.ANDROID_VIEW,TypeUtil.ANDROID_VIEW,"cp","layout")
                .addStatement("$N.$N = $T.bind($N)", "cp", mElement.getSimpleName(),TypeUtil.BINDINGUTIL, "v")
                .build();
        TypeSpec layoutFinder = TypeSpec.classBuilder(mClassElement.getSimpleName() + "$$Finder")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ParameterizedTypeName.get(TypeUtil.BASEFINDER,className))
                .addMethod(build)
                .build();
        return JavaFile.builder(packageName, layoutFinder).build();
    }
}