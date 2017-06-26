package com.suhang.sample.dagger;

import com.suhang.layoutfinderannotation.GenParentComponent;
import com.suhang.sample.dagger.module.BaseModule;

/**
 * Created by 苏杭 on 2017/6/26 23:22.
 */
@GenParentComponent(tag = 10,modules = BaseModule.class,scope = BaseScope.class)
public class Base {
}
