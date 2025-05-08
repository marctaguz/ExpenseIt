package com.example.expenseit.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PageHeader(
    title: String,
    leftActionButtonVisible: Boolean = false,
    rightActionButtonVisible: Boolean = false,
    onLeftAction: () -> Unit = {},
    onRightAction: () -> Unit = {},
    leftActionIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White
        )
    },
    rightActionIcon: @Composable () -> Unit = {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color.White
        )
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .height(75.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (leftActionButtonVisible) {
            IconButton(onClick = onLeftAction) {
                leftActionIcon()
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Text(
            text = title,
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        if (rightActionButtonVisible) {
            IconButton(onClick = onRightAction) {
                rightActionIcon()
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}