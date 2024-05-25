package com.example.attendance.admin.tabs.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.attendance.R
import com.example.attendance.admin.tabs.model.LogsModel
import com.example.attendance.databinding.ItemRowLogsAttendanceBinding
import com.example.attendance.databinding.ItemRowUserDataBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
class AttendanceAdapter(private var logList: List<LogsModel>) : RecyclerView.Adapter<AttendanceAdapter.LogViewHolder>() {

    class LogViewHolder(val binding: ItemRowLogsAttendanceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemRowLogsAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]
        val timeIn = log.timestamp
        val timeOut = log.timeout
        var fullName = log.fullName

        holder.binding.tvName.text = fullName
        holder.binding.timeIn.text = timeIn
        holder.binding.timeOut.text = timeOut



    }



    override fun getItemCount(): Int {
        return logList.size
    }

    fun updateData(newLogsList: List<LogsModel>) {
        logList = newLogsList
        notifyDataSetChanged()
    }
}
