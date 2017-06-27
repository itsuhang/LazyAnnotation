package com.suhang.sample.dagger.module;

import android.app.Activity;


import com.suhang.sample.Dog;
import com.suhang.sample.dagger.BaseScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by 苏杭 on 2017/6/2 11:18.
 */
@Module
public class ActivityModule {
    private  Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @BaseScope
    Activity provideActivity() {
        return activity;
    }

}
