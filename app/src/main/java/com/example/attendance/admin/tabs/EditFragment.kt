package com.example.attendance.admin.tabs
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.attendance.R
import com.google.firebase.database.FirebaseDatabase
import com.example.attendance.databinding.FragmentEditBinding
import java.util.*

class EditFragment : Fragment() {
    private lateinit var binding: FragmentEditBinding
    private lateinit var btnTimeIn: Button
    private lateinit var btnTimeOut: Button
    private lateinit var tvTimeInLimit: TextView
    private lateinit var tvTimeOutLimit: TextView
    private lateinit var btnUpdate: Button

    private lateinit var calendar: Calendar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditBinding.inflate(layoutInflater)
        val view = binding.root

        // Initialize views
        btnTimeIn = view.findViewById(R.id.btnTimeIn)
        btnTimeOut = view.findViewById(R.id.btnTimeOut)
        tvTimeInLimit = view.findViewById(R.id.tvTimeInLimit)
        tvTimeOutLimit = view.findViewById(R.id.tvTimeOutLimit)
        btnUpdate = view.findViewById(R.id.btnUpdate)

        calendar = Calendar.getInstance()

        // Set click listeners
        btnTimeIn.setOnClickListener {
            showTimePickerDialog(tvTimeInLimit)
        }

        btnTimeOut.setOnClickListener {
            showTimePickerDialog(tvTimeOutLimit)
        }

        btnUpdate.setOnClickListener {
            val timeIn = tvTimeInLimit.text.toString()
            val timeOut = tvTimeOutLimit.text.toString()

            if (timeIn.isNotEmpty() && timeOut.isNotEmpty()) {
                uploadToDatabase(timeIn, timeOut)
            } else {
                // Notify the user to select both times
            }
        }

        return view
    }

    private fun showTimePickerDialog(textView: TextView) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minuteOfDay ->
                val selectedTime = "$hourOfDay:$minuteOfDay"
                textView.text = selectedTime
            },
            hour,
            minute,
            true
        )

        timePickerDialog.show()
    }

    private fun uploadToDatabase(timeIn: String, timeOut: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference.child("TimeSettings")
        if (tvTimeInLimit.text == "" || tvTimeOutLimit.text == ""){
            return
        }
        else{
            databaseReference.child("time_in").setValue(timeIn)
            databaseReference.child("time_out").setValue(timeOut)
            Toast.makeText(requireContext(),"Success!!",Toast.LENGTH_SHORT).show()
            findNavController().apply {
                popBackStack(R.id.editFragment, false) // Pop all fragments up to HomeFragment
                navigate(R.id.adminNavFragment) // Navigate to LoginFragment
            }
        }

        // Notify the user that the data is updated successfully
    }
}
