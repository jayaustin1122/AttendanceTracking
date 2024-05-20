package com.example.attendance.viewmodels

import android.app.Application
import android.app.ProgressDialog
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.attendance.repository.UserRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: UserRepository = UserRepository()

    private val _rfidData = MutableLiveData<String>()
    val rfidData: LiveData<String> = _rfidData

    private val _fingerPrint = MutableLiveData<String>()
    val fingerPrint: LiveData<String> = _fingerPrint

    private val _signUpStatus = MutableLiveData<String>()
    val signUpStatus: LiveData<String> = _signUpStatus

    fun startFetchingRFIDData() {
        repository.getRFIDData().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rfid = dataSnapshot.getValue(String::class.java) ?: "RFID Not Found or Damage"
                _rfidData.postValue(rfid)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _rfidData.postValue("RFID Not Found or Damage")
            }
        })
    }
    fun startFetchingFingerPrintsData() {
        repository.getFingerPrint().addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val finger = dataSnapshot.getValue(String::class.java) ?: "FingerPrint Not Found"
                _fingerPrint.postValue(finger)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                _fingerPrint.postValue(" Not Found or Damage")
            }
        })
    }

    fun createUserAccount(
        email: String,
        password: String,
        selectedImage: Uri,
        fullName: String,
        pin: String,
        progressDialog: ProgressDialog
    ) {
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.createUser(email, password)
                val uid = repository.auth.uid!!
                val imageUrl = repository.uploadImage(selectedImage, uid).toString()
                val currentDate = getCurrentDate()
                val currentTime = getCurrentTime()

                val userData = mapOf(
                    "uid" to uid,
                    "email" to email,
                    "fullName" to fullName,
                    "image" to imageUrl,
                    "currentDate" to currentDate,
                    "currentTime" to currentTime,
                    "id" to System.currentTimeMillis().toString(),
                    "userType" to "member",
                    "RFID" to rfidData.value,
                    "fingerprint" to fingerPrint.value,
                    "status" to true
                )

                withContext(Dispatchers.Main) {
                    repository.saveUserData(uid, userData).child(uid).setValue(userData).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            progressDialog.dismiss()
                            _signUpStatus.postValue("Account Created")
                            repository.clearRFIDData()
                            repository.clearFingerPrint()
                        } else {
                            progressDialog.dismiss()
                            _signUpStatus.postValue("Error: ${task.exception?.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    Toast.makeText(getApplication(), "Failed Creating Account: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentTime(): String {
        val tz = TimeZone.getTimeZone("GMT+08:00")
        val c = Calendar.getInstance(tz)
        val hours = String.format("%02d", c.get(Calendar.HOUR))
        val minutes = String.format("%02d", c.get(Calendar.MINUTE))
        return "$hours:$minutes"
    }

    private fun getCurrentDate(): String {
        val currentDateObject = Date()
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        return formatter.format(currentDateObject)
    }
}
