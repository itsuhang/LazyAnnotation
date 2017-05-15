package com.suhang.layoutfindercompiler;

import com.google.auto.service.AutoService;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.suhang.layoutfinderannotation.BindLayout;

import java.util.HashMap;
import java.util.LinkedHashSet;
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
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class LayoutFinderProcessor extends AbstractProcessor {
    private Map<String, LayoutClass> mLayoutClassMap = new HashMap<>();
    Filer mFiler;
    Elements mElements;
    Messager mMessager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElements = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(BindLayout.class.getCanonicalName());
        return types;
    }

    private void processBindView(RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(BindLayout.class)) {
            //得到该注解所在的类(若为抽象类则不生成Finder,而是查找该抽象类的子类)
            TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
            if (!enclosingElement.getModifiers().contains(Modifier.ABSTRACT)) {
                getLayoutClass(enclosingElement, element);
            } else {
                //查找所有的根元素
                for (Element e : roundEnv.getRootElements()) {
                    if (e instanceof TypeElement) {
                        TypeElement typeElement = (TypeElement) e;
                        TypeMirror mirror = typeElement.getSuperclass();
                        if (mirror instanceof DeclaredType) {
                            DeclaredType superclass = (DeclaredType) mirror;
                            //判断该元素的父类是否为enclosingElement
                            if (superclass.asElement().equals(enclosingElement)) {
                                getLayoutClass(typeElement, element);
                            }
                        }
                    }
                }
            }
        }
    }

    private void getLayoutClass(TypeElement parent, Element element) {
        String fullClassName = parent.getQualifiedName().toString();
        LayoutClass annotatedClass = mLayoutClassMap.get(fullClassName);
        if (annotatedClass == null) {
            annotatedClass = new LayoutClass(parent, mElements, element);
            mLayoutClassMap.put(fullClassName, annotatedClass);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            mLayoutClassMap.clear();
            processBindView(roundEnvironment);
            for (LayoutClass layoutClass : mLayoutClassMap.values()) {
                layoutClass.gen().writeTo(mFiler);

            }
        } catch (Exception e) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "出异常了" + e.getMessage());
            return true;
        }
        return true;
    }
}
