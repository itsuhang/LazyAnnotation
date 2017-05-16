package com.suhang.sample;

import com.suhang.layoutfinderannotation.FindMethod;

import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by 苏杭 on 2017/5/15 17:35.
 */

@FindMethod
public interface NetworkService {
    @POST(AppMain.URL)
    @FormUrlEncoded
    Flowable<AppMain> getAppMain(@FieldMap Map<String, String> params);
    @GET("history/content/{user}")
    Flowable<GithubBean> getGithubData(@Path("user") String path);

    @POST(AppMain.URL1)
    @FormUrlEncoded
    Flowable<AppMain> getApp(@FieldMap Map<String, String> params);
}
