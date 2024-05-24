package com.example.attendance.admin.tabs.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.attendance.R
import com.example.attendance.admin.tabs.model.LogsModel
import com.example.attendance.databinding.ItemRowUserDataBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
class LogsAdapter(private var logList: List<LogsModel>) : RecyclerView.Adapter<LogsAdapter.LogViewHolder>() {

    class LogViewHolder(val binding: ItemRowUserDataBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemRowUserDataBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]
        holder.binding.tvDate.text = log.date
        val timeIn = log.timestamp

        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val date = dateFormat.parse(log.date)

        // Format the date to display the day of the week
        val dayOfWeekFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val dayOfWeek = dayOfWeekFormat.format(date)

        // Set the day of the week in the TextView
        holder.binding.tvDay.text = dayOfWeek

        // Parse the timestamp to get the hour of the day
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val time = timeFormat.parse(timeIn)
        val calendar = Calendar.getInstance()
        calendar.time = time
        val timeOut = log.timeOut
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

        // Set the background color based on the time
        when {
            hourOfDay in 8..15 -> holder.binding.card.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.red)
            )
            hourOfDay >= 16 -> holder.binding.card.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.green)
            )
            else -> holder.binding.card.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.blue)
            )
        }

        // Set OnClickListener for itemView
        holder.itemView.setOnClickListener {
            // Inflate the dialog layout
            val dialogView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.dialog_layout, null)

            // Initialize views in the dialog
            val tvTimeIn = dialogView.findViewById<TextView>(R.id.tvTimeIn)
            val tvTimeOut = dialogView.findViewById<TextView>(R.id.tvTimeOut)
            val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)
            val tvLateNote = dialogView.findViewById<TextView>(R.id.tvLateNote)

            // Set details in the dialog
            tvTimeIn.text = "Time In: $timeIn"
            tvTimeOut.text = "Time Out: $timeOut"

            tvDate.text = "Date: ${log.date}"
            tvLateNote.text = if (hourOfDay > 9) "Late" else "On Time"

            // Create and show the dialog
            val dialogBuilder = AlertDialog.Builder(holder.itemView.context)
                .setView(dialogView)
                .setPositiveButton("Close") { dialog, _ ->
                    dialog.dismiss()
                }
            dialogBuilder.create().show()
        }
    }



    override fun getItemCount(): Int {
        return logList.size
    }

    fun updateData(newLogsList: List<LogsModel>) {
        logList = newLogsList
        notifyDataSetChanged()
    }
}
