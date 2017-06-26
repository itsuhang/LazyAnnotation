package com.suhang.sample.dagger.module;

import android.app.Activity;


import com.suhang.sample.dagger.ActivityScope;

import dagger.Module;
import dagger.Provides;

/**
 * Created by 苏杭 on 2017/6/2 11:18.
 */
@Module
@ActivityScope
public class ActivityModule {
    protected final Activity activity;

    public ActivityModule(Activity activity) {
        this.activity = activity;
    }

    @Provides
    @ActivityScope
    public Activity provideActivity() {
        return activity;
    }
}
