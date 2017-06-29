package com.suhang.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.suhang.layoutfinderannotation.GenInheritedSubComponent;
import com.suhang.sample.dagger.BaseScope;
import com.suhang.sample.dagger.module.BaseModule;

/**
 * Created by 苏杭 on 2017/6/8 21:12.
 */
@GenInheritedSubComponent(tag = 11, childTag = 12, modules = BaseModule.class, scope = BaseScope.class,shouldInject = false)
public class BaseActivity<T extends Cat> extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerHelper.getInstance().getBaseActivityComponent(this,this);
    }
}
