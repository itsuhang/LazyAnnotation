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
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * 处理{@link com.suhang.layoutfinderannotation.ToString}注解,生成返回String类型的retrofit方法
 */
@AutoService(Processor.class)
public class ToStringProcessor extends AbstractProcessor {
	public static final String CLASSNAME = "StringRetrofitService";
	public static final String HELPCLASS = "RetrofitHelper";
	private List<ExecutableElement> ls = new ArrayList<>();
	private Map<String, Param> urls = new HashMap<>();
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
		urls.clear();
		for (Element element : roundEnv.getElementsAnnotatedWith(ToString.class)) {
			ExecutableElement executableElement = (ExecutableElement) element;
			ToString annotation = executableElement.getAnnotation(ToString.class);
			String value = annotation.value();
			for (AnnotationMirror annotationMirror : executableElement.getAnnotationMirrors()) {
				TypeName typeName = ClassName.get(annotationMirror.getAnnotationType());
				if (typeName.equals(TypeUtil.POST) || typeName.equals(TypeUtil.GET)) {
					for (AnnotationValue annotationValue : annotationMirror.getElementValues().values()) {
						Param param = new Param();
						param.host = value;
						param.methodName = executableElement.getSimpleName().toString();
						List<TypeName> ls = new ArrayList<>();
						for (VariableElement variableElement : executableElement.getParameters()) {
							ls.add(TypeName.get(variableElement.asType()));
						}
						param.params = ls;
						urls.put(annotationValue.getValue().toString(), param);
					}
				}
			}
			ls.add(executableElement);
			packname = mElements.getPackageOf(element).getQualifiedName().toString();
		}
		TypeSpec.Builder classBuilder = TypeSpec.interfaceBuilder(CLASSNAME)
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

	private class Param {
		String host;
		String methodName;
		List<TypeName> params;
	}

	private JavaFile getHelper() {
		TypeSpec.Builder builder = TypeSpec.classBuilder(HELPCLASS).addModifiers(Modifier.PUBLIC);
		builder.addMethod(createSearch());
		return JavaFile.builder(packname, builder.build()).build();
	}


	private MethodSpec createSearch() {
		ClassName service = ClassName.get(packname, CLASSNAME);
		MethodSpec.Builder find = MethodSpec.methodBuilder("find").addModifiers(Modifier.STATIC, Modifier.PUBLIC)
				.addParameter(TypeName.get(String.class), "url")
				.addParameter(TypeName.get(Object[].class), "objects")
				.returns(TypeUtil.FLOWABLE);
		find.addCode("try{\n");
		for (Map.Entry<String, Param> stringStringEntry : urls.entrySet()) {
			String url = stringStringEntry.getKey();
			String host = stringStringEntry.getValue().host;
			String methodName = stringStringEntry.getValue().methodName;
			List<TypeName> params = stringStringEntry.getValue().params;
			find.beginControlFlow("if($N.equals($S))", "url", url);
			find.addCode("$T retrofit = new $T.Builder().baseUrl($S).addCallAdapterFactory($T.create()).addConverterFactory($T.create()).build();\n", TypeUtil.RETROFIT, TypeUtil.RETROFIT, host, TypeUtil.RXJAVA_ADAPTER, TypeUtil.STRING_CONVERT);
			find.addCode("$T fl = $N.create($T.class).$N(", TypeUtil.FLOWABLE, "retrofit", service, methodName);
			int size = params.size();
			if (size == 0) {
				find.addCode(");\n");
			}
			for (int i = 0; i < size; i++) {
				TypeName name = params.get(i);
				if (size == 1) {
					find.addCode("($T)$N[$L]);\n", name, "objects", i);
				} else if (i == size - 1) {
					find.addCode("($T)$N[$L]);\n", name, "objects", i);
				} else {
					find.addCode("($T)$N[$L],", name, "objects", i);
				}
			}
			find.addStatement("return $N", "fl");
			find.endControlFlow();
		}


		find.addCode("}catch($T e){\nthrow new $T($S+$N.toString());\n}\n", TypeName.get(Exception.class), TypeName.get(RuntimeException.class), "MethodFinder.find()方法参数数量与Service中方法参数数量不一致,或参数顺序不正确", "e");
		find.addStatement("return null");
		return find.build();
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		try {
			if (!isCreate) {
				processToString(roundEnvironment).writeTo(mFiler);
				getHelper().writeTo(mFiler);
			}
		} catch (Exception e) {
			mMessager.printMessage(Diagnostic.Kind.ERROR, "出异常了" + e.getMessage());
			return true;
		}
		return true;
	}
}
