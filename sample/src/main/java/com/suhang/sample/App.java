package com.suhang.sample;

import android.app.Application;

import com.suhang.layoutfinder.SharedPreferencesFinder;
import com.suhang.sample.dagger.component.AppComponent;
import com.suhang.sample.dagger.component.DaggerAppComponent;
import com.suhang.sample.dagger.module.AppModule;


/**
 * Created by 苏杭 on 2017/5/18 12:53.
 */

public class App extends Application{


    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesFinder.init(this);
        appComponent = DaggerAppComponent.builder().appModule(new AppModule(this)).build();
        appComponent.injectMembers(this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
