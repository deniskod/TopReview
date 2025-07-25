package com.example.topreview.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.topreview.base.Constants

@Entity(tableName = Constants.Collections.USERS)
data class User(
    @PrimaryKey val uid: String,
    val name: String,
    val imageUrl: String,
) {

    companion object {

        const val UID_KEY = "uid"
        const val NAME_KEY = "name"
        const val IMAGE_URL_KEY = "imageUrl"

        fun fromJSON(json: Map<String, Any>): User {
            val uid = json[UID_KEY] as? String ?: ""
            val name = json[NAME_KEY] as? String ?: ""
            val imageUrl = json[IMAGE_URL_KEY] as? String ?: ""

            return User(
                uid = uid,
                name = name,
                imageUrl = imageUrl,
            )
        }
    }

    val json: Map<String, Any>
        get() = hashMapOf(
            UID_KEY to uid,
            NAME_KEY to name,
            IMAGE_URL_KEY to imageUrl,
        )
}
