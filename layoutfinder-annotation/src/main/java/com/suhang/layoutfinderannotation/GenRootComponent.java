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
@Target(ElementType.TYPE)
public @interface GenRootComponent {
    Class[] modules() default {};
    Class scope() default Object.class;

    /**
     * 标记,当父组件的childTag和子组件的tag一致时,在父组件中将子组件提供出来
     * @return
     */
    int tag() default 0;

    /**
     * 标记,当父组件的childTag和子组件的tag一致时,在父组件中将子组件提供出来
     * @return
     */
    int childTag() default 0;
}
