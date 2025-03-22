package com.example.topreview.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.topreview.models.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ReviewViewModel : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    fun uploadReview(imageUri: Uri, description: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Log.e("ReviewViewModel", "User not authenticated.")
            _uploadStatus.value = "Authentication error. Please log in."
            return
        }

        _loading.value = true
        val reviewId = UUID.randomUUID().toString()
        val storageRef =  FirebaseStorage.getInstance("gs://topreview-b5453").reference
        val imageRef = storageRef.child("reviews/$userId/$reviewId.jpg")



        val uploadTask = imageRef.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            Log.d("ReviewViewModel", "Image uploaded successfully.")
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Log.d("ReviewViewModel", "Image URL: $uri")
                _uploadStatus.value = "Review uploaded!"
                _loading.value = false
            }
        }.addOnFailureListener { exception ->
            Log.e("ReviewViewModel", "Upload failed", exception)
            _uploadStatus.value = "Upload failed: ${exception.message}"
            _loading.value = false
        }


        Log.d("ReviewViewModel", "Image upload task has been initiated.")
    }

}
