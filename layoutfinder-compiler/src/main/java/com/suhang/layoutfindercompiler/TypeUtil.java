package com.suhang.layoutfindercompiler;

import com.squareup.javapoet.ClassName;

public class TypeUtil {
    public static final ClassName ANDROID_VIEW = ClassName.get("android.view", "View");
    public static final ClassName BASEFINDER = ClassName.get("com.suhang.layoutfinder", "BaseFinder");
    public static final ClassName BINDINGUTIL = ClassName.get("android.databinding", "DataBindingUtil");
    public static final ClassName POST = ClassName.get("retrofit2.http", "POST");
}