package com.suhang.layoutfinder;

import android.util.Log;

import java.util.Map;

import io.reactivex.Flowable;

/**
 * Created by 苏杭 on 2017/5/15 20:06.
 */

public class MethodFinder {
	@SuppressWarnings("unchecked")
	public static Flowable find(Object o, Map<String, String> params, String url) {

//		Log.i("啊啊啊啊", o.getClass() + "   " + o.getClass().getCanonicalName());
		try {
			Class<?> aClass = Class.forName("com.suhang.sample.Method" + "$$Finder");
			BaseMethodFinder finder = (BaseMethodFinder) aClass.newInstance();
			return finder.find(o,params, url);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("MethodFinder:find()出错", e);
		}
	}
}
