package com.example.expenseit.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expenseit.data.local.entities.ReceiptItem
import com.example.expenseit.ui.viewmodels.SettingsViewModel
import java.math.BigDecimal

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
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val currency by settingsViewModel.currency.collectAsStateWithLifecycle()


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
                    CustomNumberField(
                        value = editedQuantity,
                        onValueChange = { editedQuantity = it },
                        label = "Quantity",
                        weightModifier = Modifier.weight(1f),
                        isDecimal = false
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    CustomNumberField(
                        value = editedPrice,
                        onValueChange = { editedPrice = it },
                        label = "Price",
                        weightModifier = Modifier.weight(3f),
                        isDecimal = true
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
                            if (editedItemName.isNotBlank() && editedQuantity.isNotBlank() && editedPrice.isNotBlank()) {
                                onDoneEditing(
                                    item.copy(
                                        itemName = editedItemName,
                                        quantity = editedQuantity.toIntOrNull() ?: item.quantity,
                                        price = editedPrice.toBigDecimalOrNull() ?: item.price
                                    )
                                )
                            }
                        }
                    ) {
                        Text(text = "Done")
                    }
                }
            }
        } else {
            // View Mode Layout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(3f)
                ) {
                    Text(
                        text = item.itemName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row {
                        // Quantity & Price Row
                        Text(
                            text = "${if (item.quantity.toDouble() == item.quantity.toDouble()) item.quantity else item.quantity} × ",
                            color = Color.Gray
                        )
                        Text(
                            text = "$currency${String.format("%.2f", item.price)}",
                            color = Color.Gray
                        )
                    }
                }

                // Total Price
                Text(
                    text = "$currency${String.format("%.2f", item.quantity * item.price.toDouble())}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )

                // Edit and Delete Buttons
                Row(
                    modifier = Modifier.width(80.dp), // Constrain the width of the buttons
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { onEditClick() },
                        modifier = Modifier.padding(0.dp) // Remove padding around the button
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp) // Reduce icon size
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.padding(0.dp) // Remove padding around the button
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp) // Reduce icon size
                        )
                    }
                }
            }
        }
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE5E7EB))
}