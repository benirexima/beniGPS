package com.example.benigps.service;


import com.example.benigps.Model.Model;

import java.util.List;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface Gps_api {

    @FormUrlEncoded
    @POST("api/user/registerUser")
    Call<Model> reguser(@Field("username") String username,
                        @Field("password") String password,
                        @Field("deviveId") String deviceId,
                        @Field("role") String role,
                        @Field("refreshToken") String refreshtoken);
    @FormUrlEncoded
    @POST("api/track")
    Call<Model>postTrack(@Field("username") String username,
                         @Field("deviceId") String deviceId,
                         @Field("inTime") String inTime);

    @GET
    Call<List<Model>> getModels();


}
