package com.suhang.sample;

import com.suhang.layoutfinderannotation.dagger.GenComponent;

/**
 * Created by 苏杭 on 2017/5/17 16:39.
 */
@GenComponent(name = ComponentList.BASE_COMPONENT,modules = {AppModule.class},subcomponents = {ComponentList.APP_COMPONENT})
public interface Component {
}
