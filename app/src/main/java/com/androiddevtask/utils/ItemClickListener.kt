package com.androiddevtask.utils

import com.androiddevtask.user.UserResponse

interface ItemClickListener {

    fun onClick(userResponse: UserResponse, position : Int)
}