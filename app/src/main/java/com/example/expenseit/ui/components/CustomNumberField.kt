package com.example.expenseit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign

@Composable
fun CustomNumberField(
    modifier: Modifier = Modifier,
    weightModifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isDecimal: Boolean = false
) {
    val isValid = value.isNotEmpty() && value != "." // Ensure input is valid
    val textColor = if (isValid) Color(0xFF374151) else Color.Red

    Column(modifier = weightModifier) {
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = textColor,
        )

        OutlinedTextField(
            modifier = modifier,
            value = value,
            onValueChange = { input ->
                val filteredInput = if (isDecimal) {
                    input.filterIndexed { index, c ->
                        c.isDigit() || (c == '.' && input.count { it == '.' } <= 1)
                    }.let { filtered ->
                        when {
                            filtered.startsWith(".") -> "0$filtered"
                            else -> filtered
                        }
                    }
                } else {
                    input.filter { it.isDigit() }
                }

                onValueChange(filteredInput)
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
            ),
            colors = TextFieldDefaults.colors().copy(
                unfocusedIndicatorColor = if (isValid) Color(0xFFE5E7EB) else Color.Red,
                focusedIndicatorColor = if (isValid) Color(0xFF015FCC) else Color.Red,
                unfocusedContainerColor = White,
                focusedContainerColor = White
            )
        )
    }
}

