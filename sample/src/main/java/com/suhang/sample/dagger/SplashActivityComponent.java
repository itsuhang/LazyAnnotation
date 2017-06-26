package com.suhang.sample.dagger;


import com.suhang.sample.SplashActivity;
import com.suhang.sample.dagger.module.ActivityModule;

import dagger.MembersInjector;
import dagger.Subcomponent;


/**
 * Created by 苏杭 on 2017/6/2 11:18.
 */
@ActivityScope
@Subcomponent(modules = ActivityModule.class)
public interface SplashActivityComponent extends MembersInjector<SplashActivity> {
    @Subcomponent.Builder
    interface Builder {
        Builder activityModule(ActivityModule activityModule);
        SplashActivityComponent build();
    }
}
