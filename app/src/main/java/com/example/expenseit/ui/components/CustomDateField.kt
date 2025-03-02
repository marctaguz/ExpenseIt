package com.example.expenseit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CustomDateField(
    label: String,
    date: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showModal by remember { mutableStateOf(false) }
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(date))

    Column() {
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color(0xFF374151),
        )

        OutlinedTextField(
            value = formattedDate,
            onValueChange = { },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showModal = true }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            modifier = modifier,
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors().copy(
                unfocusedIndicatorColor = Color(0xFFE5E7EB),
                focusedIndicatorColor = Color(0xFF015FCC),
                unfocusedContainerColor = White,
                focusedContainerColor = White
            )
        )
    }

    if (showModal) {
        DatePickerModal(
            initialDate = date,
            onDateSelected = {
                onDateSelected(it ?: System.currentTimeMillis())
                showModal = false
            },
            onDismiss = { showModal = false }
        )
    }
}