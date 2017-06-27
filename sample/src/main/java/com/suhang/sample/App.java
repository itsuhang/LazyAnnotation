package com.suhang.sample;

import android.app.Application;

import com.suhang.layoutfinder.SharedPreferencesFinder;
import com.suhang.layoutfinderannotation.GenDaggerHelper;
import com.suhang.layoutfinderannotation.GenRootComponent;
import com.suhang.sample.dagger.module.AppModule;

import javax.inject.Singleton;


/**
 * Created by 苏杭 on 2017/5/18 12:53.
 */
@GenDaggerHelper
@GenRootComponent(scope = Singleton.class,modules = {AppModule.class},tag = 10)
public class App extends Application{


    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesFinder.init(this);
        appComponent = DaggerHelper.getInstance().getAppComponent(this,this);
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
