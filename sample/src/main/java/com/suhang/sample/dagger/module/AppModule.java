package com.suhang.sample.dagger.module;


import android.app.Application;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by 苏杭 on 2017/6/2 12:34.
 */
@Singleton
@Module
public class AppModule {
    private Application app;

    public AppModule(Application app) {
        this.app = app;
    }

    @Singleton
    @Provides
    Application providerApp() {
        return app;
    }
}
