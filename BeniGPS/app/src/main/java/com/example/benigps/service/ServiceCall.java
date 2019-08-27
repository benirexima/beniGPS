package com.example.benigps.service;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceCall {

    public static ServiceCall serviceCall = new ServiceCall();

    public static ServiceCall getInstance(){

        return serviceCall;
    }


    public Retrofit getRetroObject(){

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request request1 = request.newBuilder().header("Accept", "application/json")
                        .header("Content-Type", "application/json").build();

                return chain.proceed(request1);
            }
        });

        OkHttpClient httpClient1 =httpClient.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(" http://2b951580.ngrok.io/ ")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient1)
                .build();


        return retrofit;

    }
}
