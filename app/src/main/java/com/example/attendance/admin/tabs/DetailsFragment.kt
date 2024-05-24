package com.example.attendance.admin.tabs
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.attendance.admin.tabs.adapter.LogsAdapter
import com.example.attendance.databinding.FragmentDetailsBinding
import com.example.attendance.admin.tabs.model.LogsModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class DetailsFragment : Fragment() {

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var logsAdapter: LogsAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var fragmentManager: FragmentManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        fragmentManager = requireActivity().supportFragmentManager
        binding.recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        logsAdapter = LogsAdapter(emptyList()) // Initialize adapter with empty list
        binding.recycler.adapter = logsAdapter

        // Retrieve RFID and fingerprint from arguments
        val uid = arguments?.getString("uid")
        val image = arguments?.getString("image")
        val fullName = arguments?.getString("fullName")
        uid?.let { fetchLogsFromDatabase(it) }
        binding.name.text = fullName
        Glide.with(requireContext())
            .load(image)
            .into(binding.circleImageView)


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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



}
