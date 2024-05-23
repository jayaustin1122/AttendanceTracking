package com.example.attendance.admin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.attendance.admin.tabs.model.LogsModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LogsViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance().reference
    private val _logs = MutableLiveData<List<LogsModel>>()
    val logs: LiveData<List<LogsModel>> get() = _logs

    fun fetchLogs(rfid: String, fingerPrint: String) {
        database.child("Logs").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val logList = mutableListOf<LogsModel>()
                for (logSnapshot in dataSnapshot.children) {
                    val logData = logSnapshot.getValue(LogsModel::class.java)
                    if (logData != null && logData.rfid == rfid && logData.fingerPrint == fingerPrint) {
                        logList.add(logData)
                    }
                }
                _logs.value = logList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle possible errors.
            }
        })
    }
}
