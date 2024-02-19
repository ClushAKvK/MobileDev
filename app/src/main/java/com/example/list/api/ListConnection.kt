package com.example.list.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


private const val CONNECT_TIMEOUT_INTERVAL_SEC = 10L
private const val READ_TIMEOUT_INTERVAL_SEC = 10L
private const val WRITE_TIMEOUT_INTERVAL_SEC = 10L
private const val BASE_URL = "http://10.0.2.2:5551"
//private const val BASE_URL = "http://192.168.88.49:49995"



object ListConnection {
    private var retrofit: Retrofit? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_INTERVAL_SEC, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_INTERVAL_SEC, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_INTERVAL_SEC, TimeUnit.SECONDS)
        .addInterceptor(initInterceptor())
        .build()


    var gson = GsonBuilder()
        .setDateFormat("yyyy.MM.dd")
        .create()

    fun getClient(): Retrofit {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
        return retrofit!!
    }

    fun initInterceptor() : HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        return interceptor
    }
}