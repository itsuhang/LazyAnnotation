package com.suhang.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.suhang.layoutfinder.ContextProvider;
import com.suhang.layoutfinder.LayoutFinder;
import com.suhang.layoutfinder.MethodFinder;
import com.suhang.layoutfinderannotation.BindLayout;
import com.suhang.sample.databinding.ActivityMainBinding;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements ContextProvider{
    @BindLayout
    ActivityMainBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutFinder.find(this,R.layout.activity_main);
        Retrofit.Builder builder = new Retrofit.Builder();
        NetworkService networkService = builder.baseUrl("http://www.huanpeng.com").addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create()).build().create(NetworkService.class);
        NetworkOtherService otherNetworkService = builder.baseUrl("http://gank.io/api/").addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create()).build().create(NetworkOtherService.class);
        MethodFinder.inject(networkService, NetworkService.class);
        MethodFinder.inject(otherNetworkService, NetworkOtherService.class);
    }

    @Override
    public Context providerContext() {
        return this;
    }
}
