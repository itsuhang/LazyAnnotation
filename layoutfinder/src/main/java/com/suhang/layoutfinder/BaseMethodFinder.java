package com.suhang.layoutfinder;

import java.util.Map;

import io.reactivex.Flowable;

/**
 * Created by 苏杭 on 2017/5/15 20:07.
 */

public interface BaseMethodFinder<T> {
	Flowable find(T t , Map<String ,String > params, String url);
}
