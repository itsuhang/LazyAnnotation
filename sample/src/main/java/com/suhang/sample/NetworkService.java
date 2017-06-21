package com.suhang.sample;

import com.suhang.layoutfinderannotation.FindMethod;
import com.suhang.layoutfinderannotation.ToString;

import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by 苏杭 on 2017/5/15 17:35.
 */

@FindMethod
public interface NetworkService {
    @POST(AppMain.URL)
    @FormUrlEncoded
    @ToString("http://www.huanpeng.com")
    Flowable<AppMain> getAppMain(@FieldMap Map<String, String> params);

    @POST(AppMain.URL1)
    @FormUrlEncoded
    @ToString("http://dev.huanpeng.com")
    Flowable<AppMain> getApp(@FieldMap Map<String, String> params);
}
