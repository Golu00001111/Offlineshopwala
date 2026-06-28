package com.example.offlineshop.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

data class CartItem(
    val product: Product,
    val quantity: Int
)

class InventoryRepository(private val db: AppDatabase) {

    val products: Flow<List<Product>> = db.productDao().getAllProducts()
    val sales: Flow<List<Sale>> = db.saleDao().getAllSales()
    val lowStockProducts: Flow<List<Product>> = db.productDao().getLowStock()

    suspend fun addProduct(product: Product) = db.productDao().insert(product)

    suspend fun updateProduct(product: Product) = db.productDao().update(product)

    suspend fun deleteProduct(product: Product) = db.productDao().delete(product)

    suspend fun getSaleItems(saleId: Long): List<SaleItem> = db.saleDao().getItemsForSale(saleId)

    suspend fun checkout(cart: List<CartItem>): Long = db.withTransaction {
        val total = cart.sumOf { it.product.price * it.quantity }
        val saleId = db.saleDao().insertSale(Sale(timestamp = System.currentTimeMillis(), total = total))

        val items = cart.map {
            SaleItem(
                saleId = saleId,
                productId = it.product.id,
                productName = it.product.name,
                quantity = it.quantity,
                priceEach = it.product.price
            )
        }
        db.saleDao().insertSaleItems(items)

        cart.forEach { db.productDao().decreaseStock(it.product.id, it.quantity) }

        saleId
    }
}
