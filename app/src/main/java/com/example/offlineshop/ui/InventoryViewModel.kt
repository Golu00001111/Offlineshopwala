package com.example.offlineshop.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlineshop.data.AppDatabase
import com.example.offlineshop.data.CartItem
import com.example.offlineshop.data.InventoryRepository
import com.example.offlineshop.data.Product
import com.example.offlineshop.data.Sale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = InventoryRepository(AppDatabase.getInstance(application))

    val products: StateFlow<List<Product>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.sales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart

    private val _checkoutMessage = MutableStateFlow<String?>(null)
    val checkoutMessage: StateFlow<String?> = _checkoutMessage

    fun addProduct(name: String, sku: String, price: Double, quantity: Int) {
        viewModelScope.launch {
            repository.addProduct(Product(name = name, sku = sku, price = price, quantity = quantity))
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch { repository.updateProduct(product) }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            removeFromCart(product)
        }
    }

    fun addToCart(product: Product, quantity: Int) {
        if (quantity <= 0) return
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        val availableStock = product.quantity
        if (index >= 0) {
            val newQty = (current[index].quantity + quantity).coerceAtMost(availableStock)
            current[index] = current[index].copy(quantity = newQty)
        } else {
            current.add(CartItem(product, quantity.coerceAtMost(availableStock)))
        }
        _cart.value = current
    }

    fun removeFromCart(product: Product) {
        _cart.value = _cart.value.filterNot { it.product.id == product.id }
    }

    fun decrementInCart(product: Product) {
        val current = _cart.value.toMutableList()
        val index = current.indexOfFirst { it.product.id == product.id }
        if (index < 0) return
        val newQty = current[index].quantity - 1
        if (newQty <= 0) {
            current.removeAt(index)
        } else {
            current[index] = current[index].copy(quantity = newQty)
        }
        _cart.value = current
    }

    fun cartTotal(): Double = _cart.value.sumOf { it.product.price * it.quantity }

    fun checkout() {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return
        viewModelScope.launch {
            repository.checkout(currentCart)
            _cart.value = emptyList()
            _checkoutMessage.value = "Sale completed successfully"
        }
    }

    fun clearCheckoutMessage() {
        _checkoutMessage.value = null
    }

    suspend fun getSaleItems(saleId: Long) = repository.getSaleItems(saleId)
}
