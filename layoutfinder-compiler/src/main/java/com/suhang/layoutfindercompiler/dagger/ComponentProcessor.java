package com.suhang.layoutfindercompiler.dagger;

import com.google.auto.service.AutoService;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
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
    private Map<String, String> mPacknames = new HashMap<>();

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
            mPacknames.put(annotation.name(), mElements.getPackageOf(element).getQualifiedName().toString());
            param.packname = mElements.getPackageOf(element).getQualifiedName().toString();
            if (scope != null) {
                param.scope = ClassName.get(scope);
            }
//            param.mInjects = findInject(element);
            mComponentClassMap.put(element.getSimpleName().toString(), new ComponentClass(mMessager, param));
        }
    }


    public class ComponentParam {
        String name;
        String packname;
        ClassName scope;
        List<ClassName> mModules;
        Map<ClassName, List<ClassName>> mSubcomponents;
        List<ClassName> mInjects;

        @Override
        public String toString() {
            return "ComponentParam{" +
                    "name='" + name + '\'' +
                    ", packname='" + packname + '\'' +
                    ", scope=" + scope +
                    ", mModules=" + mModules +
                    ", mSubcomponents=" + mSubcomponents +
                    ", mInjects=" + mInjects +
                    '}';
        }
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
            mPacknames.put(annotation.name(), mElements.getPackageOf(element).getQualifiedName().toString());
            param.packname = mElements.getPackageOf(element).getQualifiedName().toString();
            if (scope != null) {
                param.scope = ClassName.get(scope);
            }
//            param.mInjects = findInject(element);
            mSubComponentClassMap.put(element.getSimpleName().toString(), new SubComponentClass(mMessager, param));
        }
    }
//
//    private List<ClassName> findInject(Element element) {
//        List<ClassName> ls = new ArrayList<>();
//        for (Element ele : element.getEnclosedElements()) {
//            GenInject annotation = ele.getAnnotation(GenInject.class);
//            if (annotation != null) {
//                ExecutableElement e = (ExecutableElement) ele;
//                for (VariableElement variableElement : e.getParameters()) {
//                    DeclaredType module = (DeclaredType) variableElement.asType();
//                    ClassName c = ClassName.get((TypeElement) module.asElement());
//                    ls.add(c);
//                }
//            }
//        }
//        return ls;
//    }

    private Map<String, List<ClassName>> findInject(RoundEnvironment roundEnvironment) {
        Map<String, List<ClassName>> maps = new HashMap<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(GenInject.class)) {
            ExecutableElement ee = (ExecutableElement) element;
            GenInject annotation = element.getAnnotation(GenInject.class);
            String component = annotation.component();
            List<ClassName> ls = maps.get(component);
            if (ls == null) {
                ls = new ArrayList<>();
            }
            for (VariableElement variableElement : ee.getParameters()) {
                DeclaredType type = (DeclaredType) variableElement.asType();
                ClassName name = ClassName.get((TypeElement) type.asElement());
                ls.add(name);
            }
            maps.put(component, ls);
        }
        return maps;
    }

    private Map<String, List<SubParam>> subComponent(RoundEnvironment roundEnvironment) {
        Map<String, List<SubParam>> maps = new HashMap<>();
        for (Element element : roundEnvironment.getElementsAnnotatedWith(GenSub.class)) {
            ExecutableElement ee = (ExecutableElement) element;
            GenSub annotation = element.getAnnotation(GenSub.class);
            String component = annotation.component();
            List<SubParam> subParams = maps.get(component);
            if (subParams == null) {
                subParams = new ArrayList<>();
            }
            SubParam subParam = new SubParam();
            List<ClassName> ls = new ArrayList<>();
            for (VariableElement variableElement : ee.getParameters()) {
                DeclaredType type = (DeclaredType) variableElement.asType();
                ClassName name = ClassName.get((TypeElement) type.asElement());
                ls.add(name);
            }
            subParam.params = ls;
            DeclaredType returnType = (DeclaredType) ee.getReturnType();
            ClassName returnName = ClassName.get((TypeElement) returnType.asElement());
            String re = mPacknames.get(returnName.simpleName());
            if (re != null) {
                ClassName res = ClassName.get(re, returnName.simpleName());
                subParam.retureName = res;
            } else {
                subParam.retureName = returnName;
            }
            subParams.add(subParam);
            maps.put(component, subParams);
        }
        return maps;

    }

    class SubParam {
        ClassName retureName;
        List<ClassName> params;

        @Override
        public String toString() {
            return "SubParam{" +
                    "retureName=" + retureName +
                    ", params=" + params +
                    '}';
        }
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            mComponentClassMap.clear();
            mSubComponentClassMap.clear();
            findComponent(roundEnvironment);
            findSubComponent(roundEnvironment);
            Map<String, List<ClassName>> inject = findInject(roundEnvironment);
//            mMessager.printMessage(Diagnostic.Kind.ERROR, " " + inject.toString());
            Map<String, List<SubParam>> subParamMap = subComponent(roundEnvironment);
            for (ComponentClass componentClass : mComponentClassMap.values()) {
                List<SubParam> subParams = subParamMap.get(componentClass.getParam().name);
                List<ClassName> classNames = inject.get(componentClass.getParam().name);
                if (classNames != null) {
                    componentClass.getParam().mInjects = classNames;
                }
//                mMessager.printMessage(Diagnostic.Kind.ERROR, " " + subParams);
                if (subParams != null) {
                    Map<ClassName, List<ClassName>> maps = new HashMap<>();
                    for (SubParam subParam : subParams) {
                        maps.put(subParam.retureName, subParam.params);
                    }
                    componentClass.getParam().mSubcomponents = maps;
                }
                componentClass.gen().writeTo(mFiler);
            }
            for (SubComponentClass subComponentClass : mSubComponentClassMap.values()) {
                List<SubParam> subParams = subParamMap.get(subComponentClass.getParam().name);
                List<ClassName> classNames = inject.get(subComponentClass.getParam().name);
                if (classNames != null) {
                    subComponentClass.getParam().mInjects = classNames;
                }
                if (subParams != null) {
                    Map<ClassName, List<ClassName>> maps = new HashMap<>();
                    for (SubParam subParam : subParams) {
                        maps.put(subParam.retureName, subParam.params);
                    }
                    subComponentClass.getParam().mSubcomponents = maps;
                }
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
