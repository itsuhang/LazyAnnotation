package com.suhang.sample;

import android.app.Application;

import com.suhang.layoutfinder.SharedPreferencesFinder;


/**
 * Created by 苏杭 on 2017/5/18 12:53.
 */

public class App extends Application{


    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesFinder.init(this);
    }

}
