package com.suhang.sample.dagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Scope;

/**
 * Created by 苏杭 on 2017/6/26 14:25.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE,ElementType.METHOD,ElementType.FIELD})
@Scope
public @interface BaseScope {
}
