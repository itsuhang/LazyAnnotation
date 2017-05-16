package com.suhang.layoutfinder;

import android.util.ArrayMap;

import java.util.Map;

import io.reactivex.Flowable;

/**
 * Created by 苏杭 on 2017/5/15 20:06.
 */

public class MethodFinder {
    private static Map<String, BaseMethodFinder> mFinderMap = new ArrayMap<>();

    public static Flowable find(String url, Object ...objects) {
        for (Map.Entry<String, BaseMethodFinder> entry : mFinderMap.entrySet()) {
            Flowable flowable = entry.getValue().find(url, objects);
            if (flowable!=null) {
                return flowable;
            }
        }
        return null;
    }

    /**
     *
     * @param o 要生成的Retrofit的Service
     * @param aClass 要生成的Retrofit的Service的字节码(必须是XXXService.class,而不能是xxx.getClass())
     */
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
