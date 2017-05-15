package com.suhang.layoutfinder;

import android.databinding.DataBindingUtil;
import android.util.ArrayMap;
import android.view.View;

import java.util.Map;

/**
 * Created by 苏杭 on 2017/5/11 13:38.
 */

public class LayoutFinder {
    private static Map<String, BaseFinder> finds = new ArrayMap<>();
    @SuppressWarnings("unchecked")
    public static <T extends ContextProvider> void find(T cp, int layout) {
        try {
            String name = cp.getClass().getName();
            BaseFinder finder = finds.get(name);
            if (finder == null) {
                Class<?> aClass = Class.forName(name + "$$Finder");
                finder = (BaseFinder) aClass.newInstance();
                finds.put(name, finder);
            }
            finder.find(cp, layout);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("LayoutFinder:find()出错", e);
        }
    }
}
