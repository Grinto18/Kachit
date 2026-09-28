package com.example.data.repository

import com.example.data.dao.OrderWithItems
import com.example.data.dao.PosDao
import com.example.data.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

sealed class ProductDeleteResult {
    data class Deactivated(val orderCount: Int) : ProductDeleteResult()
    object Deleted : ProductDeleteResult()
}

class PosRepository(private val dao: PosDao) {

    // Streams
    val categories: Flow<List<CategoryEntity>> = dao.getCategories()
    val products: Flow<List<ProductEntity>> = dao.getProducts()
    val allProducts: Flow<List<ProductEntity>> = dao.getAllProductsIncludeUnavailable()
    val allRecipeItems: Flow<List<RecipeItemEntity>> = dao.getAllRecipeItems()
    val tables: Flow<List<TableEntity>> = dao.getTables()
    val activeKitchenOrders: Flow<List<OrderWithItems>> = dao.getActiveKitchenOrders()
    val allOrdersWithItems: Flow<List<OrderWithItems>> = dao.getAllOrdersWithItems()
    val heldOrders: Flow<List<OrderWithItems>> = dao.getHeldOrders()
    val inventoryItems: Flow<List<InventoryItemEntity>> = dao.getInventoryItems()
    val lowStockItems: Flow<List<InventoryItemEntity>> = dao.getLowStockItems()
    val currentOpenSession: Flow<CashSessionEntity?> = dao.getCurrentOpenSession()
    val expenses: Flow<List<ExpenseEntity>> = dao.getExpenses()
    val auditLogs: Flow<List<AuditLogEntity>> = dao.getAuditLogs()
    val suppliers: Flow<List<SupplierEntity>> = dao.getSuppliers()
    val purchases: Flow<List<PurchaseEntity>> = dao.getPurchases()

    suspend fun getProductByBarcode(barcode: String): ProductEntity? = withContext(Dispatchers.IO) {
        dao.getProductByBarcode(barcode)
    }

    suspend fun getUserByUsername(username: String): UserEntity? = withContext(Dispatchers.IO) {
        dao.getUserByUsername(username)
    }

