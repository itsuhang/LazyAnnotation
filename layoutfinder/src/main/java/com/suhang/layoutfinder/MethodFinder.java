package com.suhang.layoutfinder;

import android.util.ArrayMap;

import java.util.Map;

import io.reactivex.Flowable;

/**
 * Created by 苏杭 on 2017/5/15 20:06.
 */

public class MethodFinder {
    private static Map<String, BaseMethodFinder> mFinderMap = new ArrayMap<>();

    public static Flowable find(Class aClass, String url, Object ...objects) {
        String packname = aClass.getCanonicalName();
        BaseMethodFinder finder = mFinderMap.get(packname);
        if (finder == null) {
            throw new RuntimeException("MethodFinder:请先inject");
        }
        return finder.find(url,objects);
    }

    @SuppressWarnings("unchecked")
    public static void inject(Object o, Class aClass) {
        String packname = aClass.getCanonicalName();
        try {
            BaseMethodFinder finder = mFinderMap.get(packname);
            if (finder == null) {
                Class<?> tClass = Class.forName(packname + "$$Finder");
                finder = (BaseMethodFinder) tClass.newInstance();
                mFinderMap.put(packname, finder);
            }
            finder.inject(o);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("MethodFinder:find()出错", e);
        }
    }
}
