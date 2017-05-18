package com.suhang.sample;

import com.suhang.layoutfinderannotation.dagger.GenInject;
import com.suhang.layoutfinderannotation.dagger.GenSubComponent;

/**
 * Created by 苏杭 on 2017/5/17 16:39.
 */
@GenSubComponent(name = "AppComponent",modules = {AppModule2.class},scope = BaseScope.class)
public interface Component1 {
    @GenInject
    void inject(MainActivity activity);
}
