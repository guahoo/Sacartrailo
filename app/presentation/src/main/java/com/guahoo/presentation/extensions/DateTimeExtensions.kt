package com.guahoo.presentation.extensions

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateToMonthDay(timeInMillis: Long): String {
    val date = Date(timeInMillis * 1000)
    val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return formatter.format(date)
}

fun formatTime(timeInMillis: Long): String {
    val date = Date(timeInMillis * 1000)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}

fun formatTimeToHours(timeInMillis: Long): String {
    return "${timeInMillis / 60 / 60 } H"
}