package com.suhang.layoutfinderannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 苏杭 on 2017/5/11 11:48.
 */
@Retention(RetentionPolicy.CLASS)
@Inherited
@Target(ElementType.TYPE)
public @interface GenParentComponent {
    Class[] modules() default {};
    Class scope() default Object.class;

    /**
     * 标记,当生成的父组件和子组件的tag一致时,在父组件中将子组件提供出来
     * @return
     */
    int tag() default 0;
}
