package com.example.list.api

import retrofit2.http.GET
import com.example.list.data.Companies
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import com.example.list.data.Couriersed
import com.example.list.data.Ordered
import com.example.list.data.User
import com.example.list.data.UserLogin
import retrofit2.Call
import retrofit2.http.Query

const val APPEND_COMPANY = 11
const val UPDATE_COMPANY = 12
const val DELETE_COMPANY = 13
const val APPEND_COURIER = 21
const val UPDATE_COURIER = 22
const val DELETE_COURIER = 23
const val APPEND_ORDER = 31
const val UPDATE_ORDER = 32
const val DELETE_ORDER = 33

interface ListAPI{
    @GET("code=10")
    fun getCompanys(): Call<Companies>

    @Headers("Content-Type: application/json")
    @POST("company")
    fun postCompany(@Body postCompany: PostCompany): Call<PostResult>

    @GET("code=20")
    fun getCouriers(): Call<Couriersed>

    @Headers("Content-Type: application/json")
    @POST("couriers")
    fun postCourier(@Body postCouriers: PostCouriers): Call<PostResult>

    @GET("code=30")
    fun getOrders(): Call<Ordered>

    @Headers("Content-Type: application/json")
    @POST("orders")
    fun postOrders(@Body postOrders: PostOrders): Call<PostResult>

    @Headers("Content-Type: application/json")
    @POST("auth/login")
    fun login(@Body user: UserLogin): Call<PostResult>

    @Headers("Content-Type: application/json")
    @POST("auth/registration")
    fun registration(@Body user: User): Call<PostResult>

}