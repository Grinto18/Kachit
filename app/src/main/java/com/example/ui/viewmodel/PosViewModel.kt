package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.dao.OrderWithItems
import com.example.data.entity.*
import com.example.data.repository.PosRepository
import com.example.data.repository.ProductDeleteResult
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    LOGIN,
    POS,
    TABLES,
    KITCHEN,
    ORDERS,
    MENU_MANAGEMENT,
    INVENTORY,
    REPORTS,
    CASH_REGISTER,
    EXPENSES,
    SETTINGS
}

data class CartItem(
    val product: ProductEntity,
    val quantity: Int = 1,
    val unitPrice: Double = product.price,
    val note: String = ""
) {
    val total: Double get() = quantity * unitPrice
}

data class CartState(
    val items: List<CartItem> = emptyList(),
    val orderType: String = "DINE_IN", // DINE_IN, TAKEAWAY, DELIVERY
    val selectedTable: TableEntity? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val deliveryFee: Double = 0.0,
    val discountPercent: Double = 0.0,
    val orderNotes: String = ""
) {
    val subtotal: Double get() = items.sumOf { it.total }
    val discountAmount: Double get() = subtotal * (discountPercent / 100.0)
    val total: Double get() = (subtotal - discountAmount + deliveryFee).coerceAtLeast(0.0)
    val totalItemCount: Int get() = items.sumOf { it.quantity }
}

class PosViewModel(private val repository: PosRepository) : ViewModel() {

