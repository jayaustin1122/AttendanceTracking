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

                // Once you have the list of dates, initialize your calendar view
                setupCalendarView(datesList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }
    private fun setupCalendarView(datesList: List<String>) {
        val events = mutableMapOf<Calendar, List<LogsModel>>()

        // Convert datesList to a map of Calendar to corresponding data
        datesList.forEach { dateString ->
            val dateParts = dateString.split("/")
            val day = dateParts[1].toInt()
            val month = dateParts[0].toInt() - 1 // Month is 0-based in Calendar
            val year = dateParts[2].toInt()
            val calendar = Calendar.getInstance().apply {
                set(year, month, day)
            }

            // Retrieve data from Firebase for the current date
            retrieveDataForDate(dateString) { logList ->
                events[calendar] = logList
                // Add markers for events on specific dates
                binding.calendarView.addDecorator(EventDecorator(Color.RED, calendar))
            }
        }

        // Handle date selection
        binding.calendarView.setOnDateChangedListener { _, selectedDate, _ ->
            // Handle date selection here
            val calendar = Calendar.getInstance().apply {
                time = selectedDate
            }
            val selectedLogs = events[calendar]
            // Update UI with the selectedLogs
            updateUI(selectedLogs)
        }
    }

    private fun retrieveDataForDate(dateString: String, callback: (List<LogsModel>) -> Unit) {
        val logList = mutableListOf<LogsModel>()

        // Retrieve data from Firebase for the specified date
        val query = database.child("Logs").orderByChild("date").equalTo(dateString)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (logSnapshot in dataSnapshot.children) {
                    val log = logSnapshot.getValue(LogsModel::class.java)
                    log?.let { logList.add(it) }
                }
                callback(logList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun updateUI(logs: List<LogsModel>?) {
        // Update UI with the selected logs
        // For example, display the logs in a RecyclerView
        logs?.let {
            val adapter = (logs)
            binding.recyclerView.adapter = adapter
        }
    }

}