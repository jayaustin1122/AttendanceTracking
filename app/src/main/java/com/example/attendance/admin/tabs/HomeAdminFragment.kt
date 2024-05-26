package com.example.attendance.admin.tabs

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendance.R
import com.example.attendance.admin.tabs.adapter.HomeAdapter
import com.example.attendance.admin.tabs.model.UsersModel
import com.example.attendance.databinding.FragmentHomeAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale


class HomeAdminFragment : Fragment() {
    private lateinit var binding : FragmentHomeAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var accArrayList : ArrayList<UsersModel>

    //adapter
    private lateinit var adapter : HomeAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeAdminBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this@HomeAdminFragment.requireContext())
        progressDialog.setTitle("PLease wait")
        progressDialog.setCanceledOnTouchOutside(false)
        getUsers()
        displayTimeSettings()
        binding.addnewEmployee.setOnClickListener {
            findNavController().navigate(R.id.action_homeAdminFragment_to_signUpFragment)
        }
    }

    private fun getUsers() {

            //initialize
            accArrayList = ArrayList()

            val dbRef = FirebaseDatabase.getInstance().getReference("Users")
            dbRef.addValueEventListener(object : ValueEventListener {
                @SuppressLint("SuspiciousIndentation")
                override fun onDataChange(snapshot: DataSnapshot) {
                    // clear list
                    accArrayList.clear()
                    for (data in snapshot.children){
                        //data as model
                        val model = data.getValue(UsersModel::class.java)
                            accArrayList.add(model!!)

                    }
                    //set up adapter
                    adapter = HomeAdapter(requireContext(), accArrayList,findNavController())
                    //set to recycler
                    binding.recy.setHasFixedSize(true)
                    binding.recy.layoutManager = LinearLayoutManager(context)
                    binding.recy.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {

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