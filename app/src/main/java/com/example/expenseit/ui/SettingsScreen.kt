import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.theme.backgroundLight
import com.example.expenseit.ui.viewmodels.SettingsViewModel
import com.example.expenseit.R
import com.example.expenseit.ui.viewmodels.CategoryViewModel

@Composable
fun SettingsScreen(navController: NavController, modifier: Modifier = Modifier) {
    var selectedCurrency by remember { mutableStateOf("$") }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val headerHeight = 160.dp
    val overlapHeight = 86.dp
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val categoryViewModel: CategoryViewModel = hiltViewModel()

    //observe currency from vm
    val currency by settingsViewModel.currency.collectAsStateWithLifecycle()
    val categoryCount by categoryViewModel.categoryCount.collectAsStateWithLifecycle()

    LaunchedEffect(currency) {
        selectedCurrency = currency
    }

    Scaffold(
        content = { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxWidth()
                        .height(headerHeight)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    PageHeader(title = "Settings")
                }

                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = headerHeight - overlapHeight)
                        .clip(RoundedCornerShape(topStart = 34.dp, topEnd = 34.dp)),
                    colors = CardDefaults.cardColors(backgroundLight),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        Text("Preferences")
                        Card(
                            modifier = Modifier
                                .padding(top = 6.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Card (
                                Modifier.clickable() { showCurrencyDialog = true },
                                colors = CardDefaults.cardColors(containerColor = Color.White)

                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_currency),
                                        contentDescription = null,
                                        tint = Color(0xFF6b7280)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text("Currency", modifier = Modifier.weight(1f))
                                    Text(
                                        text = selectedCurrency,
                                        color = Color(0xFF6b7280),
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF6b7280)
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.alpha(1F), color = backgroundLight)
                            Card(
                                Modifier.clickable() { navController.navigate("edit_category") },
                                colors = CardDefaults.cardColors(containerColor = Color.White)

                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_category),
                                        contentDescription = null,
                                        tint = Color(0xFF6b7280)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text("Categories", Modifier.weight(1f))
                                    Text(
                                        text = categoryCount.toString(),
                                        color = Color(0xFF6b7280),
                                        modifier = Modifier.padding(horizontal = 6.dp)

                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF6b7280),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))
                        Text("Data & Privacy")
                        Card(
                            modifier = Modifier
                                .padding(top = 6.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Card(
                                Modifier.clickable() {},
                                colors = CardDefaults.cardColors(containerColor = Color.White)

                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_download),
                                        contentDescription = null,
                                        tint = Color(0xFF6b7280)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text("Export Data", modifier = Modifier.weight(1f))
                                    Text(
                                        text = "",
                                        color = Color(0xFF6b7280),
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF6b7280)
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.alpha(1F), color = backgroundLight)
                            Card(
                                Modifier.clickable() {},
                                colors = CardDefaults.cardColors(containerColor = Color.White)

                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_privacy),
                                        contentDescription = null,
                                        tint = Color(0xFF6b7280)
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))

                                    Text("Privacy Settings", Modifier.weight(1f))
                                    Text(
                                        text = "",
                                        color = Color(0xFF6b7280),
                                        modifier = Modifier.padding(horizontal = 6.dp)

                                    )
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowRight,
                                        contentDescription = null,
                                        tint = Color(0xFF6b7280),
                                    )
                                }
                            }
                        }

                        if (showCurrencyDialog) {
                            CurrencySelectionDialog(
                                selectedCurrency = selectedCurrency,
                                onCurrencySelected = { currency ->
                                    settingsViewModel.setCurrency(currency) // Update currency in ViewModel
                                    selectedCurrency = currency
                                    showCurrencyDialog = false
                                },
                                onDismiss = { showCurrencyDialog = false }
                            )
                        }
                    }
                }
            }

        }
    )
}

@Composable
fun CurrencySelectionDialog(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currencies = listOf("$", "€", "£", "¥")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                currencies.forEach { currency ->
                    TextButton(onClick = { onCurrencySelected(currency) }) {
                        Text(currency)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
