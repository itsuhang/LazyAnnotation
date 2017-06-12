package com.suhang.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.suhang.layoutfinderannotation.DaggerFinder;

/**
 * Created by 苏杭 on 2017/6/8 21:12.
 */

public class BaseActivity<T> extends AppCompatActivity{
	@DaggerFinder
	T model;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
}
