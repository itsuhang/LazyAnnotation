package com.suhang.layoutfindercompiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.suhang.layoutfinderannotation.GenParentComponent;
import com.suhang.layoutfinderannotation.GenRootComponent;
import com.suhang.layoutfinderannotation.GenSubComponent;

import java.io.IOException;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * Created by 苏杭 on 2017/6/12 22:03.
 */
@AutoService(Processor.class)
public class DaggerFinderProcessor extends AbstractProcessor {
	Filer mFiler;
	Elements mElements;
	Messager mMessager;
	private Types mTypeUtils;
	private Map<String, JavaFile> mMap = new HashMap<>();
	private Map<Integer, List<Param>> groups = new HashMap<>();

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
		types.add(GenSubComponent.class.getCanonicalName());
		return types;
	}

	private boolean isSubType(Element e, Element parent) {
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


	/**
	 * 解析注解
	 *
	 * @param roundEnv
	 * @return
	 */
	private void processDagger(RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getElementsAnnotatedWith(GenSubComponent.class)) {
			String packname = mElements.getPackageOf(element).getQualifiedName().toString();
			TypeElement typeElement = (TypeElement) element;
			String className = typeElement.getSimpleName().toString() + "Component";
			GenSubComponent genSubComponent = element.getAnnotation(GenSubComponent.class);
			int tag = genSubComponent.tag();
			TypeElement scope = getScope(genSubComponent);
			List<TypeName> modules = getModules(genSubComponent);
			List<Param> params = groups.get(tag);
			if (params == null) {
				params = new ArrayList<>();
			}
			Param param = new Param(scope, modules, packname, className, typeElement, Param.CHILD, null);
			params.add(param);
			groups.put(tag, params);
		}
	}

	private void processDaggerParent(RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getElementsAnnotatedWith(GenParentComponent.class)) {
			String packname = mElements.getPackageOf(element).getQualifiedName().toString();
			TypeElement typeElement = (TypeElement) element;
			String className = typeElement.getSimpleName().toString() + "Component";
			GenParentComponent genSubComponent = element.getAnnotation(GenParentComponent.class);
			int tag = genSubComponent.tag();
			TypeElement scope = getScope(genSubComponent);
			List<TypeName> modules = getModules(genSubComponent);
			List<Param> params = groups.get(tag);
			if (params == null) {
				params = new ArrayList<>();
			}
			Param param = new Param(scope, modules, packname, className, typeElement, Param.PARENT, new HashMap<String, ClassName>());
			params.add(param);
			groups.put(tag, params);
		}
	}

	private void processDaggerRoot(RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getElementsAnnotatedWith(GenRootComponent.class)) {
			String packname = mElements.getPackageOf(element).getQualifiedName().toString();
			TypeElement typeElement = (TypeElement) element;
			String className = typeElement.getSimpleName().toString() + "Component";
			GenRootComponent genSubComponent = element.getAnnotation(GenRootComponent.class);
			int tag = genSubComponent.tag();
			TypeElement scope = getScope(genSubComponent);
			List<TypeName> modules = getModules(genSubComponent);
			List<Param> params = groups.get(tag);
			if (params == null) {
				params = new ArrayList<>();
			}
			Param param = new Param(scope, modules, packname, className, typeElement, Param.ROOT, new HashMap<String, ClassName>());
			params.add(param);
			groups.put(tag, params);
		}
	}

	/**
	 * 生成dagger子组件
	 *
	 * @param param
	 * @return
	 */
	private void genDaggerChild(Param param) {
		TypeSpec.Builder classBuilder = getClassBuilder(param, false);
		if (!TypeName.get(param.scope.asType()).equals(TypeName.OBJECT)) {
			classBuilder.addAnnotation(ClassName.get(param.scope));
		}
		mMap.put(param.className, JavaFile.builder(param.packname, classBuilder.build()).build());
	}

	/**
	 * 生成dagger父组件
	 *
	 * @param param
	 * @return
	 */
	private void genDaggerParent(Param param) {
		TypeSpec.Builder classBuilder = getClassBuilder(param, false);
		if (!TypeName.get(param.scope.asType()).equals(TypeName.OBJECT)) {
			classBuilder.addAnnotation(ClassName.get(param.scope));
		}
		for (Map.Entry<String, ClassName> entry : param.childs.entrySet()) {
			classBuilder.addMethod(MethodSpec.methodBuilder("provider" + entry.getKey()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
					.returns(entry.getValue()).build());
		}
		mMap.put(param.className, JavaFile.builder(param.packname, classBuilder.build()).build());
	}

	/**
	 * 生成dagger父组件
	 *
	 * @param param
	 * @return
	 */
	private void genDaggerRoot(Param param) {
		TypeSpec.Builder classBuilder = getClassBuilder(param, true);
		if (!TypeName.get(param.scope.asType()).equals(TypeName.OBJECT)) {
			classBuilder.addAnnotation(ClassName.get(param.scope));
		}
		for (Map.Entry<String, ClassName> entry : param.childs.entrySet()) {
			classBuilder.addMethod(MethodSpec.methodBuilder("provider" + entry.getKey()).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
					.returns(entry.getValue()).build());
		}
		mMap.put(param.className, JavaFile.builder(param.packname, classBuilder.build()).build());
	}

	private TypeSpec.Builder getClassBuilder(Param param, boolean isRoot) {
		List<MethodSpec> methods = new ArrayList<>();
		AnnotationSpec.Builder subComponentBuilder = AnnotationSpec.builder(isRoot ? TypeUtil.COMPONENT : TypeUtil.SUBCOMPONENT);
		CodeBlock.Builder codeBlockBuilder = CodeBlock.builder();
		if (param.modules.size() == 1) {
			codeBlockBuilder = codeBlockBuilder.add("$T.class", param.modules.get(0));
			methods.add(MethodSpec.methodBuilder("setModule").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(param.modules.get(0), "module").returns(ClassName.get(param.packname + "." + param.className, "Builder")).build());
		} else {
			for (int i = 0; i < param.modules.size(); i++) {
				TypeName typeName = param.modules.get(i);
				methods.add(MethodSpec.methodBuilder("setModule").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).addParameter(param.modules.get(i), "module").returns(ClassName.get(param.packname + "." + param.className, "Builder")).build());
				if (i == param.modules.size() - 1) {
					codeBlockBuilder.add("$T.class}", typeName);
				} else if (i == 0) {
					codeBlockBuilder.add("{$T.class,", typeName);
				} else {
					codeBlockBuilder.add("$T.class,", typeName);
				}
			}
		}
		AnnotationSpec subComponent = subComponentBuilder.addMember("modules", codeBlockBuilder.build()).build();
		MethodSpec buildSpec = MethodSpec.methodBuilder("build").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(ClassName.get(param.packname, param.className)).build();
		TypeSpec.Builder builder = TypeSpec.interfaceBuilder("Builder").addAnnotation(isRoot ? TypeUtil.COMPONENTP_BUILDER : TypeUtil.SUBCOMPONENTP_BUILDER).addModifiers(Modifier.STATIC, Modifier.ABSTRACT, Modifier.PUBLIC).addMethod(buildSpec);
		for (MethodSpec method : methods) {
			builder.addMethod(method);
		}
		return TypeSpec.interfaceBuilder(param.className).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(subComponent)
				.addType(builder.build())
				.addSuperinterface(ParameterizedTypeName.get(TypeUtil.MEMBERSINJECTOR, ClassName.get(param.element)));
	}


	private void getGroup() {
		for (List<Param> params : groups.values()) {
			List<Param> child = new ArrayList<>();
			List<Param> parent = new ArrayList<>();
			List<Param> root = new ArrayList<>();
			for (Param param : params) {
				switch (param.type) {
					case Param.CHILD:
						child.add(param);
						break;
					case Param.PARENT:
						parent.add(param);
						break;
					case Param.ROOT:
						root.add(param);
						break;
				}
			}
			if (root.size() > 1) {
				mMessager.printMessage(Diagnostic.Kind.ERROR, "同一个Tag类型只能有一个Root组件");
			} else if (root.size() == 1 && parent.size() == 1) {
				root.get(0).childs = getClassName(parent);
			} else if (root.size() == 1 && parent.size() == 0) {
				root.get(0).childs = getClassName(child);
			}

			if (parent.size() > 1) {
				mMessager.printMessage(Diagnostic.Kind.ERROR, "同一个Tag类型只能有一个父组件");
			} else if (parent.size() == 1) {
				parent.get(0).childs = getClassName(child);
			}

			for (Param param : child) {
				genDaggerChild(param);
			}

			for (Param param : parent) {
				genDaggerParent(param);
			}

			for (Param param : root) {
				genDaggerRoot(param);
			}
		}
	}

	private Map<String, ClassName> getClassName(List<Param> params) {
		Map<String, ClassName> ls = new HashMap<>();
		for (Param param : params) {
			ls.put(param.className, ClassName.get(param.packname,param.className,"Builder"));
		}
		return ls;
	}

	class Param {
		public static final int ROOT = 100;
		public static final int PARENT = 101;
		public static final int CHILD = 102;

		Param(TypeElement scope, List<TypeName> modules, String packname, String className, TypeElement element, int type, Map<String, ClassName> childs) {
			this.scope = scope;
			this.modules = modules;
			this.packname = packname;
			this.className = className;
			this.element = element;
			this.type = type;
			this.childs = childs;
		}

		int type;
		TypeElement scope;
		List<TypeName> modules;
		String packname;
		String className;
		TypeElement element;
		Map<String, ClassName> childs;
	}


	/**
	 * 获取域
	 *
	 * @param genSubComponent
	 * @return
	 */
	private TypeElement getScope(GenSubComponent genSubComponent) {
		try {
			genSubComponent.scope();
		} catch (MirroredTypeException e) {
			return (TypeElement) mTypeUtils.asElement(e.getTypeMirror());
		}
		return null;
	}

	/**
	 * 获取Module
	 *
	 * @param genSubComponent
	 * @return
	 */
	private List<TypeName> getModules(GenSubComponent genSubComponent) {
		try {
			genSubComponent.modules();
		} catch (MirroredTypesException e) {
			List<TypeName> names = new ArrayList<>();
			for (TypeMirror typeMirror : e.getTypeMirrors()) {
				names.add(TypeName.get(typeMirror));
			}
			return names;
		}
		return null;
	}

	/**
	 * 获取域
	 *
	 * @param genParentComponent
	 * @return
	 */
	private TypeElement getScope(GenParentComponent genParentComponent) {
		try {
			genParentComponent.scope();
		} catch (MirroredTypeException e) {
			return (TypeElement) mTypeUtils.asElement(e.getTypeMirror());
		}
		return null;
	}

	/**
	 * 获取Module
	 *
	 * @param genParentComponent
	 * @return
	 */
	private List<TypeName> getModules(GenParentComponent genParentComponent) {
		try {
			genParentComponent.modules();
		} catch (MirroredTypesException e) {
			List<TypeName> names = new ArrayList<>();
			for (TypeMirror typeMirror : e.getTypeMirrors()) {
				names.add(TypeName.get(typeMirror));
			}
			return names;
		}
		return null;
	}

	/**
	 * 获取域
	 *
	 * @return
	 */
	private TypeElement getScope(GenRootComponent genRootComponent) {
		try {
			genRootComponent.scope();
		} catch (MirroredTypeException e) {
			return (TypeElement) mTypeUtils.asElement(e.getTypeMirror());
		}
		return null;
	}

	/**
	 * 获取Module
	 *
	 * @param genRootComponent
	 * @return
	 */
	private List<TypeName> getModules(GenRootComponent genRootComponent) {
		try {
			genRootComponent.modules();
		} catch (MirroredTypesException e) {
			List<TypeName> names = new ArrayList<>();
			for (TypeMirror typeMirror : e.getTypeMirrors()) {
				names.add(TypeName.get(typeMirror));
			}
			return names;
		}
		return null;
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		mMap.clear();
		groups.clear();
		processDagger(roundEnv);
		processDaggerParent(roundEnv);
		processDaggerRoot(roundEnv);
		getGroup();
		try {
			for (JavaFile javaFile : mMap.values()) {
				javaFile.writeTo(mFiler);
			}
		} catch (IOException e) {
			mMessager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
		}
		return true;
	}
}
