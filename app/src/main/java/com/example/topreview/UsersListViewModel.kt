package com.example.topreview

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.topreview.model.User
import com.example.topreview.model.UserModel

class UsersListViewModel : ViewModel() {

    var users: LiveData<List<User>> = UserModel.shared.users

    fun refreshAllUsers() {
        UserModel.shared.refreshAllUsers()
    }
}