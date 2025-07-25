package com.example.topreview.base

import com.example.topreview.model.Review
import com.example.topreview.model.User

typealias ReviewsCallback = (List<Review>) -> Unit
typealias UsersCallback = (List<User>) -> Unit
typealias EmptyCallback = () -> Unit

object Constants {

    object Collections {
        const val REVIEWS = "reviews"
        const val USERS = "users"
    }
}