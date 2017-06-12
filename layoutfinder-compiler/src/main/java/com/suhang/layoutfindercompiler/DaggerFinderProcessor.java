package com.suhang.layoutfindercompiler;

import com.google.auto.service.AutoService;
import com.suhang.layoutfinderannotation.DaggerFinder;

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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by 苏杭 on 2017/6/12 22:03.
 */
@AutoService(Processor.class)
public class DaggerFinderProcessor extends AbstractProcessor {
	private Map<String, DaggerClass> mLayoutClassMap = new HashMap<>();
	Filer mFiler;
	Elements mElements;
	Messager mMessager;
	private Types mTypeUtils;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		mFiler = processingEnvironment.getFiler();
		mElements = processingEnvironment.getElementUtils();
		mMessager = processingEnvironment.getMessager();
		mTypeUtils = processingEnvironment.getTypeUtils();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> types = new LinkedHashSet<>();
		types.add(DaggerFinder.class.getCanonicalName());
		return types;
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
	private void processDagger(RoundEnvironment roundEnv) {
		Map<TypeElement, List<Element>> map = new HashMap<>();
		for (Element element : roundEnv.getElementsAnnotatedWith(DaggerFinder.class)) {
			TypeElement parentElement = (TypeElement) element.getEnclosingElement();
			List<Element> ls = new ArrayList<>();
			for (Element e : roundEnv.getRootElements()) {
				if (isSubType(e,parentElement)&& !mTypeUtils.isSameType(e.asType(), parentElement.asType())) {
					ls.add(e);
				}
			}
			map.put(parentElement, ls);
		}
		for (Map.Entry<TypeElement, List<Element>> entry : map.entrySet()) {
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		processDagger(roundEnv);
		return true;
	}
}
