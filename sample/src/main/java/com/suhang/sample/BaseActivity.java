package com.suhang.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.suhang.sample.dagger.BaseComponent;
import com.suhang.sample.dagger.module.BaseModule;

/**
 * Created by 苏杭 on 2017/6/8 21:12.
 */

public class BaseActivity extends AppCompatActivity {

    private BaseComponent mBaseComponent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBaseComponent = ((App) getApplication()).getAppComponent().providerBaseComponent().setModule(new BaseModule(this)).build();
    }

    public BaseComponent getBaseComponent() {
        return mBaseComponent;
    }
}
