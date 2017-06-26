package com.suhang.sample;

import android.app.Application;

import com.suhang.layoutfinder.SharedPreferencesFinder;
import com.suhang.layoutfinderannotation.GenRootComponent;
import com.suhang.sample.dagger.module.AppModule;

import javax.inject.Singleton;


/**
 * Created by 苏杭 on 2017/5/18 12:53.
 */
@GenRootComponent(scope = Singleton.class,modules = AppModule.class,tag = 10)
public class App extends Application{


    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesFinder.init(this);
        appComponent = DaggerAppComponent.builder().setModule(new AppModule(this)).build();
        appComponent.injectMembers(this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
