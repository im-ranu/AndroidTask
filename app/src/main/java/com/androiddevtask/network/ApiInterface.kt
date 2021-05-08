package com.androiddevtask.network


import com.androiddevtask.profile.ProfileResponse
import com.androiddevtask.user.UserResponse

import io.reactivex.rxjava3.core.Single
import retrofit2.http.*


interface ApiInterface {



    @GET("users")
    fun getUserList(@Query("since") int : Int) : Single<List<UserResponse>>

    @GET("users/{username}")
    fun getUserDetails(@Path("username") username : String) : Single<ProfileResponse>


}