package com.example.topreview.model

import android.graphics.Bitmap
import android.util.Log
import com.example.topreview.base.Constants
import com.example.topreview.base.EmptyCallback
import com.example.topreview.base.ReviewsCallback
import com.example.topreview.base.UsersCallback
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class FirebaseModel {

    private val database = Firebase.firestore
    private val storage = Firebase.storage

    init {

        val settings = firestoreSettings {
            setLocalCacheSettings(memoryCacheSettings {  })
        }
        database.firestoreSettings = settings
    }

    fun getAllReviews(callback: ReviewsCallback) {
        database.collection(Constants.Collections.REVIEWS)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val reviews: MutableList<Review> = mutableListOf()
                        for (json in it.result) {
                            reviews.add(Review.fromJSON(json.data))
                        }
                        Log.d("TAG", reviews.size.toString())
                        callback(reviews)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun getUserReviews(userId: String, callback: ReviewsCallback) {
        database.collection(Constants.Collections.REVIEWS)
            .whereEqualTo("userId",userId)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val reviews: MutableList<Review> = mutableListOf()
                        for (json in it.result) {
                            reviews.add(Review.fromJSON(json.data))
                        }
                        Log.d("TAG", reviews.size.toString())
                        callback(reviews)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun addReview(review: Review, callback: EmptyCallback) {
        database.collection(Constants.Collections.REVIEWS).document(review.id).set(review.json)
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun deleteReview(review: Review, callback: EmptyCallback) {
        database.collection(Constants.Collections.REVIEWS).document(review.id).delete()
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun getAllUsers(callback: UsersCallback) {
        database.collection(Constants.Collections.USERS)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (json in it.result) {
                            users.add(User.fromJSON(json.data))
                        }
                        Log.d("TAG", users.size.toString())
                        callback(users)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun getUserById(uid: String, callback: UsersCallback) {
        database.collection(Constants.Collections.USERS).whereEqualTo("uid", uid)
            .get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val users: MutableList<User> = mutableListOf()
                        for (json in it.result) {
                            users.add(User.fromJSON(json.data))
                        }
                        Log.d("TAG", users.size.toString())
                        callback(users)
                    }

                    false -> callback(listOf())
                }
            }
    }

    fun addUser(user: User, callback: EmptyCallback) {
        database.collection(Constants.Collections.USERS).document(user.uid).set(user.json)
            .addOnCompleteListener {
                callback()
            }
            .addOnFailureListener {
                Log.d("TAG", it.toString() + it.message)
            }
    }

    fun uploadImage(image: Bitmap, name: String, callback: (String?) -> Unit) {
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/$name.jpg")
        val baos = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = imageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            callback(null)
        }.addOnSuccessListener { taskSnapshot ->
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                callback(uri.toString())
            }
        }
    }
}