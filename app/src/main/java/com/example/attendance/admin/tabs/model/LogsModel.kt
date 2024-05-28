package com.example.attendance.admin.tabs.model

data class LogsModel (
    var timestamp: String = "",
    var timeout: String = "",
    var date: String = "",
    var fullName: String = ""



){
    constructor() : this("", "", "","") {
        // Default constructor required for Firebase
    }
}