package com.example.expenseit.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expenseit.data.local.entities.ReceiptItem

@Composable
fun ReceiptItemCard(
    item: ReceiptItem,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onDoneEditing: (ReceiptItem) -> Unit,
    onDelete: () -> Unit) {
    var editedItemName by remember { mutableStateOf(item.itemName) }
    var editedQuantity by remember { mutableStateOf(item.quantity.toString()) }
    var editedPrice by remember { mutableStateOf(item.price.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        if (isEditing) {
            // Edit Mode Layout
            Column {
                // Item Name
                CustomTextField(
                    value = editedItemName,
                    onValueChange = { editedItemName = it },
                    label = "Item Name",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity & Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = editedQuantity,
                        onValueChange = { editedQuantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = editedPrice,
                        onValueChange = { editedPrice = it },
                        label = { Text("Price") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val updatedItem = item.copy(
                                itemName = editedItemName,
                                quantity = editedQuantity.toIntOrNull() ?: item.quantity,
                                price = editedPrice.toDoubleOrNull() ?: item.price
                            )
                            onDoneEditing(updatedItem) // Pass updated item back
                        }
                    ) {
                        Text(text = "Done")
                    }
                }
            }
        } else {
            // View Mode Layout
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row {
                        Text(
                            text = "${if (item.quantity.toDouble() == item.quantity.toDouble()) item.quantity else item.quantity} × ",
                            color = Color.Gray
                        )
                        Text(
                            text = "$${String.format("%.2f", item.price)}",
                            color = Color.Gray
                        )
                    }
                }

                Text(
                    text = "$${String.format("%.2f", item.quantity * item.price)}",
                    modifier = Modifier.width(56.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { onEditClick() }) { // Only one item can enter edit mode
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE5E7EB))
}