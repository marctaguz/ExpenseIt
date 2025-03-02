package com.example.expenseit.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Match API response
    private val outputDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) // App display format

    private val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    fun dateStringToLong(dateString: String?): Long? {
        return try {
            dateString?.let {
                apiDateFormat.parse(it)?.time
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    fun longToDateString(timestamp: Long?): String {
        return timestamp?.let {
            outputDateFormat.format(Date(it)) // Convert Long to "dd/MM/yyyy"
        } ?: ""
    }
}