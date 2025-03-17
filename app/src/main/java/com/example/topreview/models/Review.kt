package com.example.topreview.models

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp

data class Review(
    val reviewId: String = "",
    val userId: String = "",
    val description: String = "",
    val imageUrl: String = "",
)
