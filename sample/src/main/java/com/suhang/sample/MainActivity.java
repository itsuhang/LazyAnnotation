package com.suhang.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.suhang.layoutfinder.MethodFinder;

import java.util.HashMap;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Retrofit.Builder builder = new Retrofit.Builder();
        builder.baseUrl("http://www.huanpeng.com").addCallAdapterFactory(RxJava2CallAdapterFactory.create()).addConverterFactory(GsonConverterFactory.create());
        NetworkService networkService = builder.build().create(NetworkService.class);
        setContentView(R.layout.activity_main);
        Log.i("啊啊啊啊", MethodFinder.find(networkService,new HashMap<String, String>(), AppMain.URL)+"");
        Log.i("啊啊啊啊", MethodFinder.find(networkService,new HashMap<String, String>(), AppMain.URL1)+"");
    }
}
