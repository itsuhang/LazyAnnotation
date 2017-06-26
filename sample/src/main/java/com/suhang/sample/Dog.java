package com.suhang.sample;

import android.util.Log;

import com.suhang.sample.dagger.BaseScope;

import javax.inject.Inject;

/**
 * Created by 苏杭 on 2017/6/2 11:23.
 */
@BaseScope
public class Dog {
    @Inject
    public Dog() {
    }

    public void introduce() {
        Log.i("啊啊啊", "This is a dog");
    }
}
