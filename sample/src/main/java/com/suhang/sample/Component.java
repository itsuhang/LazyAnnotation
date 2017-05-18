package com.suhang.sample;

import com.suhang.layoutfinderannotation.dagger.GenComponent;
import com.suhang.layoutfinderannotation.dagger.GenSub;

import javax.inject.Singleton;

/**
 * Created by 苏杭 on 2017/5/17 16:39.
 */
@GenComponent(name = "BaseComponent",modules = {AppModule.class},scope = Singleton.class)
public interface Component {
    @GenSub
    AppComponent providerAppComponent(AppModule2 module2);
    @GenSub
    BCom providerBCMComponent(AppModule2 module2);

}
