package com.example.attendance.ui

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.attendance.R
import com.example.attendance.admin.tabs.model.UsersModel
import com.example.attendance.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private val handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        handler.post(fetchRFIDDataRunnable)

    }

    private fun uploadDataToFirebase2() {
        val faceData = binding.face.text.toString().trim()
        val fingerprintData = binding.fingerprint.text.toString().trim()
        val rfidData = binding.rfid.text.toString().trim()

        // Check if any of the fields are empty
        if (faceData.isNotEmpty() || fingerprintData.isNotEmpty() || rfidData.isNotEmpty()) {
            // Query the "Users" node to find matching user
            val usersRef = database.getReference("Users")
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(usersSnapshot: DataSnapshot) {
                    usersSnapshot.children.forEach { userChild ->
                        val userData = userChild.getValue(UsersModel::class.java)

                        // Check if any of the fields match
                        if (userData?.face == binding.face.text ||
                            userData?.fingerPrint == binding.fingerprint.text ||
                            userData?.RFID == binding.rfid.text
                        ) {
                            val uid = userChild.key // Get the UID of the user
                            processUser2(uid) // Pass UID to another function for processing
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })

        }
    }


    private fun processUser2(uid: String?) {
        uid?.let { userId ->
            val currentDate = getCurrentDate()
            val userRef = database.getReference("Users").child(userId)
            val attendanceRef = userRef.child("Attendance").child(currentDate)

            // Check if attendance data for the current date already exists
            attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Attendance data for the current date exists, so insert the timeout timestamp
                        if (!dataSnapshot.hasChild("timeout")) {
                            // If "timeout" path doesn't exist, add it
                            val currentTime = getCurrentTime()
                            val attendanceData = HashMap<String, Any>()
                            attendanceData["timeout"] = currentTime.toString()

                            attendanceRef.updateChildren(attendanceData).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    database.getReference("Pasok/face").setValue("")
                                    database.getReference("Pasok/fingerprint").setValue("")
                                    database.getReference("Pasok/rfid").setValue("")
                                    binding.rfid.text = ""
                                    binding.face.text = ""
                                    binding.fingerprint.text = ""
                                } else {
                                    // Handle failure if needed
                                }
                            }
                        } else {
                            // If "timeout" path exists, check if it has a value
                            val timeoutValue = dataSnapshot.child("timeout").getValue(String::class.java)
                            if (timeoutValue.isNullOrEmpty()) {
                                // If "timeout" has no value, update it
                                val currentTime = getCurrentTime()
                                val attendanceData = HashMap<String, Any>()
                                attendanceData["timeout"] = currentTime.toString()

                                attendanceRef.updateChildren(attendanceData).addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        database.getReference("Pasok/face").setValue("")
                                        database.getReference("Pasok/fingerprint").setValue("")
                                        database.getReference("Pasok/rfid").setValue("")
                                        binding.rfid.text = ""
                                        binding.face.text = ""
                                        binding.fingerprint.text = ""
                                    } else {
                                        // Handle failure if needed
                                    }
                                }
                            } else {
                                database.getReference("Pasok/face").setValue("")
                                database.getReference("Pasok/fingerprint").setValue("")
                                database.getReference("Pasok/rfid").setValue("")
                                binding.rfid.text = ""
                                binding.face.text = ""
                                binding.fingerprint.text = ""
                            }
                        }
                    } else {
                        // Attendance data for the current date does not exist, so skip the upload
                        Log.d("AttendanceFragment", "Attendance data for $currentDate does not exist. Skipping timeout update.")
                        return
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
        }
    }






    private val fetchRFIDDataRunnable = object : Runnable {
        override fun run() {
            getRFIDData()
            getFingerPrint()
            getFaceID()
            uploadDataToFirebase()
            uploadDataToFirebase2()
            handler.postDelayed(this, 1000) // Fetch data every minute (60000 milliseconds)

        }
    }

    private fun getFaceID() {
        val uid = auth.uid
        val userRef = database.getReference("Pasok/face")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rfidData = dataSnapshot.getValue(String::class.java)
                if (rfidData != null) {
                    binding.face.text = rfidData
                } else {
                    // Handle the case where RFID data is not available
                    binding.face.text = "Face Not Found or Damage"

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "Face:onCancelled", databaseError.toException())
                // Handle the case where an error occurred while retrieving RFID data
                binding.face.text = "Face Not Found or Damage"

            }
        })
    }

    private fun getRFIDData() {
        val uid = auth.uid
        val userRef = database.getReference("Pasok/rfid")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rfidData = dataSnapshot.getValue(String::class.java)
                if (rfidData != null) {
                    binding.rfid.text = rfidData
                } else {
                    // Handle the case where RFID data is not available
                    binding.rfid.text = "rfid Not Found or Damage"

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "rfid:onCancelled", databaseError.toException())
                // Handle the case where an error occurred while retrieving RFID data
                binding.rfid.text = "fingerPrint Not Found or Damage"

            }
        })
    }

    private fun getFingerPrint() {
        val uid = auth.uid
        val userRef = database.getReference("Pasok/fingerprint")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rfidData = dataSnapshot.getValue(String::class.java)
                if (rfidData != null) {
                    binding.fingerprint.text = rfidData
                } else {
                    // Handle the case where RFID data is not available
                    binding.fingerprint.text = "FingerPrint Not Found or Damage"

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "fingerPrint:onCancelled", databaseError.toException())
                // Handle the case where an error occurred while retrieving RFID data
                binding.fingerprint.text = "fingerPrint Not Found or Damage"

            }
        })
    }
    private fun uploadDataToFirebase() {
        val faceData = binding.face.text.toString().trim()
        val fingerprintData = binding.fingerprint.text.toString().trim()
        val rfidData = binding.rfid.text.toString().trim()

        // Check if any of the fields are empty
        if (faceData.isNotEmpty() || fingerprintData.isNotEmpty() || rfidData.isNotEmpty()) {
            // Query the "Users" node to find matching user
            val usersRef = database.getReference("Users")
            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(usersSnapshot: DataSnapshot) {
                    usersSnapshot.children.forEach { userChild ->
                        val userData = userChild.getValue(UsersModel::class.java)

                        // Check if any of the fields match
                        if (userData?.face == binding.face.text ||
                            userData?.fingerPrint == binding.fingerprint.text ||
                            userData?.RFID == binding.rfid.text
                        ) {
                            val uid = userChild.key // Get the UID of the user
                            processUser(uid) // Pass UID to another function for processing
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })

        }
    }

    private fun processUser(uid: String?) {
        uid?.let { userId ->
            val userRef = database.getReference("Users").child(userId)
            val currentDate = getCurrentDate()

            // Check if attendance data for the current date already exists
            userRef.child("Attendance").child(currentDate).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Attendance data for the current date already exists, so skip the upload
                        Log.d("AttendanceFragment", "Attendance data for $currentDate already exists. Skipping upload.")
                        return
                    } else {
                        // Attendance data for the current date does not exist, so perform the upload
                        val currentTime = getCurrentTime()
                        val attendanceRef = userRef.child("Attendance").child(currentDate)
                        val timeStamp = System.currentTimeMillis()

                        // Fetch user's full name from database
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userDataSnapshot: DataSnapshot) {
                                if (userDataSnapshot.exists()) {
                                    val fullName = userDataSnapshot.child("fullName").getValue(String::class.java)

                                    // Create attendance data
                                    val attendanceData = HashMap<String, Any>()
                                    attendanceData["timestamp"] = currentTime.toString()
                                    attendanceData["date"] = currentDate
                                    fullName?.let { attendanceData["fullName"] = it }
                                    attendanceData["rfid"] = binding.rfid.text.toString()
                                    attendanceData["face"] = binding.face.text.toString()
                                    attendanceData["finger"] = binding.fingerprint.text.toString()

                                    // Save attendance data
                                    attendanceRef.setValue(attendanceData).addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            // If the attendance is successfully added, clear the fields in "Pasok"
                                            database.getReference("Pasok/face").setValue("")
                                            database.getReference("Pasok/fingerprint").setValue("")
                                            database.getReference("Pasok/rfid").setValue("")
                                            binding.rfid.text = ""
                                            binding.face.text = ""
                                            binding.fingerprint.text = ""
                                        } else {
                                            // Handle failure if needed
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle onCancelled if needed
                            }
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
        }
    }


    private fun getCurrentTime(): String {
        val tz = TimeZone.getTimeZone("GMT+08:00")
        val c = Calendar.getInstance(tz)
        val hours = String.format("%02d", c.get(Calendar.HOUR))
        val minutes = String.format("%02d", c.get(Calendar.MINUTE))
        return "$hours:$minutes"
    }


    @SuppressLint("SimpleDateFormat")
    private fun getCurrentDate(): String {
        val currentDateObject = Date()
        val formatter = SimpleDateFormat(   "dd-MM-yyyy")
        return formatter.format(currentDateObject)
    }





}