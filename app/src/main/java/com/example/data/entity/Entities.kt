package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,
    val fullName: String,
    val role: String, // ADMIN, MANAGER, CASHIER, WAITER, KITCHEN
    val active: Boolean = true,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameFr: String,
    val nameEn: String,
    val icon: String = "restaurant",
    val sortOrder: Int = 0,
    val active: Boolean = true
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameFr: String,
    val nameEn: String,
    val categoryId: String,
    val price: Double,
    val cost: Double = 0.0,
    val barcode: String = "",
    val kitchenStation: String = "MAIN_KITCHEN", // MAIN_KITCHEN, GRILL, DRINKS, DESSERT
    val available: Boolean = true,
    val description: String = "",
    val iconName: String = "dinner_dining"
)

@Entity(tableName = "restaurant_tables")
data class TableEntity(
    @PrimaryKey val id: String,
    val tableNumber: Int,
    val capacity: Int = 4,
    val status: String = "AVAILABLE", // AVAILABLE, OCCUPIED, WAITING_PAYMENT, RESERVED
    val currentOrderId: String? = null,
    val guestCount: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val invoiceNumber: String? = null,
    val orderType: String = "DINE_IN", // DINE_IN, TAKEAWAY, DELIVERY
    val tableId: String? = null,
    val tableNumber: Int? = null,
    val customerName: String? = null,
    val customerPhone: String? = null,
    val deliveryAddress: String? = null,
    val deliveryFee: Double = 0.0,
    val cashierName: String = "",
    val status: String = "NEW", // NEW, PREPARING, READY, PAID, CANCELLED, REFUNDED
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String? = null, // CASH, CARD, BARIDIMOB, CCP, OTHER
    val amountReceived: Double = 0.0,
    val changeAmount: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val paidAt: Long? = null
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val note: String = "",
    val kitchenStation: String = "MAIN_KITCHEN",
    val status: String = "NEW" // NEW, PREPARING, READY, SERVED
)

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val id: String,
    val nameAr: String,
    val nameFr: String,
    val sku: String,
    val unit: String, // kg, g, liter, ml, piece, box
    val currentStock: Double,
    val minStock: Double,
    val unitCost: Double,
    val supplierId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recipe_items")
data class RecipeItemEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val inventoryItemId: String,
    val quantityNeeded: Double,
    val unit: String
)

@Entity(tableName = "suppliers")
data class SupplierEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val notes: String = ""
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey val id: String,
    val purchaseNumber: String,
    val supplierId: String,
    val supplierName: String,
    val date: Long = System.currentTimeMillis(),
    val totalCost: Double,
    val notes: String = ""
)

@Entity(tableName = "purchase_items")
data class PurchaseItemEntity(
    @PrimaryKey val id: String,
    val purchaseId: String,
    val inventoryItemId: String,
    val itemName: String,
    val quantity: Double,
    val unitCost: Double,
    val totalCost: Double
)

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val category: String, // مواد غذائية, كهرباء وماء, رواتب, صيانة, نقل, أخرى
    val amount: Double,
    val description: String,
    val userName: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_sessions")
data class CashSessionEntity(
    @PrimaryKey val id: String,
    val cashierName: String,
    val openedAt: Long = System.currentTimeMillis(),
    val closedAt: Long? = null,
    val openingCash: Double,
    val cashSales: Double = 0.0,
    val cashExpenses: Double = 0.0,
    val expectedCash: Double = 0.0,
    val actualCash: Double? = null,
    val difference: Double? = null,
    val status: String = "OPEN", // OPEN, CLOSED
    val notes: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val userName: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val address: String = "",
    val totalSpent: Double = 0.0,
    val ordersCount: Int = 0,
    val notes: String = ""
)

@Entity(tableName = "app_settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
