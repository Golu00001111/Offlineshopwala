package com.example.offlineshop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.offlineshop.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    val products by viewModel.products.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingProduct = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add product")
            }
        }
    ) { padding ->
        if (products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No products yet. Tap + to add your first item.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products, key = { it.id }) { product ->
                    ProductRow(
                        product = product,
                        onEdit = {
                            editingProduct = product
                            showDialog = true
                        },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
            }
        }
    }

    if (showDialog) {
        ProductEditDialog(
            initial = editingProduct,
            onDismiss = { showDialog = false },
            onSave = { name, sku, price, qty ->
                if (editingProduct != null) {
                    viewModel.updateProduct(
                        editingProduct!!.copy(name = name, sku = sku, price = price, quantity = qty)
                    )
                } else {
                    viewModel.addProduct(name, sku, price, qty)
                }
                showDialog = false
            }
        )
    }
}

@Composable
private fun ProductRow(product: Product, onEdit: () -> Unit, onDelete: () -> Unit) {
    val lowStock = product.quantity <= 5
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text("SKU: ${product.sku}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Price: $${"%.2f".format(product.price)}  •  Stock: ${product.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (lowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (lowStock) {
                    Text("Low stock", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}

@Composable
private fun ProductEditDialog(
    initial: Product?,
    onDismiss: () -> Unit,
    onSave: (name: String, sku: String, price: Double, quantity: Int) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var sku by remember { mutableStateOf(initial?.sku ?: "") }
    var price by remember { mutableStateOf(initial?.price?.toString() ?: "") }
    var quantity by remember { mutableStateOf(initial?.quantity?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Product" else "Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true)
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU / Code") }, singleLine = true)
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, singleLine = true)
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity in stock") }, singleLine = true)
                error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val priceVal = price.toDoubleOrNull()
                val qtyVal = quantity.toIntOrNull()
                when {
                    name.isBlank() -> error = "Name is required"
                    priceVal == null || priceVal < 0 -> error = "Enter a valid price"
                    qtyVal == null || qtyVal < 0 -> error = "Enter a valid quantity"
                    else -> onSave(name.trim(), sku.trim(), priceVal, qtyVal)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
