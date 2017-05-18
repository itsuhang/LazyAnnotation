package com.suhang.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.suhang.layoutfinder.ContextProvider;
import com.suhang.layoutfinder.LayoutFinder;
import com.suhang.layoutfinderannotation.BindLayout;
import com.suhang.sample.databinding.ActivityMainBinding;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements ContextProvider{
    @BindLayout
    ActivityMainBinding mBinding;
    @Inject
    AppMain mAppMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutFinder.find(this,R.layout.activity_main);
        ((App) getApplication()).getBaseComponent().providerBCom(new AppModule2()).inject(this);
    }

    @Override
    public Context providerContext() {
        return this;
    }
}
