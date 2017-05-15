package com.suhang.layoutfinder;

/**
 * Created by 苏杭 on 2017/5/11 13:52.
 */

public interface BaseFinder<T extends ContextProvider> {
    void find(T cp, int id);
}
