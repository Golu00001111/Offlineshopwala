package com.example.offlineshop.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.offlineshop.data.CartItem
import com.example.offlineshop.data.Product

@Composable
fun SellScreen(viewModel: InventoryViewModel) {
    val products by viewModel.products.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val checkoutMessage by viewModel.checkoutMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(checkoutMessage) {
        checkoutMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearCheckoutMessage()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Text("Add products in the Inventory tab before making a sale.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        val inCart = cart.find { it.product.id == product.id }?.quantity ?: 0
                        SellProductRow(
                            product = product,
                            quantityInCart = inCart,
                            onAdd = { viewModel.addToCart(product, 1) },
                            onRemove = { viewModel.decrementInCart(product) }
                        )
                    }
                }
            }

            CartSummary(
                cart = cart,
                total = viewModel.cartTotal(),
                onCheckout = { viewModel.checkout() }
            )
        }
    }
}

@Composable
private fun SellProductRow(
    product: Product,
    quantityInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    val outOfStock = product.quantity <= quantityInCart
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "$${"%.2f".format(product.price)}  •  ${product.quantity} in stock",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onRemove, enabled = quantityInCart > 0) {
                Icon(Icons.Default.Remove, contentDescription = "Remove one")
            }
            Text("$quantityInCart", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onAdd, enabled = !outOfStock) {
                Icon(Icons.Default.Add, contentDescription = "Add one")
            }
        }
    }
}

@Composable
private fun CartSummary(cart: List<CartItem>, total: Double, onCheckout: () -> Unit) {
    Surface(tonalElevation = 4.dp) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Items: ${cart.sumOf { it.quantity }}", style = MaterialTheme.typography.bodyMedium)
                Text("Total: $${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onCheckout,
                enabled = cart.isNotEmpty(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Complete Sale")
            }
        }
    }
}
