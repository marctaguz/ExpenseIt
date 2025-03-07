package com.example.expenseit.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun CustomNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val filteredInput = if (isDecimal) {
                // Allow digits and a single decimal point
                input.filterIndexed { index, c ->
                    c.isDigit() || (c == '.' && input.count { it == '.' } <= 1)
                }.let { filtered ->
                    // Ensure that the input doesn't start with a decimal point
                    if (filtered.startsWith(".")) {
                        "0$filtered"
                    } else {
                        filtered
                    }
                }
            } else {
                // Allow only digits
                input.filter { it.isDigit() }
            }
            onValueChange(filteredInput)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        modifier = modifier.fillMaxWidth()
    )
}
