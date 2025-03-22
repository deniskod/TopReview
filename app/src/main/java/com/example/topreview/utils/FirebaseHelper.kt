package com.example.topreview.utils

import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.topreview.R
import com.google.firebase.storage.FirebaseStorage
import java.util.*

object FirebaseHelper {
    fun uploadImageToFirebaseStorage(imageUri: Uri, userId: String, callback: (String) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference
        val imageRef = storageReference.child("reviews/${userId}/${UUID.randomUUID()}.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()  // Get the download URL
                    callback(imageUrl)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirebaseStorage", "Image upload failed", exception)
            }
    }

    fun loadImageIntoImageView(imageUrl: String, imageView: ImageView) {
        Glide.with(imageView.context)
            .load(imageUrl)
            .centerCrop()
            .placeholder(R.drawable.placeholder_image) // Replace with your own drawable // Optional: shown on failure
            .into(imageView)
    }
}
