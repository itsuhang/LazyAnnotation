package com.suhang.sample;

import android.os.Bundle;

import com.suhang.layoutfinderannotation.GenSubComponent;
import com.suhang.sample.dagger.module.SpashModule;

import javax.inject.Inject;

@GenSubComponent(modules = SpashModule.class,tag = 12)
public class SplashActivity extends BaseActivity {
    @Inject
    Cat mCat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        DaggerHelper.getInstance().getSplashActivityComponent(this);
        mCat.introduce();
    }
}
