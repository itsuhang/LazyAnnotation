package com.suhang.sample;

import com.suhang.layoutfinderannotation.FindMethod;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Path;
@FindMethod
public interface NetworkOtherService {
    @GET("history/content/{user}")
    Flowable<GithubBean> getGithubData(@Path("user") String path);
    @GET("hstory/content/{user}")
    Flowable<GithubBean> getAAAData(@Path("user") String path);
}
