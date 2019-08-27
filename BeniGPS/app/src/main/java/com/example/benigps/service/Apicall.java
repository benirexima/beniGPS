package com.example.benigps.service;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Apicall {

    private static Retrofit retrofit = null;
    public static Gps_api getClient() {

        // change your base URL
        if (retrofit==null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(" http://2b951580.ngrok.io/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        //Creating object for our interface
        Gps_api api = retrofit.create(Gps_api.class);
        return api; // return the APIInterface object
    }
}
