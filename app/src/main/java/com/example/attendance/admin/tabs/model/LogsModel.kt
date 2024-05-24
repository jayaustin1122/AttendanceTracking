package com.example.attendance.admin.tabs.model

data class LogsModel (
    val timestamp: String = "",
    val timeOut: String = "",
    val date: String = ""


){
    constructor() : this("", "", "") {
        // Default constructor required for Firebase
    }
}