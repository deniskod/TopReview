package com.example.topreview.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val rating: Float,
    val imageUrl: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable {
    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(description)
        parcel.writeFloat(rating)
        parcel.writeString(imageUrl)
        parcel.writeString(userId)
        parcel.writeLong(timestamp)
    }

    companion object CREATOR : Parcelable.Creator<Review> {
        override fun createFromParcel(parcel: Parcel): Review {
            return Review(
                id = parcel.readLong(),
                description = parcel.readString() ?: "",
                rating = parcel.readFloat(),
                imageUrl = parcel.readString() ?: "",
                userId = parcel.readString() ?: "",
                timestamp = parcel.readLong()
            )
        }

        override fun newArray(size: Int): Array<Review?> {
            return arrayOfNulls(size)
        }
    }
}
