package com.example.topreview.model

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.topreview.base.EmptyCallback
import com.example.topreview.model.dao.AppLocalDb
import com.example.topreview.model.dao.AppLocalDbRepository
import java.util.concurrent.Executors

class UserModel private constructor() {

    enum class LoadingState {
        LOADING,
        LOADED
    }

    private val database: AppLocalDbRepository = AppLocalDb.database
    private var executor = Executors.newSingleThreadExecutor()
    val users: LiveData<List<User>> = database.userDao().getAllUsers()
    val loadingState: MutableLiveData<LoadingState> = MutableLiveData<LoadingState>()
    private val firebaseModel = FirebaseModel()
    companion object {
        val shared = UserModel()
    }

    fun refreshAllUsers() {
        loadingState.postValue(LoadingState.LOADING)
        firebaseModel.getAllUsers() { users ->
            executor.execute {
                for (user in users) {
                    database.userDao().insert(user)
                }
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }



    fun add(user: User, image: Bitmap?, callback: EmptyCallback) {
        firebaseModel.addUser(user) {
            image?.let {
                uploadTo(
                    image = image,
                    name = user.uid,
                    callback = { uri ->
                        if (!uri.isNullOrBlank()) {
                            val st = user.copy(imageUrl = uri)
                            firebaseModel.addUser(st, callback)
                        } else {
                            callback()
                        }
                    },
                )
            } ?: callback()
        }
    }

    fun getUserById(uid: String) {
        loadingState.postValue(LoadingState.LOADING)
        firebaseModel.getUserById(uid) { users ->
            executor.execute {
                for (user in users) {
                    database.userDao().insert(user)
                }
                loadingState.postValue(LoadingState.LOADED)
            }
        }
    }

    private fun uploadTo(image: Bitmap, name: String, callback: (String?) -> Unit) {
        uploadImageToFirebase(image, name, callback)
    }

    private fun uploadImageToFirebase(
        image: Bitmap,
        name: String,
        callback: (String?) -> Unit
    ) {
        firebaseModel.uploadImage(image, name, callback)
    }
}