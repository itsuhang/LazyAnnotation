package com.suhang.sample;

import com.suhang.layoutfinderannotation.dagger.GenComponent;

/**
 * Created by 苏杭 on 2017/5/17 16:39.
 */
@GenComponent(name = "AAAComponent",modules = {AppModule.class},scope = BaseScope.class)
public interface Component2 {
}
