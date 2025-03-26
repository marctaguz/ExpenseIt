package com.example.expenseit.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Match API response
    private val outputDateFormat = SimpleDateFormat("MMM dd", Locale.getDefault()) // App display format

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

    fun formatDateForChart(dateInMillis: Long): String {
        val dateFormatter = SimpleDateFormat("dd-MMM", Locale.getDefault())
        return dateFormatter.format(Date(dateInMillis))
    }

    fun formatMonthForChart(yearMonth: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM", Locale.getDefault())  // ✅ "Mar", "Apr"
        return try {
            val date = inputFormat.parse(yearMonth)
            date?.let { outputFormat.format(it) } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    fun longToDateString(timestamp: Long?): String {
        return timestamp?.let {
            outputDateFormat.format(Date(it)) // Convert Long to "dd/MM/yyyy"
        } ?: ""
    }
}