package com.suhang.sample.dagger.component;


import com.suhang.sample.MainActivity;
import com.suhang.sample.dagger.ActivityScope;
import com.suhang.sample.dagger.module.ActivityModule;

import dagger.MembersInjector;
import dagger.Subcomponent;


/**
 * Created by 苏杭 on 2017/6/2 11:18.
 */
@ActivityScope
@Subcomponent(modules = ActivityModule.class)
public interface ActivityComponent extends MembersInjector<MainActivity> {
    @Subcomponent.Builder
    interface Builder {
        Builder activityModule(ActivityModule activityModule);
        ActivityComponent build();
    }
}
