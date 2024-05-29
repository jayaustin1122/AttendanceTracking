package com.example.attendance.ui

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.attendance.R
import com.example.attendance.admin.tabs.adapter.LogsAdapter
import com.example.attendance.admin.tabs.model.LogsModel
import com.example.attendance.databinding.FragmentDetailsBinding
import com.example.attendance.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UserFragment : Fragment() {
    private var _binding: FragmentUserBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressDialog : ProgressDialog
    private lateinit var logsAdapter: LogsAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentManager: FragmentManager
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }
    @SuppressLint("SimpleDateFormat")
    private fun getCurrentMonth(): String {
        val currentDateObject = Date()
        val formatter = SimpleDateFormat("MMMM")
        return formatter.format(currentDateObject)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        fragmentManager = requireActivity().supportFragmentManager
        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        logsAdapter = LogsAdapter(emptyList()) // Initialize adapter with empty list
        binding.recycler.adapter = logsAdapter

        // Retrieve RFID and fingerprint from arguments
        val uid = auth.uid
        uid?.let { fetchLogsFromDatabase(it) }

        loadUsersInfo()
        displayTimeSettings()
        binding.currentMonth.text = getCurrentMonth()
        binding.btnLogout.setOnClickListener {
            progressDialog.setMessage("Logging Out...")
            progressDialog.show()
            view.postDelayed({
                auth.signOut()
                progressDialog.dismiss()

                findNavController().apply {
                    popBackStack(R.id.settingsAdminFragment, false)
                    navigate(R.id.loginFragment)
                }
            }, 2000)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun loadUsersInfo() {
        //reference
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(auth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get user info
                    val name = "${snapshot.child("fullName").value}"
                    val image = "${snapshot.child("image").value}"

                    //set data
                    binding.name.text = name
                    binding.name.text = name
                    Glide.with(requireContext())
                        .load(image)
                        .into(binding.circleImageView)
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

    }


    private fun fetchLogsFromDatabase(uid: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        val attendanceRef = dbRef.child("Attendance")

        attendanceRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val logsList = ArrayList<LogsModel>()
                for (logSnapshot in dataSnapshot.children) {
                    val timestamp = logSnapshot.child("timestamp").getValue(String::class.java)
                    val timeout = logSnapshot.child("timeout").getValue(String::class.java)
                    val date = logSnapshot.child("date").getValue(String::class.java)

                    if (timestamp != null && timeout != null && date != null) {
                        logsList.add(LogsModel(timestamp, timeout, date))
                    } else {
                        Log.e("DetailsFragment", "One or more values (timestamp, timeout, date) are null")
                    }
                }
                logsAdapter.updateData(logsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DetailsFragment", "DatabaseError: ${error.message}")
                // Handle error
            }
        })
    }

    private fun displayTimeSettings() {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("TimeSettings")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Retrieve time_in and time_out values from dataSnapshot
                val timeIn = dataSnapshot.child("time_in").getValue(String::class.java)
                val timeOut = dataSnapshot.child("time_out").getValue(String::class.java)

                // Convert time from 24-hour format to 12-hour format
                val timeIn12Hour = convertTo12HourFormat(timeIn)
                val timeOut12Hour = convertTo12HourFormat(timeOut)

                // Display the converted values in TextViews
                // Assuming you have TextViews with IDs timeInTextView and timeOutTextView
                binding.tvTimeInLimit.text = "Time In: $timeIn12Hour"
                binding.tvTimeOutLimit.text = "Time Out: $timeOut12Hour"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun convertTo12HourFormat(time: String?): String {
        val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf24.parse(time ?: "00:00")
        return sdf12.format(date)
    }

}