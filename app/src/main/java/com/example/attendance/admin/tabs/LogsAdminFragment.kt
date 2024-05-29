package com.example.attendance.admin.tabs

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.attendance.R
import com.example.attendance.admin.tabs.adapter.AttendanceAdapter
import com.example.attendance.admin.tabs.model.LogsModel
import com.example.attendance.databinding.FragmentLogsAdminBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.io.File
import java.util.Calendar

class LogsAdminFragment : Fragment() {
    private lateinit var binding: FragmentLogsAdminBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var progressDialog: ProgressDialog
    private lateinit var accArrayList: ArrayList<LogsModel>
    private lateinit var adapter: AttendanceAdapter
    private var selectedDateString: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogsAdminBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this@LogsAdminFragment.requireContext())
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
        getRecords()
        binding.imgBtnFilter.setOnClickListener {
            showDatePicker()
        }
        binding.generate.setOnClickListener {
            selectedDateString?.let {
                generatePDF(it)
            } ?: Toast.makeText(context, "Please select a date first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Date")
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedDate ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH) + 1
            val year = calendar.get(Calendar.YEAR)
            selectedDateString = "$day-${month.toString().padStart(2, '0')}-$year"
            filterRecords(selectedDateString!!)
        }

        datePicker.show(requireActivity().supportFragmentManager, "Date Picker")
    }

    private fun filterRecords(selectedDate: String) {
        accArrayList = ArrayList()

        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val attendanceRef = userSnapshot.child("Attendance")
                    for (attendanceSnapshot in attendanceRef.children) {
                        val date = attendanceSnapshot.key
                        if (date == selectedDate) {
                            val model = attendanceSnapshot.getValue(LogsModel::class.java)
                            model?.let { accArrayList.add(it) }
                        }
                    }
                }
                adapter = AttendanceAdapter(accArrayList)
                binding.recy.setHasFixedSize(true)
                binding.recy.layoutManager = LinearLayoutManager(context)
                binding.recy.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getRecords() {
        accArrayList = ArrayList()
        val currentDate = getCurrentDate()

        val dbRef = FirebaseDatabase.getInstance().getReference("Users")
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (userSnapshot in snapshot.children) {
                    val attendanceRef = userSnapshot.child("Attendance")
                    for (attendanceSnapshot in attendanceRef.children) {
                        val date = attendanceSnapshot.key
                        if (date == currentDate) {
                            val model = attendanceSnapshot.getValue(LogsModel::class.java)
                            model?.let { accArrayList.add(it) }
                            break
                        }
                    }
                }
                adapter = AttendanceAdapter(accArrayList)
                binding.recy.setHasFixedSize(true)
                binding.recy.layoutManager = LinearLayoutManager(context)
                binding.recy.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val year = calendar.get(Calendar.YEAR)
        return "$day-${month.toString().padStart(2, '0')}-$year"
    }

    private fun generatePDF(date: String) {
        val filePath = "${requireContext().filesDir}/attendance_$date.pdf"
        val file = File(filePath)
        val pdfWriter = PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument, PageSize.A4)

        document.add(Paragraph("Attendance Records for $date").setBold().setFontSize(20f).setTextAlignment(TextAlignment.CENTER))
        document.add(Paragraph("\n"))

        val table = Table(UnitValue.createPercentArray(floatArrayOf(1f, 3f, 3f, 3f, 3f))).useAllAvailableWidth()
        table.addHeaderCell(Cell().add(Paragraph("ID").setBold()))
        table.addHeaderCell(Cell().add(Paragraph("Name").setBold()))
        table.addHeaderCell(Cell().add(Paragraph("Time In").setBold()))
        table.addHeaderCell(Cell().add(Paragraph("Time Out").setBold()))

        accArrayList.forEach { log ->
            table.addCell(log.id.toString())
            table.addCell(log.fullName)
            table.addCell(log.timestamp)
            table.addCell(log.timeout)
        }

        document.add(table)
        document.close()

        Toast.makeText(context, "PDF Generated at $filePath", Toast.LENGTH_LONG).show()
    }
}
