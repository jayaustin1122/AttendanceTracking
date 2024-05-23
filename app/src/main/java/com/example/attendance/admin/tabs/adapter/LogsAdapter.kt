package com.example.attendance.admin.tabs.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.admin.tabs.model.LogsModel
import com.example.attendance.databinding.ItemRowUserDataBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class LogsAdapter(private val logList: List<LogsModel>) : RecyclerView.Adapter<LogsAdapter.LogViewHolder>() {

    class LogViewHolder(val binding: ItemRowUserDataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemRowUserDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]
        holder.binding.tvDate.text = log.date

        // Convert date string to a Date object
        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
        val date: Date? = dateFormat.parse(log.date)
        val calendar = Calendar.getInstance()
        if (date != null) {
            calendar.time = date
        }

        // Get day of the week
        val dayOfWeekString = SimpleDateFormat("EEEE", Locale.US).format(date)
        holder.binding.tvDay.text = dayOfWeekString
    }

    override fun getItemCount(): Int {
        return logList.size
    }
}

