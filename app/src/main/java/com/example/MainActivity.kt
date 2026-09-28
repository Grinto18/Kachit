package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.dao.OrderWithItems
import com.example.data.db.AppDatabase
import com.example.data.repository.PosRepository
import com.example.i18n.AppStrings
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppSideNavRail
import com.example.ui.components.AppTopBar
import com.example.ui.screens.*
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.PalaisDoreTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.PosViewModel
import com.example.ui.viewmodel.PosViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = PosRepository(database.posDao())
        val viewModelFactory = PosViewModelFactory(repository)

        setContent {
            PalaisDoreTheme {
                val viewModel: PosViewModel = viewModel(factory = viewModelFactory)
                MainAppContent(viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: PosViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val cartState by viewModel.cartState.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val products by viewModel.filteredProducts.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allRecipeItems by viewModel.allRecipeItems.collectAsStateWithLifecycle()
    val tables by viewModel.tables.collectAsStateWithLifecycle()
    val activeKitchenOrders by viewModel.activeKitchenOrders.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val heldOrders by viewModel.heldOrders.collectAsStateWithLifecycle()
    val inventoryItems by viewModel.inventoryItems.collectAsStateWithLifecycle()
    val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()

    val showPaymentDialog by viewModel.showPaymentDialog.collectAsStateWithLifecycle()
    val lastPaidReceipt by viewModel.lastPaidReceipt.collectAsStateWithLifecycle()
    val feedbackMessage by viewModel.feedbackMessage.collectAsStateWithLifecycle()

    // Handle Android system back button
    BackHandler(enabled = currentScreen != AppScreen.POS && currentScreen != AppScreen.LOGIN) {
        viewModel.navigateTo(AppScreen.POS)
    }

    // Dismiss feedback after 4 seconds
    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            kotlinx.coroutines.delay(4000)
            viewModel.clearFeedback()
        }
    }

    if (currentScreen == AppScreen.LOGIN) {
        LoginScreen(
            onLoginSuccess = { user, pass ->
                viewModel.login(user, pass)
            }
        )
    } else {
        Scaffold(
            topBar = {
                AppTopBar(
                    currentScreen = currentScreen,
                    currentUser = currentUser,
                    currentLanguage = currentLanguage,
                    lowStockCount = lowStockItems.size,
                    onNavigate = { viewModel.navigateTo(it) },
                    onLanguageChange = { viewModel.setLanguage(it) },
                    onLogout = { viewModel.logout() }
                )
            },
            bottomBar = {
                // Show bottom nav on phone/compact mode
                BoxWithConstraints {
                    if (maxWidth < 840.dp) {
                        AppBottomNav(
                            currentScreen = currentScreen,
                            currentUser = currentUser,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                }
            },
            containerColor = DarkBackground
        ) { paddingValues ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val isTabletOrDesktop = maxWidth >= 840.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    // Side Nav Rail on Tablet / Desktop
                    if (isTabletOrDesktop) {
                        AppSideNavRail(
                            currentScreen = currentScreen,
                            currentUser = currentUser,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }

                    // Main Active Screen Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        when (currentScreen) {
                            AppScreen.POS -> {
                                PosScreen(
                                    categories = categories,
                                    products = products,
                                    tables = tables,
                                    cartState = cartState,
                                    selectedCategoryId = selectedCategoryId,
                                    searchQuery = searchQuery,
                                    onCategorySelect = { viewModel.selectCategory(it) },
                                    onSearchChange = { viewModel.setSearchQuery(it) },
                                    onBarcodeScan = { viewModel.scanBarcode(it) },
                                    onAddToCart = { viewModel.addToCart(it) },
                                    onIncreaseQty = { viewModel.increaseQuantity(it) },
                                    onDecreaseQty = { viewModel.decreaseQuantity(it) },
                                    onRemoveItem = { viewModel.removeFromCart(it) },
                                    onUpdateItemNote = { id, note -> viewModel.updateItemNote(id, note) },
                                    onOrderTypeChange = { viewModel.setOrderType(it) },
                                    onSelectTable = { viewModel.selectTable(it) },
                                    onCustomerDetailsChange = { name, phone, addr ->
                                        viewModel.setCustomerDetails(name, phone, addr)
                                    },
                                    onClearCart = { viewModel.clearCart() },
                                    onHoldOrder = { viewModel.holdCurrentOrder() },
                                    onResumeHeldOrder = { viewModel.resumeHeldOrder(it) },
                                    onDeleteHeldOrder = { viewModel.deleteHeldOrder(it) },
                                    onSendToKitchen = { viewModel.sendOrderToKitchen() },
                                    onOpenPayment = { viewModel.openPaymentDialog() },
                                    heldOrders = heldOrders
                                )
                            }
                            AppScreen.TABLES -> {
                                TablesScreen(
                                    tables = tables,
                                    activeOrders = activeKitchenOrders,
                                    onSelectTableForOrder = { table ->
                                        viewModel.selectTable(table)
                                        viewModel.navigateTo(AppScreen.POS)
                                    },
                                    onTransferTable = { from, to ->
                                        viewModel.transferTable(from, to)
                                    }
                                )
                            }
                            AppScreen.KITCHEN -> {
                                KitchenScreen(
                                    orders = activeKitchenOrders,
                                    onUpdateItemStatus = { id, status -> viewModel.setKitchenItemStatus(id, status) },
                                    onMarkOrderReady = { viewModel.markOrderReady(it) },
                                    onCompleteOrder = { viewModel.completeOrder(it) }
                                )
                            }
                            AppScreen.ORDERS -> {
                                OrdersScreen(
                                    orders = allOrders,
                                    onReprintReceipt = { orderWithItems ->
                                        viewModel.showReceipt(orderWithItems)
                                    },
                                    onRefundOrder = { id, reason ->
                                        viewModel.refundOrder(id, reason)
                                    },
                                    onCancelOrder = { id, reason ->
                                        viewModel.cancelOrder(id, reason)
                                    }
                                )
                            }
                            AppScreen.MENU_MANAGEMENT -> {
                                MenuManagementScreen(
                                    products = allProducts,
                                    categories = categories,
                                    inventoryItems = inventoryItems,
                                    allRecipeItems = allRecipeItems,
                                    onSaveProduct = { product, recipes ->
                                        viewModel.saveProduct(product, recipes)
                                    },
                                    onToggleAvailability = { id, available ->
                                        viewModel.toggleProductAvailability(id, available)
                                    },
                                    onDeleteProduct = { product ->
                                        viewModel.deleteProduct(product)
                                    },
                                    onLoadRecipe = { productId, onLoaded ->
                                        viewModel.loadProductRecipe(productId, onLoaded)
                                    }
                                )
                            }
                            AppScreen.INVENTORY -> {
                                InventoryScreen(
                                    inventoryItems = inventoryItems,
                                    onRestock = { id, qty, cost -> viewModel.restock(id, qty, cost) },
                                    onRecordWaste = { id, qty, reason -> viewModel.recordWaste(id, qty, reason) },
                                    onRecordPhysicalStockCount = { id, stock, notes -> viewModel.recordPhysicalStockCount(id, stock, notes) }
                                )
                            }
                            AppScreen.REPORTS -> {
                                ReportsScreen(
                                    orders = allOrders,
                                    expenses = expenses
                                )
                            }
                            AppScreen.CASH_REGISTER -> {
                                CashRegisterScreen(
                                    currentSession = currentSession,
                                    onOpenShift = { amount, notes -> viewModel.openShift(amount, notes) }
                                )
                            }
                            AppScreen.EXPENSES -> {
                                ExpensesScreen(
                                    expenses = expenses,
                                    onAddExpense = { cat, amt, desc -> viewModel.logExpense(cat, amt, desc) }
                                )
                            }
                            AppScreen.SETTINGS -> {
                                SettingsScreen(
                                    currentLanguage = currentLanguage,
                                    auditLogs = auditLogs,
                                    onLanguageChange = { viewModel.setLanguage(it) }
                                )
                            }
                            AppScreen.LOGIN -> {}
                        }

                        // Floating Feedback Toast Banner
                        if (feedbackMessage != null) {
                            Surface(
                                color = GoldPrimary,
                                shape = RoundedCornerShape(10.dp),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 20.dp, start = 20.dp, end = 20.dp)
                            ) {
                                Text(
                                    text = feedbackMessage!!,
                                    color = Color.Black,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Payment Dialog
    if (showPaymentDialog) {
        PaymentDialog(
            totalAmount = cartState.total,
            onDismiss = { viewModel.closePaymentDialog() },
            onConfirmPayment = { method, received, change ->
                viewModel.processCartPayment(method, received, change)
            }
        )
    }

    // Receipt Preview Dialog (Displayed after payment or on reprint)
    if (lastPaidReceipt != null) {
        ReceiptDialog(
            orderWithItems = lastPaidReceipt!!,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}
