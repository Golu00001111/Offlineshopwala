package com.example.offlineshop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.offlineshop.data.Sale
import com.example.offlineshop.data.SaleItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesHistoryScreen(viewModel: InventoryViewModel) {
    val sales by viewModel.sales.collectAsState()

    if (sales.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No sales recorded yet.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sales, key = { it.id }) { sale ->
            SaleRow(sale = sale, viewModel = viewModel)
        }
    }
}

@Composable
private fun SaleRow(sale: Sale, viewModel: InventoryViewModel) {
    var expanded by remember { mutableStateOf(false) }
    var saleItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy  h:mm a", Locale.getDefault()) }

    LaunchedEffect(expanded) {
        if (expanded && !loaded) {
            saleItems = viewModel.getSaleItems(sale.id)
            loaded = true
        }
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(dateFormat.format(Date(sale.timestamp)), style = MaterialTheme.typography.bodyMedium)
                Text("$${"%.2f".format(sale.total)}", style = MaterialTheme.typography.titleMedium)
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                if (!loaded) {
                    Text("Loading items...", style = MaterialTheme.typography.bodySmall)
                } else {
                    saleItems.forEach { item ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${item.productName} x${item.quantity}", style = MaterialTheme.typography.bodySmall)
                            Text("$${"%.2f".format(item.priceEach * item.quantity)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
