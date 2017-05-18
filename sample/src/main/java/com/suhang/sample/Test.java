package com.suhang.sample;

import com.suhang.layoutfinderannotation.dagger.GenInject;
import com.suhang.layoutfinderannotation.dagger.GenSub;

/**
 * Created by 苏杭 on 2017/5/18 17:24.
 */

public interface Test {
    @GenSub(component = "BaseComponent")
    AppComponent providerAppComponent(AppModule2 module2);
    @GenSub(component = "BaseComponent")
    BCom providerBCMComponent(AppModule2 module2);

    @GenInject(component = "AppComponent")
    void inject(MainActivity activity);
    @GenInject(component = "AppComponent")
    void inject(GithubBean activity);
}
