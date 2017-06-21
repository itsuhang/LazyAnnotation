package com.suhang.sample;

import android.util.Log;

/**
 * 本地存储SharedPreferences工具
 * Created by sh
 */
public class SharedPrefUtil {
	public static SharedPrefUtil getInstance() {
		Log.i("啊啊啊", "呃呃呃");
		return Aaa.sp;
	}
	private SharedPrefUtil() {
	}

	static class Aaa {
		static SharedPrefUtil sp = new SharedPrefUtil();
	}
}