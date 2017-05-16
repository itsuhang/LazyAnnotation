package com.suhang.layoutfinder;

import io.reactivex.Flowable;

/**
 * Created by 苏杭 on 2017/5/15 20:07.
 */

public interface BaseMethodFinder<T> {
	Flowable find(String url, Object[] objects);

	void inject(T t);
}
