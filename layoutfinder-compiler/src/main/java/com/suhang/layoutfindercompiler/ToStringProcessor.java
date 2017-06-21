package com.suhang.layoutfindercompiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.suhang.layoutfinderannotation.ToString;

import java.lang.reflect.ParameterizedType;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 处理{@link com.suhang.layoutfinderannotation.ToString}注解,生成返回String类型的retrofit方法
 */
@AutoService(Processor.class)
public class ToStringProcessor extends AbstractProcessor {
    private List<ExecutableElement> ls = new ArrayList<>();
    boolean isCreate;
    Filer mFiler;
    Elements mElements;
    Messager mMessager;
    Types mTypeUtil;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElements = processingEnvironment.getElementUtils();
        mMessager = processingEnvironment.getMessager();
        mTypeUtil = processingEnvironment.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(ToString.class.getCanonicalName());
        return types;
    }

    private String packname;

    private JavaFile processToString(RoundEnvironment roundEnv) {
        isCreate = true;
        ls.clear();
        for (Element element : roundEnv.getElementsAnnotatedWith(ToString.class)) {
            ls.add((ExecutableElement) element);
            packname = mElements.getPackageOf(element).getQualifiedName().toString();
        }
        TypeSpec.Builder classBuilder = TypeSpec.interfaceBuilder("RetrofitService")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        for (ExecutableElement element : ls) {
            MethodSpec methodSpec = MethodSpec.overriding(element).build();
            List<AnnotationSpec> annotations = methodSpec.annotations;
            List<AnnotationSpec> anno = new ArrayList<>();
            for (AnnotationSpec annotation : annotations) {
                if (!annotation.type.equals(TypeName.get(Override.class)) && !annotation.type.equals(TypeName.get(ToString.class))) {
                    anno.add(annotation);
                }
            }
            String name = methodSpec.name;
            MethodSpec.Builder builder = MethodSpec.methodBuilder(name).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
            for (AnnotationSpec annotationSpec : anno) {
                builder.addAnnotation(annotationSpec);
            }
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(TypeUtil.FLOWABLE, TypeName.get(String.class));
            builder.returns(parameterizedTypeName);
            for (ParameterSpec parameter : methodSpec.parameters) {
                builder.addParameter(parameter);
            }
            classBuilder.addMethod(builder.build());
        }
        return JavaFile.builder(packname, classBuilder.build()).build();
    }

    private void getParams(TypeElement typeElement, String name) {
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        try {
            if (!isCreate) {
                processToString(roundEnvironment).writeTo(mFiler);
            }
        } catch (Exception e) {
            mMessager.printMessage(Diagnostic.Kind.ERROR, "出异常了" + e.getMessage());
            return true;
        }
        return true;
    }
}
