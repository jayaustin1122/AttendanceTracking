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
import com.example.attendance.admin.tabs.adapter.AttendanceAdapter
import com.example.attendance.admin.tabs.adapter.HomeAdapter
import com.example.attendance.admin.tabs.model.LogsModel
import com.example.attendance.admin.tabs.model.UsersModel
import com.example.attendance.databinding.FragmentLogsAdminBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class LogsAdminFragment : Fragment() {
    private lateinit var binding : FragmentLogsAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    // array list to hold events
    private lateinit var accArrayList : ArrayList<LogsModel>

    //adapter
    private lateinit var adapter : AttendanceAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogsAdminBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this@LogsAdminFragment.requireContext())
        progressDialog.setTitle("PLease wait")
        progressDialog.setCanceledOnTouchOutside(false)
        getRecords()
    }

    private fun getRecords() {
        val accArrayList = ArrayList<LogsModel>()
        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val attendanceRef = userSnapshot.child("Attendance")
                    for (attendanceSnapshot in attendanceRef.children) {
                        val model = attendanceSnapshot.getValue(LogsModel::class.java)
                        model?.let { accArrayList.add(it) }
                    }
                }
                adapter = AttendanceAdapter(accArrayList)
                binding.recy.setHasFixedSize(true)
                binding.recy.layoutManager = LinearLayoutManager(context)
                binding.recy.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }
}