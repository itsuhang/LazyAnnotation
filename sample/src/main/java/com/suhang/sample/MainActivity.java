package com.suhang.sample;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.suhang.layoutfinder.ContextProvider;
import com.suhang.layoutfinder.LayoutFinder;
import com.suhang.layoutfinder.MethodFinder;
import com.suhang.layoutfinderannotation.BindLayout;
import com.suhang.sample.databinding.ActivityMainBinding;

import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
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
//        Log.i("啊啊啊啊", MethodFinder.find(NetworkService.class,new HashMap<String, String>(), AppMain.URL)+"");
        Log.i("啊啊啊啊", MethodFinder.find("history/content/{user}", "5/10") + "");
        MethodFinder.find("history/content/{user}", "5/10").subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer() {
            @Override
            public void accept(@NonNull Object o) throws Exception {
                Log.i("啊啊啊啊", o.toString());
            }
        });
    }

    @Override
    public Context providerContext() {
        return this;
    }
}
