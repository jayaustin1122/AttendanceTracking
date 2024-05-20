package com.example.attendance.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class UserRepository {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    suspend fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun uploadImage(uri: Uri, uid: String): Uri {
        val reference = storage.reference.child("Attendance/profile").child(uid)
        reference.putFile(uri).await()
        return reference.downloadUrl.await()
    }

    fun getRFIDData(): DatabaseReference {
        return database.getReference("Attendance/RFID")
    }

    fun getFingerPrint(): DatabaseReference {
        return database.getReference("Attendance/FingerPrints")
    }

    fun saveUserData(uid: String, userData: Map<String, Any?>): DatabaseReference {
        val userRef = database.getReference("Attendance/Users").child(uid)
        userRef.setValue(userData)
        return userRef
    }

    fun clearRFIDData() {
        database.getReference("Attendance/RFID").setValue("")
    }

    fun clearFingerPrint() {
        database.getReference("Attendance/FingerPrints").setValue("")
    }
}
