package com.suhang.sample;


import dagger.Subcomponent;

/**
 * Created by 苏杭 on 2017/5/17 20:39.
 */
@Subcomponent(modules = {AppModule2.class})
public interface BCom {
    void inject(MainActivity mainActivity);
}