    // Language State
    private val _currentLanguage = MutableStateFlow(AppLanguage.ARABIC)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Screen Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.POS)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Auth & User State (Default logged in as Admin for instant access, easy switch to Cashier/Kitchen)
    private val _currentUser = MutableStateFlow<UserEntity?>(
        UserEntity("u_admin", "admin", "admin123", "مدير النظام (Admin)", "ADMIN")
    )
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Cart State
    private val _cartState = MutableStateFlow(CartState())
    val cartState: StateFlow<CartState> = _cartState.asStateFlow()

    // Category Filter in POS
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    // Search query in POS
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Dialog & Feedback States
    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog.asStateFlow()

    private val _lastPaidReceipt = MutableStateFlow<OrderWithItems?>(null)
    val lastPaidReceipt: StateFlow<OrderWithItems?> = _lastPaidReceipt.asStateFlow()

    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    // Data streams from repository
    val categories: StateFlow<List<CategoryEntity>> = repository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipeItems: StateFlow<List<RecipeItemEntity>> = repository.allRecipeItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tables: StateFlow<List<TableEntity>> = repository.tables
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeKitchenOrders: StateFlow<List<OrderWithItems>> = repository.activeKitchenOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderWithItems>> = repository.allOrdersWithItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val heldOrders: StateFlow<List<OrderWithItems>> = repository.heldOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryItems: StateFlow<List<InventoryItemEntity>> = repository.inventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockItems: StateFlow<List<InventoryItemEntity>> = repository.lowStockItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSession: StateFlow<CashSessionEntity?> = repository.currentOpenSession
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val expenses: StateFlow<List<ExpenseEntity>> = repository.expenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products for POS
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        products,
        _selectedCategoryId,
        _searchQuery
    ) { prods, catId, query ->
        prods.filter { prod ->
            val matchesCategory = catId == null || prod.categoryId == catId
            val matchesSearch = query.isBlank() ||
                    prod.nameAr.contains(query, ignoreCase = true) ||
                    prod.nameFr.contains(query, ignoreCase = true) ||
                    prod.barcode.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        AppStrings.currentLanguage = language
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun scanBarcode(code: String) {
        viewModelScope.launch {
            val product = repository.getProductByBarcode(code.trim())
            if (product != null) {
                addToCart(product)
                _feedbackMessage.value = "تمت إضافة ${product.nameAr} بواسطة الباركود"
            } else {
                _feedbackMessage.value = "لم يتم العثور على منتج بهذا الباركود ($code)"
            }
        }
    }

    // Cart Operations
    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        val currentItems = _cartState.value.items.toMutableList()
        val index = currentItems.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existing = currentItems[index]
            currentItems[index] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            currentItems.add(CartItem(product = product, quantity = quantity))
        }
        _cartState.value = _cartState.value.copy(items = currentItems)
    }

    fun increaseQuantity(productId: String) {
        val updated = _cartState.value.items.map {
            if (it.product.id == productId) it.copy(quantity = it.quantity + 1) else it
        }
        _cartState.value = _cartState.value.copy(items = updated)
    }

    fun decreaseQuantity(productId: String) {
        val currentItems = _cartState.value.items
        val existing = currentItems.find { it.product.id == productId } ?: return
        if (existing.quantity > 1) {
            val updated = currentItems.map {
                if (it.product.id == productId) it.copy(quantity = it.quantity - 1) else it
            }
            _cartState.value = _cartState.value.copy(items = updated)
        } else {
            removeFromCart(productId)
        }
    }

    fun removeFromCart(productId: String) {
        val updated = _cartState.value.items.filterNot { it.product.id == productId }
        _cartState.value = _cartState.value.copy(items = updated)
    }

    fun updateItemNote(productId: String, note: String) {
        val updated = _cartState.value.items.map {
            if (it.product.id == productId) it.copy(note = note) else it
        }
        _cartState.value = _cartState.value.copy(items = updated)
    }

    fun setOrderType(type: String) {
        _cartState.value = _cartState.value.copy(
            orderType = type,
            deliveryFee = if (type == "DELIVERY") 200.0 else 0.0
        )
    }

    fun selectTable(table: TableEntity?) {
        _cartState.value = _cartState.value.copy(
            selectedTable = table,
            orderType = if (table != null) "DINE_IN" else _cartState.value.orderType
        )
    }

    fun setCustomerDetails(name: String, phone: String, address: String) {
        _cartState.value = _cartState.value.copy(
            customerName = name,
            customerPhone = phone,
            deliveryAddress = address
        )
    }

    fun setDiscountPercent(percent: Double) {
        _cartState.value = _cartState.value.copy(discountPercent = percent)
    }

    fun clearCart() {
        _cartState.value = CartState()
    }

    // Submit Order to Kitchen
    fun sendOrderToKitchen(onSuccess: (String) -> Unit = {}) {
        val state = _cartState.value
        if (state.items.isEmpty()) {
            _feedbackMessage.value = "يرجى إضافة منتجات إلى الطلب أولاً"
            return
        }
        if (state.orderType == "DINE_IN" && state.selectedTable == null) {
            _feedbackMessage.value = "يرجى اختيار طاولة للطلب المحلي"
            return
        }

        viewModelScope.launch {
            val orderId = "ord_" + UUID.randomUUID().toString().take(8)
            val orderNumber = repository.generateOrderNumber()
            val order = OrderEntity(
                id = orderId,
                orderNumber = orderNumber,
                orderType = state.orderType,
                tableId = state.selectedTable?.id,
                tableNumber = state.selectedTable?.tableNumber,
                customerName = state.customerName.ifBlank { null },
                customerPhone = state.customerPhone.ifBlank { null },
                deliveryAddress = state.deliveryAddress.ifBlank { null },
                deliveryFee = state.deliveryFee,
                cashierName = _currentUser.value?.fullName ?: "كاشير",
                status = "PREPARING",
                subtotal = state.subtotal,
                discount = state.discountAmount,
                tax = 0.0,
                total = state.total,
                notes = state.orderNotes,
                createdAt = System.currentTimeMillis()
            )

            val items = state.items.map { item ->
                OrderItemEntity(
                    id = "oi_" + UUID.randomUUID().toString().take(8),
                    orderId = orderId,
                    productId = item.product.id,
                    productName = item.product.nameAr,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    note = item.note,
                    kitchenStation = item.product.kitchenStation,
                    status = "PREPARING"
                )
            }

            repository.createOrUpdateOrder(order, items, sendToKitchen = true)
            _feedbackMessage.value = "تم إرسال الطلب $orderNumber إلى شاشة المطبخ بنجاح!"
            clearCart()
            onSuccess(orderId)
        }
    }

    fun openPaymentDialog() {
        if (_cartState.value.items.isEmpty()) {
            _feedbackMessage.value = "يرجى إضافة عناصر إلى الطلب أولاً"
            return
        }
        _showPaymentDialog.value = true
    }

    fun closePaymentDialog() {
        _showPaymentDialog.value = false
    }

    // Complete Payment for Current Cart
    fun processCartPayment(
        paymentMethod: String,
        amountReceived: Double,
        changeAmount: Double
    ) {
        val state = _cartState.value
        if (state.items.isEmpty()) return

        viewModelScope.launch {
            val orderId = "ord_" + UUID.randomUUID().toString().take(8)
            val orderNumber = repository.generateOrderNumber()
            val invoiceNumber = repository.generateInvoiceNumber()
            val now = System.currentTimeMillis()

            val order = OrderEntity(
                id = orderId,
                orderNumber = orderNumber,
                invoiceNumber = invoiceNumber,
                orderType = state.orderType,
                tableId = state.selectedTable?.id,
                tableNumber = state.selectedTable?.tableNumber,
                customerName = state.customerName.ifBlank { null },
                customerPhone = state.customerPhone.ifBlank { null },
                deliveryAddress = state.deliveryAddress.ifBlank { null },
                deliveryFee = state.deliveryFee,
                cashierName = _currentUser.value?.fullName ?: "كاشير",
                status = "PAID",
                subtotal = state.subtotal,
                discount = state.discountAmount,
                tax = 0.0,
                total = state.total,
                paymentMethod = paymentMethod,
                amountReceived = amountReceived,
                changeAmount = changeAmount,
                notes = state.orderNotes,
                createdAt = now,
                paidAt = now
            )

            val items = state.items.map { item ->
                OrderItemEntity(
                    id = "oi_" + UUID.randomUUID().toString().take(8),
                    orderId = orderId,
                    productId = item.product.id,
                    productName = item.product.nameAr,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity,
                    note = item.note,
                    kitchenStation = item.product.kitchenStation,
                    status = "READY"
                )
            }

            // Save order and deduct inventory
            repository.createOrUpdateOrder(order, items, sendToKitchen = false)
            val paidReceipt = repository.processPayment(
                orderId = orderId,
                paymentMethod = paymentMethod,
                amountReceived = amountReceived,
                changeAmount = changeAmount,
                cashierName = _currentUser.value?.fullName ?: "كاشير"
            )

            _lastPaidReceipt.value = paidReceipt
            _showPaymentDialog.value = false
            clearCart()
            _feedbackMessage.value = "تم إتمام الدفع بنجاح! فاتورة رقم $invoiceNumber"
        }
    }

    // Pay existing order from Table or Orders screen
    fun payExistingOrder(
        orderId: String,
        paymentMethod: String,
        amountReceived: Double,
        changeAmount: Double
    ) {
        viewModelScope.launch {
            val paidReceipt = repository.processPayment(
                orderId = orderId,
                paymentMethod = paymentMethod,
                amountReceived = amountReceived,
                changeAmount = changeAmount,
                cashierName = _currentUser.value?.fullName ?: "كاشير"
            )
            _lastPaidReceipt.value = paidReceipt
            _feedbackMessage.value = "تم سداد الفاتورة بنجاح!"
        }
    }

    fun showReceipt(receipt: OrderWithItems) {
        _lastPaidReceipt.value = receipt
    }

    fun refundOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.refundOrder(orderId, reason, _currentUser.value?.fullName ?: "مدير")
            _feedbackMessage.value = "تم استرجاع الطلب بنجاح"
        }
    }

    fun cancelOrder(orderId: String, reason: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId, reason, _currentUser.value?.fullName ?: "مدير")
            _feedbackMessage.value = "تم إلغاء الطلب بنجاح"
        }
    }

    fun dismissReceipt() {
        _lastPaidReceipt.value = null
    }

    fun clearFeedback() {
        _feedbackMessage.value = null
    }

    // Kitchen KDS actions
    fun setKitchenItemStatus(itemId: String, status: String) {
        viewModelScope.launch {
            repository.updateKitchenItemStatus(itemId, status)
        }
    }

    fun markOrderReady(orderId: String) {
        viewModelScope.launch {
            repository.markOrderReady(orderId)
            _feedbackMessage.value = "تم تجهيز الطلب بالكامل وهو جاهز للتقديم!"
        }
    }

    fun completeOrder(orderId: String) {
        viewModelScope.launch {
            repository.completeKitchenOrder(orderId)
            _feedbackMessage.value = "تم إنهاء الطلب وتسليمه."
        }
    }

    // Table operations
    fun transferTable(fromTableId: String, toTableId: String) {
        viewModelScope.launch {
            repository.transferTable(fromTableId, toTableId, _currentUser.value?.fullName ?: "كاشير")
            _feedbackMessage.value = "تم نقل الطاولة بنجاح!"
        }
    }

    // Expenses
    fun logExpense(category: String, amount: Double, description: String) {
        viewModelScope.launch {
            repository.addExpense(category, amount, description, _currentUser.value?.fullName ?: "كاشير")
            _feedbackMessage.value = "تم تسجيل المصروف بنجاح ($amount دج)"
        }
    }

    // Cash Register Shift
    fun openShift(amount: Double, notes: String) {
        viewModelScope.launch {
            repository.openCashSession(_currentUser.value?.fullName ?: "كاشير", amount, notes)
            _feedbackMessage.value = "تم افتتاح الصندوق بمبلغ $amount دج"
        }
    }

    // Restock Inventory
    fun restock(itemId: String, qty: Double, cost: Double) {
        viewModelScope.launch {
            repository.restockItem(itemId, qty, cost, _currentUser.value?.fullName ?: "مدير")
            _feedbackMessage.value = "تم تحديث كمية المخزون بنجاح!"
        }
    }

    // Menu Management
    fun saveProduct(product: ProductEntity, recipeItems: List<RecipeItemEntity>, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.saveProduct(product, recipeItems, _currentUser.value?.fullName ?: "مدير")
            _feedbackMessage.value = "تم حفظ المنتج '${product.nameAr}' بنجاح!"
            onDone()
        }
    }

    fun toggleProductAvailability(productId: String, available: Boolean) {
        viewModelScope.launch {
            repository.toggleProductAvailability(productId, available, _currentUser.value?.fullName ?: "مدير")
            _feedbackMessage.value = if (available) "تم تفعيل المنتج وإظهاره في الكاشير" else "تم إخفاء المنتج من شاشة الكاشير"
        }
    }

    fun deleteProduct(product: ProductEntity, onResult: (ProductDeleteResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteOrDeactivateProduct(product, _currentUser.value?.fullName ?: "مدير")
            when (result) {
                is ProductDeleteResult.Deactivated -> {
                    _feedbackMessage.value = "تم تعطيل '${product.nameAr}' وإخفاؤه من الكاشير لوجود ${result.orderCount} طلبات سابقة له"
                }
                is ProductDeleteResult.Deleted -> {
                    _feedbackMessage.value = "تم حذف المنتج '${product.nameAr}' نهائياً"
                }
            }
            onResult(result)
        }
    }

    fun loadProductRecipe(productId: String, onLoaded: (List<RecipeItemEntity>) -> Unit) {
        viewModelScope.launch {
            val items = repository.getRecipeItemsForProduct(productId)
            onLoaded(items)
        }
    }

    // Hold & Resume Orders
    fun holdCurrentOrder() {
        val state = _cartState.value
        if (state.items.isEmpty()) {
            _feedbackMessage.value = "لا يمكن تعليق سلة فارغة"
            return
        }
        viewModelScope.launch {
            val orderNumber = repository.generateOrderNumber()
            val orderId = "held_" + UUID.randomUUID().toString()
            val order = OrderEntity(
                id = orderId,
                orderNumber = orderNumber,
                orderType = state.orderType,
                tableId = state.selectedTable?.id,
                tableNumber = state.selectedTable?.tableNumber,
                customerName = state.customerName.ifBlank { null },
                customerPhone = state.customerPhone.ifBlank { null },
                deliveryAddress = state.deliveryAddress.ifBlank { null },
                deliveryFee = state.deliveryFee,
                cashierName = _currentUser.value?.fullName ?: "كاشير",
                subtotal = state.subtotal,
                discount = state.discountAmount,
                total = state.total,
                status = "HELD",
                notes = state.orderNotes
            )
            val items = state.items.map { cartItem ->
                OrderItemEntity(
                    id = UUID.randomUUID().toString(),
                    orderId = orderId,
                    productId = cartItem.product.id,
                    productName = cartItem.product.nameAr,
                    unitPrice = cartItem.unitPrice,
                    quantity = cartItem.quantity,
                    note = cartItem.note,
                    kitchenStation = cartItem.product.kitchenStation
                )
            }
            repository.holdOrder(order, items, _currentUser.value?.fullName ?: "كاشير")
            clearCart()
            _feedbackMessage.value = "تم تعليق الطلب $orderNumber بنجاح! يمكنك استئنافه لاحقاً"
        }
    }

    fun resumeHeldOrder(orderWithItems: OrderWithItems) {
        viewModelScope.launch {
            val order = orderWithItems.order
            val items = orderWithItems.items

            val restoredCartItems = items.map { item ->
                val prod = repository.products.first().find { it.id == item.productId }
                    ?: ProductEntity(
                        id = item.productId,
                        nameAr = item.productName,
                        nameFr = "",
                        nameEn = "",
                        categoryId = "cat_grill",
                        price = item.unitPrice,
                        kitchenStation = item.kitchenStation
                    )
                CartItem(
                    product = prod,
                    quantity = item.quantity,
                    unitPrice = item.unitPrice,
                    note = item.note
                )
            }

            val table = if (order.tableId != null) {
                repository.tables.first().find { it.id == order.tableId }
            } else null

            _cartState.value = CartState(
                items = restoredCartItems,
                orderType = order.orderType,
                selectedTable = table,
                customerName = order.customerName ?: "",
                customerPhone = order.customerPhone ?: "",
                deliveryAddress = order.deliveryAddress ?: "",
                deliveryFee = order.deliveryFee,
                discountPercent = if (order.subtotal > 0) (order.discount / order.subtotal) * 100.0 else 0.0,
                orderNotes = order.notes
            )

            // Remove from held orders in DB
            repository.deleteHeldOrder(order.id, _currentUser.value?.fullName ?: "كاشير")
            _feedbackMessage.value = "تم استئناف الطلب ${order.orderNumber} في شاشة البيع"
        }
    }

    fun deleteHeldOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteHeldOrder(orderId, _currentUser.value?.fullName ?: "كاشير")
            _feedbackMessage.value = "تم إلغاء الطلب المعلق"
        }
    }

    // Waste and Stock Adjustments
    fun recordWaste(itemId: String, qty: Double, reason: String) {
        viewModelScope.launch {
            repository.recordWaste(itemId, qty, reason, _currentUser.value?.fullName ?: "مدير")
            _feedbackMessage.value = "تم تسجيل الهالك وخصم $qty من المخزون"
        }
    }

    fun recordPhysicalStockCount(itemId: String, actualStock: Double, notes: String) {
        viewModelScope.launch {
            repository.recordPhysicalStockCount(itemId, actualStock, notes, _currentUser.value?.fullName ?: "مدير")
            _feedbackMessage.value = "تم تسجيل الجرد الفعلي بنجاح!"
        }
    }

    // Authentication
    fun login(username: String, pass: String): Boolean {
        // Fast instant login for demo accounts
        val role = when (username.lowercase().trim()) {
            "admin" -> "ADMIN"
            "cashier" -> "CASHIER"
            "waiter" -> "WAITER"
            "kitchen" -> "KITCHEN"
            "manager" -> "MANAGER"
            else -> "CASHIER"
        }
        val name = when (username.lowercase().trim()) {
            "admin" -> "مدير المطعم (Admin)"
            "cashier" -> "أحمد الجيجلي (كاشير)"
            "waiter" -> "كريم بلقاسم (نادل)"
            "kitchen" -> "شيف مصطفى (المطبخ)"
            "manager" -> "ياسين مسير (Manager)"
            else -> username
        }

        _currentUser.value = UserEntity(
            id = "u_$username",
            username = username,
            passwordHash = pass,
            fullName = name,
            role = role
        )

        // Route to initial screen based on role:
        // Cashier goes directly to POS!
        // Kitchen goes to KDS!
        // Waiter goes to Tables!
        // Admin / Manager goes to POS or Dashboard
        when (role) {
            "CASHIER" -> _currentScreen.value = AppScreen.POS
            "KITCHEN" -> _currentScreen.value = AppScreen.KITCHEN
            "WAITER" -> _currentScreen.value = AppScreen.TABLES
            else -> _currentScreen.value = AppScreen.POS
        }

        _feedbackMessage.value = "مرحباً بك $name"
        return true
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = AppScreen.LOGIN
    }
}

class PosViewModelFactory(private val repository: PosRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PosViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
