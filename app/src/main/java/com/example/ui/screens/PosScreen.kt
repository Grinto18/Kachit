package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.OrderWithItems
import com.example.data.entity.CategoryEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.TableEntity
import com.example.i18n.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.CartItem
import com.example.ui.viewmodel.CartState
import java.text.DecimalFormat

@Composable
fun PosScreen(
    categories: List<CategoryEntity>,
    products: List<ProductEntity>,
    tables: List<TableEntity>,
    cartState: CartState,
    selectedCategoryId: String?,
    searchQuery: String,
    onCategorySelect: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onBarcodeScan: (String) -> Unit,
    onAddToCart: (ProductEntity) -> Unit,
    onIncreaseQty: (String) -> Unit,
    onDecreaseQty: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onUpdateItemNote: (String, String) -> Unit,
    onOrderTypeChange: (String) -> Unit,
    onSelectTable: (TableEntity?) -> Unit,
    onCustomerDetailsChange: (String, String, String) -> Unit,
    onClearCart: () -> Unit,
    onHoldOrder: () -> Unit = {},
    onResumeHeldOrder: (OrderWithItems) -> Unit = {},
    onDeleteHeldOrder: (String) -> Unit = {},
    onSendToKitchen: () -> Unit,
    onOpenPayment: () -> Unit,
    heldOrders: List<OrderWithItems> = emptyList()
) {
    var showTablePicker by remember { mutableStateOf(false) }
    var showDeliveryDialog by remember { mutableStateOf(false) }
    var showHeldOrdersDialog by remember { mutableStateOf(false) }
    var showNoteDialogForItem by remember { mutableStateOf<CartItem?>(null) }
    var itemNoteText by remember { mutableStateOf("") }

    val currencyFormat = DecimalFormat("#,##0")

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        val isWide = maxWidth >= 840.dp

        if (isWide) {
            // Tablet / Desktop 2-column layout (Left: Products 65%, Right: Cart 35%)
            Row(modifier = Modifier.fillMaxSize()) {
                // Products Catalog Column
                Column(
                    modifier = Modifier
                        .weight(0.64f)
                        .fillMaxHeight()
                        .padding(12.dp)
                ) {
                    PosHeaderSearchBar(
                        searchQuery = searchQuery,
                        onSearchChange = onSearchChange,
                        onBarcodeScan = onBarcodeScan
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PosCategoryTabs(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelect = onCategorySelect
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PosProductGrid(
                        products = products,
                        onAddToCart = onAddToCart,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(DarkBorder)
                )

                // Current Order Cart Column
                Column(
                    modifier = Modifier
                        .weight(0.36f)
                        .fillMaxHeight()
                        .background(DarkSurface)
                        .padding(14.dp)
                ) {
                    PosCartSection(
                        cartState = cartState,
                        heldOrdersCount = heldOrders.size,
                        onOrderTypeChange = onOrderTypeChange,
                        onOpenTablePicker = { showTablePicker = true },
                        onOpenDeliveryDialog = { showDeliveryDialog = true },
                        onOpenHeldOrdersDialog = { showHeldOrdersDialog = true },
                        onIncreaseQty = onIncreaseQty,
                        onDecreaseQty = onDecreaseQty,
                        onRemoveItem = onRemoveItem,
                        onOpenNoteDialog = { item ->
                            showNoteDialogForItem = item
                            itemNoteText = item.note
                        },
                        onClearCart = onClearCart,
                        onHoldOrder = onHoldOrder,
                        onSendToKitchen = onSendToKitchen,
                        onOpenPayment = onOpenPayment
                    )
                }
            }
        } else {
            // Mobile Compact Layout: Vertical split
            Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                PosHeaderSearchBar(
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    onBarcodeScan = onBarcodeScan
                )

                Spacer(modifier = Modifier.height(6.dp))

                PosCategoryTabs(
                    categories = categories,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelect = onCategorySelect
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.weight(1f)) {
                    PosProductGrid(
                        products = products,
                        onAddToCart = onAddToCart,
                        columns = 2
                    )
                }

                // Bottom Cart Summary Bar
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${cartState.totalItemCount} أصناف في السلة",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Text(
                                text = "${currencyFormat.format(cartState.total)} دج",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onSendToKitchen,
                                enabled = cartState.items.isNotEmpty(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = GoldPrimary
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("للمطبخ", fontSize = 12.sp)
                            }

                            Button(
                                onClick = onOpenPayment,
                                enabled = cartState.items.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("الدفع", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Table Picker Dialog
    if (showTablePicker) {
        AlertDialog(
            onDismissRequest = { showTablePicker = false },
            title = {
                Text(
                    text = "اختيار الطاولة للطلب المحلي",
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            },
            text = {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tables) { table ->
                        val isCurrentSelected = cartState.selectedTable?.id == table.id
                        val isAvailable = table.status == "AVAILABLE"
                        val bgColor = when {
                            isCurrentSelected -> GoldPrimary
                            table.status == "OCCUPIED" -> StatusAmberBg
                            table.status == "WAITING_PAYMENT" -> StatusRedBg
                            else -> DarkSurfaceVariant
                        }
                        val textColor = when {
                            isCurrentSelected -> Color.Black
                            table.status == "OCCUPIED" -> StatusAmber
                            table.status == "WAITING_PAYMENT" -> StatusRed
                            else -> TextWhite
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = bgColor,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isCurrentSelected) GoldLight else DarkBorder
                            ),
                            modifier = Modifier
                                .height(70.dp)
                                .clickable {
                                    onSelectTable(table)
                                    showTablePicker = false
                                }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(4.dp)
                            ) {
                                Text(
                                    text = "طاولة ${table.tableNumber}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = textColor
                                )
                                Text(
                                    text = if (isAvailable) "متاحة (${table.capacity} ك)" else table.status,
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTablePicker = false }) {
                    Text("إغلاق", color = GoldPrimary)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Delivery Info Dialog
    if (showDeliveryDialog) {
        var cName by remember { mutableStateOf(cartState.customerName) }
        var cPhone by remember { mutableStateOf(cartState.customerPhone) }
        var cAddress by remember { mutableStateOf(cartState.deliveryAddress) }

        AlertDialog(
            onDismissRequest = { showDeliveryDialog = false },
            title = {
                Text(
                    text = "بيانات عميل التوصيل (Delivery)",
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cName,
                        onValueChange = { cName = it },
                        label = { Text("اسم العميل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cPhone,
                        onValueChange = { cPhone = it },
                        label = { Text("رقم الهاتف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cAddress,
                        onValueChange = { cAddress = it },
                        label = { Text("عنوان التوصيل (الشارع، الحي، المعلم)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCustomerDetailsChange(cName, cPhone, cAddress)
                        showDeliveryDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("حفظ البيانات")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeliveryDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Item Note Dialog
    if (showNoteDialogForItem != null) {
        val item = showNoteDialogForItem!!
        AlertDialog(
            onDismissRequest = { showNoteDialogForItem = null },
            title = { Text("ملاحظة للمطبخ: ${item.product.nameAr}", color = GoldLight, fontSize = 15.sp) },
            text = {
                OutlinedTextField(
                    value = itemNoteText,
                    onValueChange = { itemNoteText = it },
                    placeholder = { Text("مثلاً: بدون بصل، مشوي جيداً، صوص حار منفصل...") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateItemNote(item.product.id, itemNoteText)
                        showNoteDialogForItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("حفظ الملاحظة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialogForItem = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Held Orders Dialog
    if (showHeldOrdersDialog) {
        AlertDialog(
            onDismissRequest = { showHeldOrdersDialog = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PauseCircle, contentDescription = null, tint = StatusAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الطلبات المعلقة (${heldOrders.size})",
                            fontWeight = FontWeight.Bold,
                            color = GoldLight,
                            fontSize = 17.sp
                        )
                    }
                    IconButton(onClick = { showHeldOrdersDialog = false }) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }
            },
            text = {
                if (heldOrders.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد طلبات معلقة حالياً", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(heldOrders, key = { it.order.id }) { held ->
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = held.order.orderNumber,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldLight,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "${held.items.size} أصناف • ${held.order.total} دج" + (if (held.order.tableNumber != null) " • طاولة ${held.order.tableNumber}" else ""),
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Button(
                                            onClick = {
                                                onResumeHeldOrder(held)
                                                showHeldOrdersDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("استئناف", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(
                                            onClick = { onDeleteHeldOrder(held.order.id) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "حذف", tint = StatusRed, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showHeldOrdersDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun PosHeaderSearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBarcodeScan: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text(AppStrings.get("search_hint"), color = TextMuted, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        onBarcodeScan(searchQuery)
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            ),
            modifier = Modifier.weight(1f)
        )

        // Barcode trigger quick button
        IconButton(
            onClick = {
                if (searchQuery.isNotBlank()) onBarcodeScan(searchQuery)
            },
            modifier = Modifier
                .size(52.dp)
                .background(GoldContainer, RoundedCornerShape(10.dp))
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan",
                tint = GoldPrimary
            )
        }
    }
}

@Composable
fun PosCategoryTabs(
    categories: List<CategoryEntity>,
    selectedCategoryId: String?,
    onCategorySelect: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategoryId == null,
                onClick = { onCategorySelect(null) },
                label = { Text("الكل (جميع الأصناف)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextWhite
                )
            )
        }

        items(categories) { cat ->
            val isSelected = selectedCategoryId == cat.id
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelect(cat.id) },
                label = { Text(cat.nameAr, fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = DarkSurfaceVariant,
                    labelColor = TextWhite
                )
            )
        }
    }
}

@Composable
fun PosProductGrid(
    products: List<ProductEntity>,
    onAddToCart: (ProductEntity) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3
) {
    val currencyFormat = DecimalFormat("#,##0")

    if (products.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد أطباق متطابقة مع البحث الحالي",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products, key = { it.id }) { product ->
                ProductCard(
                    product = product,
                    currencyFormat = currencyFormat,
                    onAddToCart = { onAddToCart(product) }
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: ProductEntity,
    currencyFormat: DecimalFormat,
    onAddToCart: () -> Unit
) {
    val stationBadgeColor = when (product.kitchenStation) {
        "GRILL" -> StatusAmber
        "DRINKS" -> StatusBlue
        "DESSERT" -> StatusPurple
        else -> GoldPrimary
    }

    val stationLabel = when (product.kitchenStation) {
        "GRILL" -> "مشاوي"
        "DRINKS" -> "مشروبات"
        "DESSERT" -> "تحليات"
        else -> "المطبخ"
    }

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onAddToCart() }
            .testTag("product_card_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top row: Station badge and category icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = stationBadgeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = stationLabel,
                        fontSize = 10.sp,
                        color = stationBadgeColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add",
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Center: Dish name in Arabic & French
            Column {
                Text(
                    text = product.nameAr,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextWhite,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = product.nameFr,
                    fontSize = 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Bottom: Price in DZD
            Text(
                text = "${currencyFormat.format(product.price)} دج",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = GoldLight
            )
        }
    }
}

@Composable
fun PosCartSection(
    cartState: CartState,
    heldOrdersCount: Int = 0,
    onOrderTypeChange: (String) -> Unit,
    onOpenTablePicker: () -> Unit,
    onOpenDeliveryDialog: () -> Unit,
    onOpenHeldOrdersDialog: () -> Unit = {},
    onIncreaseQty: (String) -> Unit,
    onDecreaseQty: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onOpenNoteDialog: (CartItem) -> Unit,
    onClearCart: () -> Unit,
    onHoldOrder: () -> Unit = {},
    onSendToKitchen: () -> Unit,
    onOpenPayment: () -> Unit
) {
    val currencyFormat = DecimalFormat("#,##0")

    Column(modifier = Modifier.fillMaxSize()) {
        // Order Type Selector Tabs & Held Orders Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val types = listOf(
                "DINE_IN" to AppStrings.get("dine_in"),
                "TAKEAWAY" to AppStrings.get("takeaway"),
                "DELIVERY" to AppStrings.get("delivery")
            )
            types.forEach { (typeKey, typeLabel) ->
                val isSelected = cartState.orderType == typeKey
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) GoldPrimary else DarkSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOrderTypeChange(typeKey) }
                ) {
                    Text(
                        text = typeLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else TextWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            // Held Orders Quick Button with badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (heldOrdersCount > 0) StatusAmberBg else DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (heldOrdersCount > 0) StatusAmber else DarkBorder
                ),
                modifier = Modifier.clickable { onOpenHeldOrdersDialog() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PauseCircle,
                        contentDescription = "معلق",
                        tint = if (heldOrdersCount > 0) StatusAmber else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    if (heldOrdersCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$heldOrdersCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusAmber
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Table / Delivery Info Row
        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (cartState.orderType == "DINE_IN") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.TableRestaurant, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (cartState.selectedTable != null) "طاولة ${cartState.selectedTable.tableNumber}" else "لم يتم تحديد طاولة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    TextButton(
                        onClick = onOpenTablePicker,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("تغيير الطاولة", color = GoldPrimary, fontSize = 11.sp)
                    }
                } else if (cartState.orderType == "DELIVERY") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.DeliveryDining, contentDescription = null, tint = StatusBlue, modifier = Modifier.size(18.dp))
                        Text(
                            text = if (cartState.customerName.isNotBlank()) cartState.customerName else "عميل التوصيل",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    TextButton(
                        onClick = onOpenDeliveryDialog,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("تعديل العنوان", color = GoldPrimary, fontSize = 11.sp)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(18.dp))
                        Text(
                            text = "طلب سفري خارجي (Takeaway)",
                            fontSize = 12.sp,
                            color = TextWhite
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cart Items List
        if (cartState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Empty",
                        tint = DarkBorder,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppStrings.get("empty_cart"),
                        color = TextMuted,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(cartState.items, key = { it.product.id }) { item ->
                    CartItemRow(
                        item = item,
                        currencyFormat = currencyFormat,
                        onIncrease = { onIncreaseQty(item.product.id) },
                        onDecrease = { onDecreaseQty(item.product.id) },
                        onRemove = { onRemoveItem(item.product.id) },
                        onAddNote = { onOpenNoteDialog(item) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Financial Calculation Breakdown
        Surface(
            color = DarkSurfaceVariant,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = AppStrings.get("subtotal"), fontSize = 12.sp, color = TextMuted)
                    Text(text = "${currencyFormat.format(cartState.subtotal)} دج", fontSize = 12.sp, color = TextWhite)
                }

                if (cartState.discountAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = AppStrings.get("discount"), fontSize = 12.sp, color = StatusGreen)
                        Text(text = "-${currencyFormat.format(cartState.discountAmount)} دج", fontSize = 12.sp, color = StatusGreen)
                    }
                }

                if (cartState.deliveryFee > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "رسوم التوصيل", fontSize = 12.sp, color = TextMuted)
                        Text(text = "+${currencyFormat.format(cartState.deliveryFee)} دج", fontSize = 12.sp, color = TextWhite)
                    }
                }

                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.get("total"),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "${currencyFormat.format(cartState.total)} دج",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Big Touch Action Buttons: [مسح] [تعليق] [إرسال للمطبخ] [الدفع]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = onClearCart,
                enabled = cartState.items.isNotEmpty(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                modifier = Modifier.weight(0.18f).height(50.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(18.dp))
            }

            OutlinedButton(
                onClick = onHoldOrder,
                enabled = cartState.items.isNotEmpty(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusAmber),
                border = androidx.compose.foundation.BorderStroke(1.dp, StatusAmber),
                modifier = Modifier.weight(0.24f).height(50.dp)
            ) {
                Text("تعليق", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onSendToKitchen,
                enabled = cartState.items.isNotEmpty(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                modifier = Modifier.weight(0.29f).height(50.dp)
            ) {
                Text("للمطبخ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOpenPayment,
                enabled = cartState.items.isNotEmpty(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                modifier = Modifier.weight(0.29f).height(50.dp)
            ) {
                Text(
                    text = "دفع",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    currencyFormat: DecimalFormat,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onAddNote: () -> Unit
) {
    Surface(
        color = DarkSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.nameAr,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${currencyFormat.format(item.unitPrice)} دج × ${item.quantity}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = "${currencyFormat.format(item.total)} دج",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GoldLight
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Actions: [-] [Qty] [+] and [Add Note] and [Remove]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DarkBorder)
                            .clickable { onDecrease() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("-", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Text(
                        text = "${item.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(GoldContainer)
                            .clickable { onIncrease() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Note pill or button
                    Surface(
                        color = if (item.note.isNotBlank()) GoldContainer else DarkBackground,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.clickable { onAddNote() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                Icons.Default.EditNote,
                                contentDescription = "Note",
                                tint = if (item.note.isNotBlank()) GoldLight else TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (item.note.isNotBlank()) item.note.take(10) else "ملاحظة",
                                fontSize = 10.sp,
                                color = if (item.note.isNotBlank()) GoldLight else TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = StatusRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
