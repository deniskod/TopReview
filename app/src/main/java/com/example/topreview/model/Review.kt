package com.example.topreview.model

import android.content.Context
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topreview.base.Constants
import com.example.topreview.base.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = Constants.Collections.REVIEWS)
data class Review(
    @PrimaryKey var id: String,
    val description: String,
    val rating: Float,
    val city: String,
    val imageUrl: String,
    val userId: String,
    val timestamp: Long? = null
): Parcelable {

    companion object {

        var lastUpdated: Long
            get() = MyApplication.Globals.context?.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                ?.getLong(LOCAL_LAST_UPDATED, 0) ?: 0

            set(value) {
                MyApplication.Globals.context
                    ?.getSharedPreferences("TAG", Context.MODE_PRIVATE)?.apply {
                        edit().putLong(LOCAL_LAST_UPDATED, value).apply()
                    }
            }

        const val ID_KEY = "id"
        const val DESCRIPTION_KEY = "description"
        const val RATING_KEY = "rating"
        const val CITY_KEY = "city"
        const val IMAGE_URL_KEY = "imageUrl"
        const val USER_ID_KEY = "userId"
        const val LAST_UPDATED = "timestamp"
        const val LOCAL_LAST_UPDATED = "localReviewLastUpdated"

        fun fromJSON(json: Map<String, Any>): Review {
            val id = json[ID_KEY] as? String ?: ""
            val description = json[DESCRIPTION_KEY] as? String ?: ""
            val rating = (json[RATING_KEY] as? Number)?.toFloat() ?: 0f
            val city = json[CITY_KEY] as? String ?: ""
            val imageUrl = json[IMAGE_URL_KEY] as? String ?: ""
            val userId = json[USER_ID_KEY] as? String ?: ""
            val timeStamp = json[LAST_UPDATED] as? Timestamp
            val timestampLong = timeStamp?.toDate()?.time ?: System.currentTimeMillis()

            return Review(
                id = id,
                description = description,
                rating = rating,
                city = city,
                imageUrl = imageUrl,
                userId = userId,
                timestamp = timestampLong
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            ID_KEY to id,
            DESCRIPTION_KEY to description,
            RATING_KEY to rating,
            CITY_KEY to city,
            IMAGE_URL_KEY to imageUrl,
            USER_ID_KEY to userId,
            LAST_UPDATED to FieldValue.serverTimestamp()
        )
}
