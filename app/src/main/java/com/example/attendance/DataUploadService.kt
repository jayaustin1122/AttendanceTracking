package com.example.attendance
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.example.attendance.admin.tabs.model.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DataUploadService : Service() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val handler = Handler()

    override fun onCreate() {
        super.onCreate()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(uploadDataRunnable)
        return START_STICKY
    }

    private val uploadDataRunnable = object : Runnable {
        override fun run() {

                uploadDataToFirebase()
            Toast.makeText(this@DataUploadService,"ssgee", Toast.LENGTH_SHORT)
            // Schedule the next upload after 2 seconds
            handler.postDelayed(this, 2000)
        }
    }

    private fun isDataAvailable(): Boolean {
        var dataAvailable = false
        val userRef = database.getReference("Pasok")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val pasokData = dataSnapshot.value as? Map<String, Any>
                if (pasokData != null) {
                    // Check if any of the fields have values
                    val faceData = pasokData["face"] as? String
                    val fingerprintData = pasokData["fingerprint"] as? String
                    val rfidData = pasokData["rfid"] as? String

                    if (faceData != null || fingerprintData != null || rfidData != null) {
                        // Data is available
                        dataAvailable = true
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("DataUploadService", "Pasok:onCancelled", databaseError.toException())
            }
        })

        return dataAvailable
    }

    private fun uploadDataToFirebase() {
        val pasokRef = database.getReference("Pasok")

        pasokRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(pasokSnapshot: DataSnapshot) {
                // Iterate through each child of "Pasok"
                pasokSnapshot.children.forEach { pasokChild ->
                    // Get the data from "Pasok"
                    val pasokData = pasokChild.getValue() as? Map<String, Any>

                    // Check if any of the fields exist
                    val faceData = pasokData?.get("face") as? String
                    val fingerprintData = pasokData?.get("fingerprint") as? String
                    val rfidData = pasokData?.get("rfid") as? String

                    // Query the "Users" node to find matching user
                    val usersRef = database.getReference("Users")
                    usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(usersSnapshot: DataSnapshot) {
                            usersSnapshot.children.forEach { userChild ->
                                val userData = userChild.getValue(UsersModel::class.java)

                                // Check if any of the fields match
                                if (userData?.face == faceData || userData?.fingerPrint == fingerprintData || userData?.RFID == rfidData) {
                                    // If a match is found, upload data to the corresponding user's "Attendance" subpath
                                    val userAttendanceRef = userChild.child("Attendance").ref
                                    userAttendanceRef.updateChildren(pasokData!!).addOnCompleteListener { uploadTask ->
                                        if (uploadTask.isSuccessful) {
                                            Log.d("DataUploadService", "Data uploaded successfully")
                                        } else {
                                            Log.e("DataUploadService", "Failed to upload data", uploadTask.exception)
                                        }
                                    }
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            Log.w("DataUploadService", "Users:onCancelled", databaseError.toException())
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("DataUploadService", "Pasok:onCancelled", databaseError.toException())
            }
        })
    }


    override fun onBind(intent: Intent): IBinder? {
        // Return null because this is not a bound service
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove any pending tasks from the handler
        handler.removeCallbacks(uploadDataRunnable)
    }
}