    suspend fun createOrUpdateOrder(
        order: OrderEntity,
        items: List<OrderItemEntity>,
        sendToKitchen: Boolean
    ): String = withContext(Dispatchers.IO) {
        val finalStatus = if (sendToKitchen) "PREPARING" else "NEW"
        val updatedOrder = order.copy(
            status = finalStatus
        )
        dao.insertOrder(updatedOrder)
        dao.deleteOrderItems(order.id)
        dao.insertOrderItems(items.map { it.copy(orderId = order.id) })

        // If Dine In, update table status to OCCUPIED
        if (order.orderType == "DINE_IN" && order.tableId != null) {
            dao.updateTableStatus(order.tableId, "OCCUPIED", order.id)
        }

        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = order.cashierName,
                action = if (sendToKitchen) "SEND_TO_KITCHEN" else "SAVE_ORDER",
                details = "طلب رقم ${order.orderNumber} - إجمالي: ${order.total} دج"
            )
        )
        order.id
    }

    suspend fun processPayment(
        orderId: String,
        paymentMethod: String,
        amountReceived: Double,
        changeAmount: Double,
        cashierName: String
    ): OrderWithItems? = withContext(Dispatchers.IO) {
        val orderWithItems = dao.getOrderWithItemsById(orderId) ?: return@withContext null
        val order = orderWithItems.order
        val items = orderWithItems.items

        val invoiceNumber = generateInvoiceNumber()
        val now = System.currentTimeMillis()

        // 1. Update Order status to PAID
        val paidOrder = order.copy(
            status = "PAID",
            invoiceNumber = invoiceNumber,
            paymentMethod = paymentMethod,
            amountReceived = amountReceived,
            changeAmount = changeAmount,
            paidAt = now
        )
        dao.updateOrder(paidOrder)

        // 2. Free Table if Dine In
        if (paidOrder.tableId != null) {
            dao.updateTableStatus(paidOrder.tableId, "AVAILABLE", null)
        }

        // 3. Deduct Stock for Recipe Items
        dao.deductStockForOrder(items)

        // 4. Update Cash Register if cash
        if (paymentMethod == "CASH") {
            // Note: will be reflected in cash sales session
        }

        // 5. Audit Log
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = cashierName,
                action = "PAYMENT_PROCESSED",
                details = "دفع الفاتورة $invoiceNumber بقيمة ${paidOrder.total} دج بطريقة $paymentMethod"
            )
        )

        dao.getOrderWithItemsById(orderId)
    }

    suspend fun cancelOrder(orderId: String, reason: String, userName: String) = withContext(Dispatchers.IO) {
        val orderWithItems = dao.getOrderWithItemsById(orderId) ?: return@withContext
        dao.updateOrderStatus(orderId, "CANCELLED")
        if (orderWithItems.order.tableId != null) {
            dao.updateTableStatus(orderWithItems.order.tableId, "AVAILABLE", null)
        }
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = "CANCEL_ORDER",
                details = "إلغاء الطلب ${orderWithItems.order.orderNumber} - السبب: $reason"
            )
        )
    }

    suspend fun refundOrder(orderId: String, reason: String, userName: String) = withContext(Dispatchers.IO) {
        val orderWithItems = dao.getOrderWithItemsById(orderId) ?: return@withContext
        dao.updateOrderStatus(orderId, "REFUNDED")
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = "REFUND_ORDER",
                details = "استرجاع الطلب ${orderWithItems.order.orderNumber} - ${orderWithItems.order.total} دج - السبب: $reason"
            )
        )
    }

    // Kitchen KDS actions
    suspend fun updateKitchenItemStatus(itemId: String, status: String) = withContext(Dispatchers.IO) {
        dao.updateOrderItemStatus(itemId, status)
    }

    suspend fun markOrderReady(orderId: String) = withContext(Dispatchers.IO) {
        dao.updateOrderStatus(orderId, "READY")
    }

    suspend fun completeKitchenOrder(orderId: String) = withContext(Dispatchers.IO) {
        val orderWithItems = dao.getOrderWithItemsById(orderId) ?: return@withContext
        // If not already paid, it stays as READY, else COMPLETED
        if (orderWithItems.order.status == "PAID") {
            dao.updateOrderStatus(orderId, "COMPLETED")
        } else {
            dao.updateOrderStatus(orderId, "READY")
        }
    }

    // Table Management
    suspend fun transferTable(sourceTableId: String, targetTableId: String, userName: String) = withContext(Dispatchers.IO) {
        val sourceTable = dao.getTableById(sourceTableId) ?: return@withContext
        val targetTable = dao.getTableById(targetTableId) ?: return@withContext
        val orderId = sourceTable.currentOrderId ?: return@withContext

        // Move order to target table
        dao.updateTableStatus(sourceTableId, "AVAILABLE", null)
        dao.updateTableStatus(targetTableId, "OCCUPIED", orderId)

        val orderWithItems = dao.getOrderWithItemsById(orderId)
        if (orderWithItems != null) {
            dao.updateOrder(orderWithItems.order.copy(tableId = targetTableId, tableNumber = targetTable.tableNumber))
        }

        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = "TRANSFER_TABLE",
                details = "تحويل الطلب من طاولة ${sourceTable.tableNumber} إلى طاولة ${targetTable.tableNumber}"
            )
        )
    }

    suspend fun addExpense(category: String, amount: Double, description: String, userName: String) = withContext(Dispatchers.IO) {
        val expense = ExpenseEntity(
            id = UUID.randomUUID().toString(),
            category = category,
            amount = amount,
            description = description,
            userName = userName,
            timestamp = System.currentTimeMillis()
        )
        dao.insertExpense(expense)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = "RECORD_EXPENSE",
                details = "تسجيل مصروف: $category بقيمة $amount دج - $description"
            )
        )
    }

    suspend fun openCashSession(cashierName: String, openingCash: Double, notes: String) = withContext(Dispatchers.IO) {
        val session = CashSessionEntity(
            id = "cs_" + System.currentTimeMillis(),
            cashierName = cashierName,
            openedAt = System.currentTimeMillis(),
            openingCash = openingCash,
            cashSales = 0.0,
            cashExpenses = 0.0,
            expectedCash = openingCash,
            status = "OPEN",
            notes = notes
        )
        dao.insertCashSession(session)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = cashierName,
                action = "OPEN_CASH_SESSION",
                details = "افتتاح الصندوق بمبلغ $openingCash دج"
            )
        )
    }

    suspend fun closeCashSession(sessionId: String, actualCash: Double, cashierName: String, notes: String) = withContext(Dispatchers.IO) {
        // Find session
        val allSessions = dao.getAllCashSessions()
        // We will calculate expected cash and difference
        // In local room, session update
        // We can do a direct update
    }

    suspend fun restockItem(itemId: String, quantity: Double, unitCost: Double, userName: String) = withContext(Dispatchers.IO) {
        dao.addInventoryStock(itemId, quantity)
        val item = dao.getInventoryItemById(itemId)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = "RESTOCK_INVENTORY",
                details = "إضافة $quantity ${item?.unit ?: ""} لمخزون ${item?.nameAr ?: itemId}"
            )
        )
    }

    suspend fun getRecipeItemsForProduct(productId: String): List<RecipeItemEntity> = withContext(Dispatchers.IO) {
        dao.getRecipeItemsForProduct(productId)
    }

    suspend fun saveProduct(
        product: ProductEntity,
        recipeItems: List<RecipeItemEntity>,
        userName: String
    ) = withContext(Dispatchers.IO) {
        dao.insertProduct(product)
        dao.deleteRecipeItemsForProduct(product.id)
        if (recipeItems.isNotEmpty()) {
            dao.insertRecipeItems(recipeItems.map { it.copy(productId = product.id) })
        }
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = "SAVE_PRODUCT",
                details = "حفظ المنتج: ${product.nameAr} - السعر: ${product.price} دج - التكلفة: ${product.cost} دج"
            )
        )
    }

    suspend fun toggleProductAvailability(
        productId: String,
        available: Boolean,
        userName: String
    ) = withContext(Dispatchers.IO) {
        val product = dao.getProductById(productId) ?: return@withContext
        val updated = product.copy(available = available)
        dao.updateProduct(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = if (available) "ACTIVATE_PRODUCT" else "DEACTIVATE_PRODUCT",
                details = "${if (available) "تفعيل" else "إخفاء"} المنتج: ${product.nameAr}"
            )
        )
    }

    suspend fun deleteOrDeactivateProduct(
        product: ProductEntity,
        userName: String
    ): ProductDeleteResult = withContext(Dispatchers.IO) {
        val count = dao.getProductOrderCount(product.id)
        if (count > 0) {
            // Cannot delete permanently due to financial history; deactivate instead!
            dao.updateProduct(product.copy(available = false))
            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    userName = userName,
                    action = "DEACTIVATE_PRODUCT",
                    details = "تعطيل المنتج ${product.nameAr} لوجود $count طلبات مرتبطة به"
                )
            )
            ProductDeleteResult.Deactivated(count)
        } else {
            // Never used in any orders, safe to delete completely
            dao.deleteRecipeItemsForProduct(product.id)
            dao.deleteProduct(product)
            dao.insertAuditLog(
                AuditLogEntity(
                    id = UUID.randomUUID().toString(),
                    userName = userName,
                    action = "DELETE_PRODUCT",
                    details = "حذف نهائي للمنتج الجديد ${product.nameAr}"
                )
            )
            ProductDeleteResult.Deleted
        }
    }

    suspend fun holdOrder(
        order: OrderEntity,
        items: List<OrderItemEntity>,
        cashierName: String
    ): String = withContext(Dispatchers.IO) {
        val heldOrder = order.copy(
            status = "HELD"
        )
        dao.insertOrder(heldOrder)
        dao.deleteOrderItems(heldOrder.id)
        dao.insertOrderItems(items.map { it.copy(orderId = heldOrder.id) })

        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = cashierName,
                action = "HOLD_ORDER",
                details = "تعليق الطلب رقم ${heldOrder.orderNumber} - إجمالي ${heldOrder.total} دج"
            )
        )
        heldOrder.id
    }

    suspend fun deleteHeldOrder(orderId: String, cashierName: String) = withContext(Dispatchers.IO) {
        dao.deleteOrderItems(orderId)
        dao.deleteOrder(orderId)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = cashierName,
                action = "DELETE_HELD_ORDER",
                details = "حذف الطلب المعلق $orderId"
            )
        )
    }

    suspend fun recordWaste(
        itemId: String,
        quantity: Double,
        reason: String,
        userName: String
    ) = withContext(Dispatchers.IO) {
        dao.deductInventoryStock(itemId, quantity)
        val item = dao.getInventoryItemById(itemId)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = "RECORD_WASTE",
                details = "تسجيل هالك/تالف: $quantity ${item?.unit ?: ""} من ${item?.nameAr ?: itemId} - السبب: $reason"
            )
        )
    }

    suspend fun recordPhysicalStockCount(
        itemId: String,
        physicalStock: Double,
        notes: String,
        userName: String
    ) = withContext(Dispatchers.IO) {
        val item = dao.getInventoryItemById(itemId) ?: return@withContext
        val diff = physicalStock - item.currentStock
        val updated = item.copy(currentStock = physicalStock, updatedAt = System.currentTimeMillis())
        dao.updateInventoryItem(updated)
        dao.insertAuditLog(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                userName = userName,
                action = "STOCK_COUNT_ADJUSTMENT",
                details = "جرد فعلي لـ ${item.nameAr}: السابق=${item.currentStock}, الفعلي=$physicalStock, الفارق=$diff ${item.unit} - ملاحظات: $notes"
            )
        )
    }

    fun generateOrderNumber(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        val datePart = sdf.format(Date())
        val randomSuffix = (1000..9999).random()
        return "ORD-$datePart-$randomSuffix"
    }

    fun generateInvoiceNumber(): String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.US)
        val datePart = sdf.format(Date())
        val randomSuffix = (1000..9999).random()
        return "INV-$datePart-$randomSuffix"
    }
}
