package com.suhang.layoutfindercompiler;

import com.squareup.javapoet.ClassName;

public class TypeUtil {
    public static final ClassName ANDROID_VIEW = ClassName.get("android.view", "View");
    public static final ClassName BASEFINDER = ClassName.get("com.suhang.layoutfinder", "BaseFinder");
    public static final ClassName BASEMETHODFINDER = ClassName.get("com.suhang.layoutfinder", "BaseMethodFinder");
    public static final ClassName BINDINGUTIL = ClassName.get("android.databinding", "DataBindingUtil");
    public static final ClassName POST = ClassName.get("retrofit2.http", "POST");
    public static final ClassName GET = ClassName.get("retrofit2.http", "GET");
    public static final ClassName FLOWABLE = ClassName.get("io.reactivex", "Flowable");
    public static final ClassName LOG = ClassName.get("android.util", "Log");
}