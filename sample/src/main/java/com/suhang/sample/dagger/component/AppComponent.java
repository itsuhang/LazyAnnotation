package com.suhang.sample.dagger.component;


import com.suhang.sample.App;
import com.suhang.sample.MainActivityComponent;
import com.suhang.sample.dagger.SplashActivityComponent;
import com.suhang.sample.dagger.module.AppModule;

import dagger.Component;
import dagger.MembersInjector;

/**
 * Created by 苏杭 on 2017/6/2 12:33.
 */
@Component(modules = {AppModule.class})
public interface AppComponent extends MembersInjector<App>{
    @Component.Builder
    interface Builder {
        Builder appModule(AppModule appModule);
        AppComponent build();
    }

    MainActivityComponent.Builder activityComponent();
    SplashActivityComponent.Builder splashActivityComponent();
}
