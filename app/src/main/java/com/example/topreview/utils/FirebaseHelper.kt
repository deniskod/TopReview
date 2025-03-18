package com.example.topreview.utils

import android.net.Uri
import android.util.Log
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
}
