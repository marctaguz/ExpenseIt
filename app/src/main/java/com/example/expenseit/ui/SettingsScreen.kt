import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.expenseit.ui.components.PageHeader
import com.example.expenseit.ui.viewmodels.SettingsViewModel

@Composable
fun SettingsScreen(navController: NavController, modifier: Modifier = Modifier) {
    var selectedCurrency by remember { mutableStateOf("$") }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    val settingsViewModel: SettingsViewModel = hiltViewModel()

    // Coroutine scope for async operations
    val scope = rememberCoroutineScope()

    // Observe the currency from the ViewModel
    val currency by settingsViewModel.currency.collectAsStateWithLifecycle()

    LaunchedEffect(currency) {
        selectedCurrency = currency
    }

    Scaffold(
        topBar = { PageHeader(title = "Settings Screen", actionButtonVisible = false) },
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(innerPadding).padding(16.dp)
            ) {

                // Currency setting section
                Text("Currency", style = MaterialTheme.typography.titleLarge)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCurrencyDialog = true }
                        .padding(8.dp)
                ) {
                    Text(
                        text = selectedCurrency,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }

                Text("Category", style = MaterialTheme.typography.titleLarge)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("edit_category") }
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Edit Category",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
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
    )
}

@Composable
fun CurrencySelectionDialog(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currencies = listOf("$", "€", "£", "¥") // Add other currencies as needed
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
