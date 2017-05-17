package com.suhang.layoutfinderannotation.dagger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 苏杭 on 2017/5/17 14:48.
 */
@Retention(RetentionPolicy.CLASS)
@Inherited
@Target(ElementType.TYPE)
/**
 * 生成dagger的Component的注解,参数为要生成的组件的名称
 */
public @interface GenInject {
	String name();
}
