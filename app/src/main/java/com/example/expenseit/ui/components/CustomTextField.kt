package com.example.expenseit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    readOnly: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
    validator: (String) -> Boolean = { it.isNotBlank() } // Default: Ensure input is not empty
) {
    val isValid = validator(value)
    val textColor = if (isValid) Color(0xFF374151) else Color.Red

    Column {
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = textColor,
        )

        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            modifier = modifier,
            readOnly = readOnly,
            shape = RoundedCornerShape(8.dp),
            trailingIcon = trailingIcon,
            colors = TextFieldDefaults.colors().copy(
                unfocusedIndicatorColor = if (isValid) Color(0xFFE5E7EB) else Color.Red, // Red if invalid
                focusedIndicatorColor = if (isValid) Color(0xFF015FCC) else Color.Red,  // Red border when focused if invalid
                unfocusedContainerColor = White,
                focusedContainerColor = White
            )
        )
    }
}
