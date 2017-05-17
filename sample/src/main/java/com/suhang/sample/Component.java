package com.suhang.sample;

import com.suhang.layoutfinderannotation.dagger.GenComponent;

/**
 * Created by 苏杭 on 2017/5/17 16:39.
 */
@GenComponent(name = "BaseComponent",modules = {AppModule.class},subcomponent = {AppComponent.class,ActivityComponent.class})
public class Component {
}
