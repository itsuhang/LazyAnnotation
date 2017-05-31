package com.suhang.layoutfindercompiler;

import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

public class TypeUtil {
    public static final ClassName ANDROID_VIEW = ClassName.get("android.view", "View");
    public static final ClassName BASEFINDER = ClassName.get("com.suhang.layoutfinder", "BaseFinder");
    public static final ClassName BASEMETHODFINDER = ClassName.get("com.suhang.layoutfinder", "BaseMethodFinder");
    public static final ClassName BINDINGUTIL = ClassName.get("android.databinding", "DataBindingUtil");
    public static final ClassName POST = ClassName.get("retrofit2.http", "POST");
    public static final ClassName GET = ClassName.get("retrofit2.http", "GET");
    public static final ClassName FLOWABLE = ClassName.get("io.reactivex", "Flowable");

    public static ArrayList<String> baseType = genBaseType();

    private static ArrayList<String> genBaseType() {
        ArrayList<String> baseType = new ArrayList<>();
        baseType.add(String.class.getCanonicalName());
        baseType.add("int");
        baseType.add("short");
        baseType.add("float");
        baseType.add("double");
        baseType.add("long");
        baseType.add("boolean");
        return baseType;
    }
}