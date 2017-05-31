package com.suhang.sample;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 本地存储SharedPreferences工具
 * Created by sh
 */
public class SharedPrefUtil {
	private static SharedPreferences mSp;
	private static String name = "config";

	private static SharedPreferences getSharedPref(Context context) {
		if (mSp == null) {
			mSp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		}
		return mSp;
	}

	/**
	 * 设置SharePrefrences文件名字
	 *
	 * @param name
	 */
	public static void setName(String name) {
		SharedPrefUtil.name = name;
	}


	//下面方法都是对不同数据类型进行保存,获取
//
//	public static void putBoolean(String key, boolean value) {
//		getSharedPref(App.getInstance()).edit().putBoolean(key, value).apply();
//	}
//
//	public static boolean getBoolean(String key, boolean defValue) {
//		return getSharedPref(App.getInstance()).getBoolean(key, defValue);
//	}
//
//	public static void putString(String key, String value) {
//		getSharedPref(App.getInstance()).edit().putString(key, value).apply();
//	}
//
//	public static String getString(String key, String defValue) {
//		return getSharedPref(App.getInstance()).getString(key, defValue);
//	}
//
//	public static void putInt(String key, int value) {
//		getSharedPref(App.getInstance()).edit().putInt(key, value).apply();
//	}
//
//	public static int getInt(String key, int defValue) {
//		return getSharedPref(App.getInstance()).getInt(key, defValue);
//	}
//
//	public static void putFloat(String key, float value) {
//		getSharedPref(App.getInstance()).edit().putFloat(key, value).apply();
//	}
//
//	public static float getFloat(String key, float defValue) {
//		return getSharedPref(App.getInstance()).getFloat(key, defValue);
//	}
//
//	public static void putLong(String key, long value) {
//		getSharedPref(App.getInstance()).edit().putLong(key, value).apply();
//	}
//
//	public static long getLong(String key, long defValue) {
//		return getSharedPref(App.getInstance()).getLong(key, defValue);
//	}


	/**
	 * 删除某个键值对
	 *
	 * @param context
	 * @param key
	 */
	public static void remove(Context context, String key) {
		getSharedPref(context).edit().remove(key).apply();
	}

}