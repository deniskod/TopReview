package com.example.topreview.repository

import android.util.Log
import com.example.topreview.dao.UserDao
import com.example.topreview.models.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDao) {
    private val db = Firebase.firestore.collection("users")

    suspend fun saveUserToFirestore(uid: String, name: String, imageUri: String) : User?{
        val user = User(uid = uid, name = name, imageUrl = imageUri)

        return try {
            db.document(uid)
                .set(user, SetOptions.merge())
                .await()

            userDao.insert(user)

            user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUserById(uid: String): User? {
        return try {
            val snapshot = db.whereEqualTo("uid", uid).get().await()
            val document = snapshot.documents.firstOrNull()
            val user = document?.toObject(User::class.java)

            if (user != null) {
                userDao.insert(user)
            }

            user
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to get user from Firestore", e)
            null
        }
    }

    suspend fun updateUser(user: User) {
        try {
            db.document(user.uid).set(user).await()

            userDao.update(user)

            Log.d("UserRepository", "User updated in Firestore and Room: ${user.uid}")
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to update user in Firestore", e)
        }
    }

    suspend fun getAll(): List<User> {
        return try {
            val snapshot = db.get().await()
            val users = mutableListOf<User>()

            for (document in snapshot.documents) {
                try {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                } catch (e: Exception) {
                    Log.e("getAll()", "Failed to parse document: ${document.id}", e)
                }
            }

            Log.d("getAll()", "Returning ${users.size} parsed users.")
            users
        } catch (e: Exception) {
            Log.e("getAll()", "Error fetching users from Firestore", e)
            emptyList()
        }
    }
}