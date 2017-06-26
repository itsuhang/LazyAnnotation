package com.suhang.sample;

import android.util.Log;


import com.suhang.sample.dagger.ActivityScope;

import javax.inject.Inject;

/**
 * Created by 苏杭 on 2017/6/2 11:23.
 */
@ActivityScope
public class Cat {

    @Inject
    public Cat() {
    }

    public void introduce() {
        Log.i("啊啊啊", "This is a cat");
    }
}
