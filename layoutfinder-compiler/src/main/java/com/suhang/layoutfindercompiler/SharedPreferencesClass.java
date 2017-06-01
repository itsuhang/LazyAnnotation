package com.suhang.layoutfindercompiler;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class SharedPreferencesClass {

    private String packname;
    private Map<String, String> params;
    private String name;
    private String className;
    private Messager mMessager;
    private Elements mElements;

    public SharedPreferencesClass(Elements elements, String packname, String className, String name, Map<String, String> params, Messager messager) {
        this.packname = packname;
        this.params = params;
        this.name = name;
        this.className = className;
        this.mMessager = messager;
        this.mElements = elements;
    }

    private List<MethodSpec> getPutType(MethodSpec init) {
        List<MethodSpec> ls = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (TypeUtil.baseType.containsKey(entry.getValue())) {
                ls.add(getNormalPutType(entry.getKey(), entry.getValue(), init));
            } else {
                MethodSpec methodSpec = getSpecialPutType(entry.getKey(), entry.getValue(), init);
                ls.add(methodSpec);
            }
        }
        return ls;
    }

    private List<MethodSpec> getGetType(MethodSpec init) {
        List<MethodSpec> ls = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (TypeUtil.baseType.containsKey(entry.getValue())) {
                ls.add(getNormalGetType(entry.getKey(), entry.getValue(), init));
                ls.add(getNormalGetTypeDefault(entry.getKey(), entry.getValue(), init));
            } else {
                ls.add(getSpecialGetType(entry.getKey(), entry.getValue(), init));
            }
        }
        return ls;
    }

    private MethodSpec getInit() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("init");
        builder.beginControlFlow("if($N==null)", "mSp");
        builder.addStatement("$N=$T.getContext().getSharedPreferences($S,$T.MODE_PRIVATE)", "mSp", TypeUtil.SP, name, TypeUtil.CONTEXT);
        builder.endControlFlow();
        builder.addModifiers(Modifier.PRIVATE, Modifier.STATIC);
        return builder.build();
    }

    private MethodSpec getNormalPutType(String key, String value, MethodSpec init) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(key + "In");
        builder.addParameter(TypeUtil.baseType.get(value), "value");
        builder.addStatement("$N()", init);
        builder.addStatement("$N.edit().$N($S,$N).apply()", "mSp", getPutName(value), key, "value");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        return builder.build();
    }

    private MethodSpec getNormalGetType(String key, String value, MethodSpec init) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(key + "Out");
        builder.addParameter(TypeUtil.baseType.get(value), "defaultValue");
        builder.addStatement("$N()", init);
        builder.addStatement("return $N.$N($S,$N)", "mSp", getGetName(value), key, "defaultValue");
        builder.returns(TypeUtil.baseType.get(value));
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        return builder.build();
    }

    private MethodSpec getNormalGetTypeDefault(String key, String value, MethodSpec init) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(key + "Out");
        builder.addStatement("$N()", init);
        if ("int".equals(value)) {
            builder.addStatement("return $N.$N($S,0)", "mSp", getGetName(value), key);
        } else if ("float".equals(value)) {
            builder.addStatement("return $N.$N($S,0f)", "mSp", getGetName(value), key);
        } else if ("boolean".equals(value)) {
            builder.addStatement("return $N.$N($S,false)", "mSp", getGetName(value), key);
        } else if ("java.lang.String".equals(value)) {
            builder.addStatement("return $N.$N($S,null)", "mSp", getGetName(value), key);
        }
        builder.returns(TypeUtil.baseType.get(value));
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        return builder.build();
    }

    private MethodSpec getSpecialPutType(String key, String value, MethodSpec init) {
        TypeElement typeElement = mElements.getTypeElement(value);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(key + "In");
        builder.addParameter(TypeName.get(typeElement.asType()), "value");
        builder.addStatement("$N()", init);
        builder.addStatement("$T json = $T.toJson($N)", TypeName.get(String.class), TypeUtil.SP, "value");
        builder.addStatement("$N.edit().$N($S,$N).apply()", "mSp", getPutName(String.class.getCanonicalName()), key, "json");
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        return builder.build();
    }

    private MethodSpec getSpecialGetType(String key, String value, MethodSpec init) {
        TypeElement typeElement = mElements.getTypeElement(value);
        TypeName typeName = TypeName.get(typeElement.asType());
        MethodSpec.Builder builder = MethodSpec.methodBuilder(key + "Out");
        builder.addStatement("$N()", init);
        builder.addStatement("$T json = $N.$N($S,null)", TypeName.get(String.class), "mSp", getGetName(String.class.getCanonicalName()), key);
        builder.beginControlFlow("if($N!=null)","json");
        builder.addStatement("return $T.fromJson($N,$T.class)", TypeUtil.SP, "json", typeName);
        builder.nextControlFlow("else");
        builder.addStatement("return null");
        builder.endControlFlow();
        builder.returns(typeName);
        builder.addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        return builder.build();
    }

    private String getPutName(String value) {
        String name = "";
        if ("int".equals(value)) {
            name = "putInt";
        } else if ("float".equals(value)) {
            name = "putFloat";
        } else if ("boolean".equals(value)) {
            name = "putBoolean";
        } else if ("java.lang.String".equals(value)) {
            name = "putString";
        }
        return name;
    }

    private String getGetName(String value) {
        String name = "";
        switch (value) {
            case "int":
                name = "getInt";
                break;
            case "float":
                name = "getFloat";
                break;
            case "boolean":
                name = "getBoolean";
                break;
            case "java.lang.String":
                name = "getString";
                break;
        }
        return name;
    }

    public JavaFile gen() {
        MethodSpec init = getInit();
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                .addField(TypeUtil.SHAREPREFERENCES, "mSp", Modifier.PRIVATE, Modifier.STATIC)
                .addMethod(init);
        for (MethodSpec spec : getPutType(init)) {
            classBuilder.addMethod(spec);
        }

        for (MethodSpec spec : getGetType(init)) {
            classBuilder.addMethod(spec);
        }

        return JavaFile.builder(packname, classBuilder.build()).build();
    }
}