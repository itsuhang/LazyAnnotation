package com.suhang.sample;

import android.app.Application;

/**
 * Created by 苏杭 on 2017/5/18 12:53.
 */

public class App extends Application{

    private BaseComponent mBaseComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        mBaseComponent = DaggerBaseComponent.builder().appModule(new AppModule()).build();
    }

    public BaseComponent getBaseComponent() {
        return mBaseComponent;
    }
}
