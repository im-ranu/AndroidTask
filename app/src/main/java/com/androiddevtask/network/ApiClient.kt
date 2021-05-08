package com.androiddevtask.network

import android.content.Context

import com.androiddevtask.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor


import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object ApiClient {


    private var retrofit: Retrofit? = null
    private var okHttpClient: OkHttpClient? = null
    fun getClient(context: Context): Retrofit? {

        if (okHttpClient == null) {
            initOkHttp(context)
        }

        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit
    }


    private fun initOkHttp(context: Context) {

        val httpClient: OkHttpClient.Builder = OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)

        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        httpClient.addInterceptor(interceptor)
        httpClient.addInterceptor { chain ->
            val original: Request = chain.request()
            val requestBuilder: Request.Builder = original.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization","Bearer ${Constants.AUTH_TOKEN}")

            val request: Request = requestBuilder.build()
            chain.proceed(request)
        }
        okHttpClient = httpClient.build()
    }
}