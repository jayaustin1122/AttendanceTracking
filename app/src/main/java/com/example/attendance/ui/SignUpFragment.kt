package com.example.attendance.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.attendance.R
import com.example.attendance.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var auth : FirebaseAuth
    private lateinit var storage : FirebaseStorage
    private lateinit var database : FirebaseDatabase
    private lateinit var selectedImage : Uri
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        database = FirebaseDatabase.getInstance()
        database.getReference("RegisterState").setValue("True")
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        handler.post(fetchRFIDDataRunnable)

        binding.imageView2.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent,1)
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.adminNavFragment)
            database.getReference("RegisterState").setValue("False")
        }

        binding.btnSignUp.setOnClickListener {
            validateData()
        }




    }
    private val handler = Handler()

    private val fetchRFIDDataRunnable = object : Runnable {
        override fun run() {
            getRFIDData()
            getFingerPrint()
            handler.postDelayed(this, 5000) // Fetch data every minute (60000 milliseconds)
        }
    }
    private fun getFingerPrint() {
        val uid = auth.uid
        val userRef = database.getReference("Register/fingerprintUID")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rfidData = dataSnapshot.getValue(String::class.java)
                if (rfidData != null) {
                    binding.etFingerPrint.setText(rfidData)
                } else {
                    // Handle the case where RFID data is not available
                    binding.etFingerPrint.setText("FingerPrint Not Found or Damage")
                    Toast.makeText(this@SignUpFragment.requireContext(), "fingerPrint data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "fingerPrint:onCancelled", databaseError.toException())
                // Handle the case where an error occurred while retrieving RFID data
                binding.etRfid.setText("fingerPrint Not Found or Damage")
                Toast.makeText(this@SignUpFragment.requireContext(), "Error retrieving fingerPrint data", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getRFIDData() {
        val uid = auth.uid
        val userRef = database.getReference("Register/rfidUID")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val rfidData = dataSnapshot.getValue(String::class.java)
                if (rfidData != null) {
                    binding.etRfid.setText(rfidData)
                } else {
                    // Handle the case where RFID data is not available
                    binding.etRfid.setText("RFID Not Found or Damage")
                    Toast.makeText(this@SignUpFragment.requireContext(), "RFID data not found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(ContentValues.TAG, "RFID:onCancelled", databaseError.toException())
                // Handle the case where an error occurred while retrieving RFID data
                binding.etRfid.setText("RFID Not Found or Damage")
                Toast.makeText(this@SignUpFragment.requireContext(), "Error retrieving RFID data", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun getCounterId() {
        val userRef = database.getReference("UsersIdCounter")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val counterValue = dataSnapshot.getValue(Int::class.java) ?: 0
                val updatedCounterValue = counterValue + 1

                // Update the counter value in the database
                userRef.setValue(updatedCounterValue)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Update the TextView with the updated counter value
                            binding.id.text = updatedCounterValue.toString()
                            // Proceed with uploading user info

                        } else {
                            // Handle update failure
                            progressDialog.dismiss()
                            Toast.makeText(this@SignUpFragment.requireContext(), "Error updating counter value", Toast.LENGTH_SHORT).show()
                        }
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog.dismiss()
                Toast.makeText(this@SignUpFragment.requireContext(), "Error retrieving counter value", Toast.LENGTH_SHORT).show()
            }
        })
    }




    private fun validateData() {
        val email = binding.etEmailSignUp.text.toString().trim()
        val pass = binding.etPasswordSignUp.text.toString().trim()
        val fullName = binding.etFullname.text.toString().trim()
        val pinCode = binding.etFingerPrint.text.toString().trim()
        val rfid = binding.etRfid.text.toString().trim()

        when {
            email.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Email...", Toast.LENGTH_SHORT).show()
            pass.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Password...", Toast.LENGTH_SHORT).show()
            fullName.isEmpty() -> Toast.makeText(this.requireContext(), "Enter Your Name...", Toast.LENGTH_SHORT).show()
            pinCode.isEmpty() -> Toast.makeText(this.requireContext(), "Scan Finger Print ...", Toast.LENGTH_SHORT).show()
            rfid.isEmpty() -> Toast.makeText(this.requireContext(), "Tap Your Card", Toast.LENGTH_SHORT).show()
            !::selectedImage.isInitialized -> Toast.makeText(this.requireContext(), "Please Upload a Picture", Toast.LENGTH_SHORT).show()
            else -> createUserAccount()
        }
    }

    private fun createUserAccount() {
        val email = binding.etEmailSignUp.text.toString()
        val password = binding.etPasswordSignUp.text.toString()
        progressDialog.setMessage("Creating Account...")
        progressDialog.show()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                auth.createUserWithEmailAndPassword(email,password).await()
                withContext(Dispatchers.Main){
                    uploadImage()
                }

            }
            catch (e : Exception){
                withContext(Dispatchers.Main){
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@SignUpFragment.requireContext(),
                        "Failed Creating Account or ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }
    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onPause() {
        callback.remove()
        super.onPause()
    }
    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            database.getReference("Register").setValue("False")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(fetchRFIDDataRunnable) // Stop fetching when the fragment is destroyed
    }
    private fun uploadImage() {
        progressDialog.setMessage("Uploading Image...")
        progressDialog.show()
        val uid = auth.uid

        val reference = storage.reference.child("profile")
            .child(binding.etFullname.text.toString())
        reference.putFile(selectedImage).addOnCompleteListener{
            if (it.isSuccessful){
                reference.downloadUrl.addOnSuccessListener {task->
                    // Pass the RFID data to uploadInfo
                    uploadInfo(task.toString())

                }
            } else {
                progressDialog.dismiss()
                Toast.makeText(this@SignUpFragment.requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.data != null) {
                    selectedImage = data.data!!
                    binding.imageView2.setImageURI(selectedImage)
                }
            }
        }
    }
    private var email = ""
    private var pass = ""
    private var fullname = ""
    private var pinCode = ""
    private var userType = "member"
    private var rfidData = ""
    private var fingerPrint = ""
    private var pin = ""
    private fun uploadInfo(imageUrl: String) {
        progressDialog.setMessage("Saving Account...")
        progressDialog.show()
        email = binding.etEmailSignUp.text.toString().trim()
        pass = binding.etPasswordSignUp.text.toString().trim()
        fullname = binding.etFullname.text.toString().trim()
        val idCounter = binding.id.text.toString().trim()
        this.rfidData = binding.etRfid.text.toString().trim()
        this.fingerPrint = binding.etFingerPrint.text.toString().trim()
        val currentDate = getCurrentDate()
        val currentTime = getCurrentTime()
        val uid = auth.uid
        val timestamp = System.currentTimeMillis()
        val hashMap : HashMap<String, Any?> = HashMap()
        getCounterId()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["password"] = pass
        hashMap["fullName"] = fullname
        hashMap[fullname] = imageUrl
        hashMap["currentDate"] = currentDate
        hashMap["currentTime"] = currentTime
        hashMap["id"] = fingerPrint
        hashMap["userType"] = "member"
        hashMap["image"] = imageUrl
        hashMap["RFID"] = rfidData
        hashMap["fingerPrint"] = fingerPrint
        hashMap["status"] = true
        hashMap["UsersIdCounter"] = true

        try {
            database.getReference("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .setValue(hashMap)
                .addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        progressDialog.dismiss()
                        uploadInfo2()
                        uploadInfo3()
                        findNavController().apply {
                            popBackStack(R.id.signUpFragment, false) // Pop all fragments up to HomeFragment
                            navigate(R.id.loginFragment) // Navigate to LoginFragment
                            database.getReference("RegisterState").setValue("False")
                        }
                        Toast.makeText(this.requireContext(),"Account Created", Toast.LENGTH_SHORT).show()
                        database.getReference("RFID").setValue("")
                        database.getReference("Register/fingerprintUID").setValue("")
                        database.getReference("Register/rfidUID").setValue("")


                    } else {
                        Toast.makeText(this.requireContext(), task.exception!!.message, Toast.LENGTH_SHORT).show()
                    }
                }
        } catch (e: Exception) {
            // Handle any exceptions that might occur during the upload process.
            progressDialog.dismiss()
            Toast.makeText(this.requireContext(), "Error uploading data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadInfo3() {

        this.pin = binding.etFingerPrint.text.toString().trim()
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["$pin"] = pin

        try {
            database.getReference("RegisteredFingerPrint").child(pin)
                .setValue(hashMap)
                .addOnCompleteListener { task ->

                }
        } catch (e: Exception) {
            // Handle any exceptions that might occur during the upload process.
            progressDialog.dismiss()
            Toast.makeText(
                this.requireContext(),
                "Error uploading data: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private fun uploadInfo2() {

        this.pin = binding.etRfid.text.toString().trim()
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["$pin"] = pin

        try {
            database.getReference("RegisteredRFID").child(pin)
                .setValue(hashMap)
                .addOnCompleteListener { task ->

                }
        } catch (e: Exception) {
            // Handle any exceptions that might occur during the upload process.
            progressDialog.dismiss()
            Toast.makeText(
                this.requireContext(),
                "Error uploading data: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
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
