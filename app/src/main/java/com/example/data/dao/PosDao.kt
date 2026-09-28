package com.example.data.dao

import androidx.room.*
import com.example.data.entity.*
import kotlinx.coroutines.flow.Flow

data class OrderWithItems(
    @Embedded val order: OrderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)

@Dao
interface PosDao {
    // --- Users ---
    @Query("SELECT * FROM users WHERE active = 1")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // --- Categories & Products ---
    @Query("SELECT * FROM categories WHERE active = 1 ORDER BY sortOrder ASC")
    fun getCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("SELECT * FROM products WHERE available = 1 ORDER BY nameAr ASC")
    fun getProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY nameAr ASC")
    fun getAllProductsIncludeUnavailable(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE barcode = :barcode AND available = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM order_items WHERE productId = :productId")
    suspend fun getProductOrderCount(productId: String): Int

    // --- Tables ---
    @Query("SELECT * FROM restaurant_tables ORDER BY tableNumber ASC")
    fun getTables(): Flow<List<TableEntity>>

    @Query("SELECT * FROM restaurant_tables WHERE id = :tableId LIMIT 1")
    suspend fun getTableById(tableId: String): TableEntity?

    @Query("SELECT * FROM restaurant_tables WHERE tableNumber = :tableNumber LIMIT 1")
    suspend fun getTableByNumber(tableNumber: Int): TableEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTables(tables: List<TableEntity>)

    @Update
    suspend fun updateTable(table: TableEntity)

    @Query("UPDATE restaurant_tables SET status = :status, currentOrderId = :orderId WHERE id = :tableId")
    suspend fun updateTableStatus(tableId: String, status: String, orderId: String?)

    // --- Orders & Items ---
    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrdersWithItems(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE status IN ('NEW', 'PREPARING', 'READY') ORDER BY createdAt ASC")
    fun getActiveKitchenOrders(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE status = 'HELD' ORDER BY createdAt DESC")
    fun getHeldOrders(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderWithItemsById(orderId: String): OrderWithItems?

    @Query("DELETE FROM orders WHERE id = :orderId")
    suspend fun deleteOrder(orderId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("DELETE FROM order_items WHERE orderId = :orderId")
    suspend fun deleteOrderItems(orderId: String)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE order_items SET status = :status WHERE id = :itemId")
    suspend fun updateOrderItemStatus(itemId: String, status: String)

    // --- Inventory & Recipes ---
    @Query("SELECT * FROM inventory_items ORDER BY nameAr ASC")
    fun getInventoryItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE currentStock <= minStock")
    fun getLowStockItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id LIMIT 1")
    suspend fun getInventoryItemById(id: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItems(items: List<InventoryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItemEntity)

    @Update
    suspend fun updateInventoryItem(item: InventoryItemEntity)

    @Query("UPDATE inventory_items SET currentStock = currentStock - :qty, updatedAt = :time WHERE id = :id")
    suspend fun deductInventoryStock(id: String, qty: Double, time: Long = System.currentTimeMillis())

    @Query("UPDATE inventory_items SET currentStock = currentStock + :qty, updatedAt = :time WHERE id = :id")
    suspend fun addInventoryStock(id: String, qty: Double, time: Long = System.currentTimeMillis())

    @Query("SELECT * FROM recipe_items WHERE productId = :productId")
    suspend fun getRecipeItemsForProduct(productId: String): List<RecipeItemEntity>

    @Query("SELECT * FROM recipe_items")
    fun getAllRecipeItems(): Flow<List<RecipeItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeItems(items: List<RecipeItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeItem(item: RecipeItemEntity)

    @Query("DELETE FROM recipe_items WHERE productId = :productId")
    suspend fun deleteRecipeItemsForProduct(productId: String)

    // --- Purchases & Suppliers ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getSuppliers(): Flow<List<SupplierEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: SupplierEntity)

    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getPurchases(): Flow<List<PurchaseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItemEntity>)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    // --- Cash Sessions (Shift) ---
    @Query("SELECT * FROM cash_sessions WHERE status = 'OPEN' ORDER BY openedAt DESC LIMIT 1")
    fun getCurrentOpenSession(): Flow<CashSessionEntity?>

    @Query("SELECT * FROM cash_sessions ORDER BY openedAt DESC")
    fun getAllCashSessions(): Flow<List<CashSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashSession(session: CashSessionEntity)

    @Update
    suspend fun updateCashSession(session: CashSessionEntity)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)

    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getCustomers(): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    // --- Settings ---
    @Query("SELECT * FROM app_settings")
    fun getSettings(): Flow<List<SettingEntity>>

    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSettingValue(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)

    // --- Inventory Deduction Transaction ---
    @Transaction
    suspend fun deductStockForOrder(items: List<OrderItemEntity>) {
        for (item in items) {
            val recipeItems = getRecipeItemsForProduct(item.productId)
            for (recipeItem in recipeItems) {
                val totalQty = recipeItem.quantityNeeded * item.quantity
                deductInventoryStock(recipeItem.inventoryItemId, totalQty)
            }
        }
    }
}
