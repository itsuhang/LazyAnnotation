package com.suhang.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.suhang.layoutfinder.ContextProvider;
import com.suhang.layoutfinder.LayoutFinder;
import com.suhang.layoutfinderannotation.BindLayout;
import com.suhang.sample.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements ContextProvider{
    @BindLayout
    ActivityMainBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutFinder.find(this,R.layout.activity_main);
    }

    @Override
    public Context providerContext() {
        return this;
    }
}
