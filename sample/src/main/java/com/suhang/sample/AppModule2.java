package com.suhang.sample;

import dagger.Module;
import dagger.Provides;

/**
 * Created by 苏杭 on 2017/5/17 15:08.
 */
@Module
public class AppModule2 {
    @Provides
    AppMain providerBean() {
        return new AppMain();
    }
}
