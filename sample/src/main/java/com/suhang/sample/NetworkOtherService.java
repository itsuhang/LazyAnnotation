package com.suhang.sample;

import io.reactivex.Flowable;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NetworkOtherService {
    @GET("history/content/{user}")
    Flowable<GithubBean> getGithubData(@Path("user") String path);
}
