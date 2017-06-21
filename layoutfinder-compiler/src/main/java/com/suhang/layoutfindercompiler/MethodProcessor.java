package com.suhang.layoutfindercompiler;

import com.google.auto.service.AutoService;

import com.suhang.layoutfinderannotation.DaggerFinder;
import com.suhang.layoutfinderannotation.FindMethod;

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
public class MethodProcessor extends AbstractProcessor {
    private Map<String, MethodClass> mMethodClassMap = new HashMap<>();
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
        types.add(FindMethod.class.getCanonicalName());
        return types;
    }


    private void getMethod(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(FindMethod.class)) {
            TypeElement e = (TypeElement) element;
            String s = e.getQualifiedName().toString();
            MethodClass methodClass = mMethodClassMap.get(s);
            if (methodClass == null) {
                mMethodClassMap.put(s, new MethodClass(mElements, element));
            }
        }
    }

    private boolean isSubType(Element e,Element parent) {
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

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            mMethodClassMap.clear();
            getMethod(roundEnvironment);
            for (MethodClass methodClass : mMethodClassMap.values()) {
                methodClass.gen().writeTo(mFiler);
            }
        } catch (Exception e) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "出异常了" + e);
            return true;
        }
        return true;
    }
}
