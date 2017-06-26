package com.suhang.layoutfindercompiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;

public class TypeUtil {
	public static final ClassName ANDROID_VIEW = ClassName.get("android.view", "View");
	public static final ClassName BASEFINDER = ClassName.get("com.suhang.layoutfinder", "BaseFinder");
	public static final ClassName CONTEXT = ClassName.get("android.content", "Context");
	public static final ClassName BASEMETHODFINDER = ClassName.get("com.suhang.layoutfinder", "BaseMethodFinder");
	public static final ClassName SP = ClassName.get("com.suhang.layoutfinder", "SharedPreferencesFinder");
	public static final ClassName BINDINGUTIL = ClassName.get("android.databinding", "DataBindingUtil");
	public static final ClassName POST = ClassName.get("retrofit2.http", "POST");
	public static final ClassName GET = ClassName.get("retrofit2.http", "GET");
	public static final ClassName FLOWABLE = ClassName.get("io.reactivex", "Flowable");
	public static final ClassName RETROFIT = ClassName.get("retrofit2", "Retrofit");
	public static final ClassName RXJAVA_ADAPTER = ClassName.get("retrofit2.adapter.rxjava2", "RxJava2CallAdapterFactory");
	public static final ClassName STRING_CONVERT = ClassName.get("retrofit2.converter.scalars", "ScalarsConverterFactory");
	public static final ClassName SHAREPREFERENCES = ClassName.get("android.content", "SharedPreferences");
	public static final ClassName COMPONENT = ClassName.get("dagger", "Component");
	public static final ClassName SUBCOMPONENT = ClassName.get("dagger", "Subcomponent");
	public static final ClassName SUBCOMPONENTP_BUILDER = ClassName.get("dagger", "Subcomponent.Builder");
	public static final ClassName MEMBERSINJECTOR = ClassName.get("dagger", "MembersInjector");

	public static Map<String,TypeName> baseType = genBaseType();

	private static Map<String,TypeName> genBaseType() {
		Map<String, TypeName> baseType = new HashMap<>();
		baseType.put(String.class.getCanonicalName(), TypeName.get(String.class));
		baseType.put("int",TypeName.INT);
		baseType.put("float",TypeName.FLOAT);
		baseType.put("long",TypeName.LONG);
		baseType.put("boolean",TypeName.BOOLEAN);
		return baseType;
	}
}