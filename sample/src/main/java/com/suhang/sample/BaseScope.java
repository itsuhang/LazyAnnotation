package com.suhang.sample;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by 苏杭 on 2017/1/20 15:46.
 */

@Scope
@Retention(RetentionPolicy.RUNTIME)
public @interface BaseScope {

}
