package com.suhang.sample;

import dagger.*;

/**
 * Created by 苏杭 on 2017/5/17 20:39.
 */
@dagger.Component(modules = {AppModule.class,AppModule2.class})
public interface BCom {
}
