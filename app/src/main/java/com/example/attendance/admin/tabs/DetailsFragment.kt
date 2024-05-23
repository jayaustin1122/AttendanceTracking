package com.example.attendance.admin.tabs

import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.attendance.R
import com.example.attendance.admin.tabs.model.LogsModel
import com.example.attendance.databinding.FragmentDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailsFragment : Fragment() {
   private lateinit var binding : FragmentDetailsBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        val fingerPrint = arguments?.getString("fingerPrint")
        val rfid = arguments?.getString("rfid")
        retrieveData(fingerPrint,rfid)
    }

    private fun retrieveData(fingerPrint: String?, rfid: String?) {
        val query = database.child("Logs")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val datesList = mutableListOf<String>()

                for (logSnapshot in dataSnapshot.children) {
                    val log = logSnapshot.getValue(LogsModel::class.java)
                    if (log?.fingerPrint == fingerPrint || log?.rfid == rfid) {
                        // Extract the date from the retrieved data
                        log?.date?.let { datesList.add(it) }
                    }
                }


            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }





}