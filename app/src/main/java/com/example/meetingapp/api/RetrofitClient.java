package com.example.meetingapp.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static String BASE_URL = "http://10.0.2.2:8000/";
    private static String TOKEN;
    private static RetrofitClient mInstance;
    private static boolean needsHeader = true;
    private Retrofit retrofit;

    private RetrofitClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        chain -> {
                            Request original = chain.request();

                            Request.Builder requestBuilder = original.newBuilder()
                                    .method(original.method(), original.body());

                            if (needsHeader) {
                                requestBuilder.addHeader("Authorization", TOKEN);
                            }

                            Request request = requestBuilder.build();
                            return chain.proceed(request);
                        }
                ).build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public static void setToken(String token) {
        TOKEN = "Token " + token;
    }

    public static void needsHeader(boolean needs) {
        needsHeader = needs;
    }

    public static synchronized RetrofitClient getInstance(String token) {
        if (mInstance == null) {
            mInstance = new RetrofitClient();
            setToken(token);
        }
        return mInstance;
    }

    public Api getApi() {
        return retrofit.create(Api.class);
    }
}