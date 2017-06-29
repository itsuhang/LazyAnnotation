package com.suhang.layoutfinderannotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 苏杭 on 2017/6/29 13:51.
 *
 * 用于父组件对应的类是基类,子组件都是继承自基类,若该基类的某些子类没有属于自己的组件,则可使用父组件来注入自己
 */
@Retention(RetentionPolicy.CLASS)
@Inherited
@Target(ElementType.TYPE)
public @interface GenInheritedRootComponent {
    Class[] modules() default {};
    Class scope() default Object.class;

    boolean shouldInject() default true;
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
