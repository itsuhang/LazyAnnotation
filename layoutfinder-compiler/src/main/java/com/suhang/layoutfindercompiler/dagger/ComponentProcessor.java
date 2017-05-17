package com.suhang.layoutfindercompiler.dagger;

import com.google.auto.service.AutoService;

import com.squareup.javapoet.ClassName;
import com.suhang.layoutfinderannotation.dagger.GenComponent;
import com.suhang.layoutfinderannotation.dagger.GenInject;
import com.suhang.layoutfinderannotation.dagger.GenSubComponent;
import com.suhang.layoutfinderannotation.dagger.Keys;
import com.suhang.layoutfindercompiler.TypeUtil;

import java.util.Arrays;
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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
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

	@Override
	public synchronized void init(ProcessingEnvironment processingEnvironment) {
		super.init(processingEnvironment);
		mFiler = processingEnvironment.getFiler();
		mElements = processingEnvironment.getElementUtils();
		mMessager = processingEnvironment.getMessager();
	}

	private void findComponent(RoundEnvironment roundEnvironment) {
		TypeMirror genType = mElements.getTypeElement(GenComponent.class.getName()).asType();
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GenComponent.class)) {
			for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
				DeclaredType type = mirror.getAnnotationType();
				if (type.equals(genType)) {
					ComponentParam param = new ComponentParam();
					for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
						String key = entry.getKey().getSimpleName().toString();
						if (entry.getValue() != null) {
							String value = entry.getValue().getValue().toString();
							setValue(param, key, value);
						}
					}
					mComponentClassMap.put(element.getSimpleName().toString(), new ComponentClass(mMessager, param));
				}
			}
		}
	}

	private void setValue(ComponentParam param, String key, String value) {
		switch (key) {
			case Keys.NAME:
				int index = value.lastIndexOf(".");
				param.name = value.substring(index + 1);
				param.packname = value.substring(0, index);
				break;
			case Keys.MODULES:
				String replace = value.replace(".class", "");
				String[] split = replace.split(",");
				TypeElement[] elements = new TypeElement[split.length];
				for (int i = 0; i < split.length; i++) {
					TypeElement typeElement = mElements.getTypeElement(split[i]);
					elements[i] = typeElement;
				}
				param.mModules = elements;
				break;
			case Keys.DEPENDENCIES:
				String[] ds = value.split(",");
				TypeElement[] dst = new TypeElement[ds.length];
				for (int i = 0; i < ds.length; i++) {
					TypeElement typeElement = mElements.getTypeElement(ds[i]);
					dst[i] = typeElement;
				}
				param.mDependencies = dst;
				break;
			case Keys.SUBCOMPONENTS:
				String[] ss = value.split(",");
				TypeElement[] sst = new TypeElement[ss.length];
				for (int i = 0; i < ss.length; i++) {
				}
				param.mSubcomponents = sst;
				break;
		}
	}

	public class ComponentParam {
		String name;
		String packname;
		TypeElement[] mModules;
		TypeElement[] mDependencies;
		TypeElement[] mSubcomponents;

		@Override
		public String toString() {
			return "ComponentParam{" +
					"name='" + name + '\'' +
					", mModules=" + Arrays.toString(mModules) +
					", mDependencies=" + Arrays.toString(mDependencies) +
					'}';
		}
	}


	private void findSubComponent(RoundEnvironment roundEnvironment) {
		TypeMirror genType = mElements.getTypeElement(GenSubComponent.class.getName()).asType();
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GenSubComponent.class)) {
		}
	}

	private void findInject(RoundEnvironment roundEnvironment) {
		for (Element element : roundEnvironment.getElementsAnnotatedWith(GenComponent.class)) {

		}
	}


	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		try {
			for (Element element : roundEnvironment.getElementsAnnotatedWith(GenInject.class)) {
				GenInject annotation = element.getAnnotation(GenInject.class);
				mMessager.printMessage(Diagnostic.Kind.WARNING,annotation.name()+"");
			}
			mComponentClassMap.clear();
			findComponent(roundEnvironment);
			for (ComponentClass componentClass : mComponentClassMap.values()) {
				componentClass.gen().writeTo(mFiler);
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
		types.add(GenInject.class.getCanonicalName());
		return types;
	}
}
