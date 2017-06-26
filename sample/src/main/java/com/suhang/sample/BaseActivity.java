package com.suhang.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.suhang.layoutfinderannotation.GenComponent;

/**
 * Created by 苏杭 on 2017/6/8 21:12.
 */

public class BaseActivity<T> extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
