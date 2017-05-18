package com.suhang.layoutfindercompiler.dagger;

import com.google.auto.service.AutoService;

import com.squareup.javapoet.ClassName;
import com.suhang.layoutfinderannotation.dagger.FindComponent;
import com.suhang.layoutfinderannotation.dagger.GenComponent;
import com.suhang.layoutfinderannotation.dagger.GenInject;
import com.suhang.layoutfinderannotation.dagger.GenSub;
import com.suhang.layoutfinderannotation.dagger.GenSubComponent;

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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by 苏杭 on 2017/5/17 16:31.
 */
@AutoService(Processor.class)
public class ComponentProcessor extends AbstractProcessor {
    Filer mFiler;
    Elements mElements;
    Messager mMessager;
    private Map<String, ComponentClass> mComponentClassMap = new HashMap<>();
    private Map<String, SubComponentClass> mSubComponentClassMap = new HashMap<>();
    private Types mTypeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElements = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
        mTypeUtils = processingEnvironment.getTypeUtils();
    }

    private void findComponent(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(GenComponent.class)) {
            GenComponent annotation = element.getAnnotation(GenComponent.class);
            ComponentParam param = new ComponentParam();
            List<ClassName> elements = new ArrayList<>();
            try {
                annotation.modules();
            } catch (MirroredTypesException e) {
                TypeMirror mirror = e.getTypeMirrors().get(0);
                TypeElement typeElement = (TypeElement) mTypeUtils.asElement(mirror);
                elements.add(ClassName.get(typeElement));
            }
            TypeElement scope = null;
            try {
                annotation.scope();
            } catch (MirroredTypesException e) {
                TypeMirror mirror = e.getTypeMirrors().get(0);
                scope = (TypeElement) mTypeUtils.asElement(mirror);
            }
            param.mModules = elements;
            param.name = annotation.name();
            param.packname = mElements.getPackageOf(element).getQualifiedName().toString();
            if (scope != null) {
                param.scope = ClassName.get(scope);
            }
            param.mSubcomponents = getSub(element);
            param.mInjects = findInject(element);
            mComponentClassMap.put(element.getSimpleName().toString(), new ComponentClass(mMessager, param));
        }
    }

    /**
     * 根据GenSub注解,查找需要提供的子Component的各个元素
     */
    private Map<ClassName, List<ClassName>> getSub(Element element) {
        Map<ClassName, List<ClassName>> maps = new HashMap<>();
        for (Element ele : element.getEnclosedElements()) {
            List<ClassName> ls = new ArrayList<>();
            GenSub annotation = ele.getAnnotation(GenSub.class);
            if (annotation != null) {
                ExecutableElement e = (ExecutableElement) ele;
                DeclaredType type = (DeclaredType) e.getReturnType();
                ClassName className = ClassName.get((TypeElement) type.asElement());
                if (className.packageName().equals("")) {
                    ClassName name = ClassName.get(mElements.getPackageOf(element).getQualifiedName().toString(), className.simpleName());
                    for (VariableElement variableElement : e.getParameters()) {
                        DeclaredType module = (DeclaredType) variableElement.asType();
                        ClassName c = ClassName.get((TypeElement) module.asElement());
                        ls.add(c);
                    }
                    maps.put(name, ls);
                } else {
                    for (VariableElement variableElement : e.getParameters()) {
                        DeclaredType module = (DeclaredType) variableElement.asType();
                        ClassName c = ClassName.get((TypeElement) module.asElement());
                        ls.add(c);
                    }
                    maps.put(className, ls);
                }
            }
        }
        return maps;
    }


    public class ComponentParam {
        String name;
        String packname;
        ClassName scope;
        List<ClassName> mModules;
        Map<ClassName, List<ClassName>> mSubcomponents;
        List<ClassName> mInjects;
    }


    private void findSubComponent(RoundEnvironment roundEnvironment) {
        for (Element element : roundEnvironment.getElementsAnnotatedWith(GenSubComponent.class)) {
            GenSubComponent annotation = element.getAnnotation(GenSubComponent.class);
            ComponentParam param = new ComponentParam();
            List<ClassName> elements = new ArrayList<>();
            try {
                annotation.modules();
            } catch (MirroredTypesException e) {
                TypeMirror mirror = e.getTypeMirrors().get(0);
                TypeElement typeElement = (TypeElement) mTypeUtils.asElement(mirror);
                elements.add(ClassName.get(typeElement));
            }
            TypeElement scope = null;
            try {
                annotation.scope();
            } catch (MirroredTypesException e) {
                TypeMirror mirror = e.getTypeMirrors().get(0);
                scope = (TypeElement) mTypeUtils.asElement(mirror);
            }
            param.mModules = elements;
            param.name = annotation.name();
            param.packname = mElements.getPackageOf(element).getQualifiedName().toString();
            if (scope != null) {
                param.scope = ClassName.get(scope);
            }
            param.mSubcomponents = getSub(element);
            param.mInjects = findInject(element);
            mSubComponentClassMap.put(element.getSimpleName().toString(), new SubComponentClass(mMessager, param));
        }
    }

    private List<ClassName> findInject(Element element) {
        List<ClassName> ls = new ArrayList<>();
        for (Element ele : element.getEnclosedElements()) {
            GenInject annotation = ele.getAnnotation(GenInject.class);
            if (annotation != null) {
                ExecutableElement e = (ExecutableElement) ele;
                for (VariableElement variableElement : e.getParameters()) {
                    DeclaredType module = (DeclaredType) variableElement.asType();
                    ClassName c = ClassName.get((TypeElement) module.asElement());
                    ls.add(c);
                }
            }
        }
        return ls;
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            mComponentClassMap.clear();
            mSubComponentClassMap.clear();
            findComponent(roundEnvironment);
            for (ComponentClass componentClass : mComponentClassMap.values()) {
                componentClass.gen().writeTo(mFiler);
            }
            findSubComponent(roundEnvironment);
            for (SubComponentClass subComponentClass : mSubComponentClassMap.values()) {
                subComponentClass.gen().writeTo(mFiler);
            }
        } catch (Exception e) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(GenSubComponent.class.getCanonicalName());
        types.add(GenComponent.class.getCanonicalName());
        types.add(FindComponent.class.getCanonicalName());
        types.add(GenSub.class.getCanonicalName());
        types.add(GenInject.class.getCanonicalName());
        return types;
    }
}
