package com.example.expenseit.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expenseit.ui.theme.LightPrimary
import com.example.expenseit.ui.theme.Purple80

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {

    var text by remember { mutableStateOf("") }

    Column() {
        Text(
            text = label,
            textAlign = TextAlign.Start,
            color = Color(0xFF374151),
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
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
}