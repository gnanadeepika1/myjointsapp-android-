package com.saveetha.network;

import android.os.Build;

import java.time.Duration;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;
    private static ApiService service;

//    public static final String BASE_URL = "https://2fk3pt3p-80.inc1.devtunnels.ms/";

    /**
     *
     */
    public static final String BASE_URL = "http://14.139.187.229:8081/aug_batch2025/myjoints/";



    private RetrofitClient() {
        // no instance
    }

    public static Retrofit getInstance() {

            // Logging Interceptor
            HttpLoggingInterceptor loggingInterceptor =
                    new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                okHttpClient = new OkHttpClient.Builder()
                        .addInterceptor(loggingInterceptor)
                        .connectTimeout(Duration.ofSeconds(60))   // connection timeout
                        .readTimeout(Duration.ofSeconds(60))      // server response timeout
                        .writeTimeout(Duration.ofSeconds(60))     // request body timeout
                        .build();
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();

        return retrofit;
    }

    public static ApiService getService() {
//        if(service==null) {
            service = getInstance().create(ApiService.class);
//        }
        return service;
    }
}

