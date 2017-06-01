package com.suhang.layoutfindercompiler;

import com.google.auto.service.AutoService;

import com.suhang.layoutfinderannotation.Field;
import com.suhang.layoutfinderannotation.SharedPreferences;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class SharedPreferencesProcessor extends AbstractProcessor {
	private Map<String, SharedPreferencesClass> mSharedPreferencesClassMap = new HashMap<>();
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
		types.add(SharedPreferences.class.getCanonicalName());
		types.add(Field.class.getCanonicalName());
		return types;
	}

	private void processSharedPreferences(RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getElementsAnnotatedWith(SharedPreferences.class)) {
			TypeElement typeElement = (TypeElement) element;
			SharedPreferences preferences = element.getAnnotation(SharedPreferences.class);
			String name = preferences.value();
			getParams(typeElement, name);
		}
	}

	private void getParams(TypeElement typeElement ,String name) {
		String fullClassName = typeElement.getQualifiedName().toString();
		Map<String, String> params = new HashMap<>();
		String packname = mElements.getPackageOf(typeElement).getQualifiedName().toString();
		for (Element element : typeElement.getEnclosedElements()) {
			String fieldName = element.getSimpleName().toString();
			Field field = element.getAnnotation(Field.class);
			if (field != null) {
				fieldName = field.value();
			}
			if (element instanceof VariableElement) {
				VariableElement variableElement = (VariableElement) element;
				String type = variableElement.asType().toString();
				params.put(fieldName, type);
			}
		}
		mSharedPreferencesClassMap.put(fullClassName, new SharedPreferencesClass(mElements,packname, typeElement.getSimpleName().toString()+"Helper",name, params,mMessager));
	}


	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		try {
			mSharedPreferencesClassMap.clear();
			processSharedPreferences(roundEnvironment);
			for (SharedPreferencesClass preferencesClass : mSharedPreferencesClassMap.values()) {
				preferencesClass.gen().writeTo(mFiler);
			}
		} catch (Exception e) {
			mMessager.printMessage(Diagnostic.Kind.ERROR, "出异常了" + e);
			return true;
		}
		return true;
	}
}
