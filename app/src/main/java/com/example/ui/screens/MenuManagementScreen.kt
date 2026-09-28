package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.CategoryEntity
import com.example.data.entity.InventoryItemEntity
import com.example.data.entity.ProductEntity
import com.example.data.entity.RecipeItemEntity
import com.example.i18n.AppStrings
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.util.UUID

@Composable
fun MenuManagementScreen(
    products: List<ProductEntity>,
    categories: List<CategoryEntity>,
    inventoryItems: List<InventoryItemEntity>,
    allRecipeItems: List<RecipeItemEntity>,
    onSaveProduct: (ProductEntity, List<RecipeItemEntity>) -> Unit,
    onToggleAvailability: (String, Boolean) -> Unit,
    onDeleteProduct: (ProductEntity) -> Unit,
    onLoadRecipe: (String, (List<RecipeItemEntity>) -> Unit) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var availabilityFilter by remember { mutableStateOf("ALL") } // ALL, AVAILABLE, UNAVAILABLE

    var showProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var editingRecipeItems by remember { mutableStateOf<List<RecipeItemEntity>>(emptyList()) }

    var productToDelete by remember { mutableStateOf<ProductEntity?>(null) }

    val currencyFormat = remember { DecimalFormat("#,##0") }

    // Filtered products list
    val filteredProducts = remember(products, searchQuery, selectedCategoryId, availabilityFilter) {
        products.filter { prod ->
            val matchesCategory = selectedCategoryId == null || prod.categoryId == selectedCategoryId
            val matchesSearch = searchQuery.isBlank() ||
                    prod.nameAr.contains(searchQuery, ignoreCase = true) ||
                    prod.nameFr.contains(searchQuery, ignoreCase = true) ||
                    prod.nameEn.contains(searchQuery, ignoreCase = true) ||
                    prod.barcode.contains(searchQuery, ignoreCase = true)
            val matchesAvailability = when (availabilityFilter) {
                "AVAILABLE" -> prod.available
                "UNAVAILABLE" -> !prod.available
                else -> true
            }
            matchesCategory && matchesSearch && matchesAvailability
        }
    }

    val availableCount = remember(products) { products.count { it.available } }
    val unavailableCount = remember(products) { products.count { !it.available } }
    val withRecipeCount = remember(products, allRecipeItems) {
        val productIdsWithRecipe = allRecipeItems.map { it.productId }.toSet()
        products.count { productIdsWithRecipe.contains(it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp)
    ) {
        // --- 1. Top Header & Stats ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.get("menu_management"),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "إدارة كاملة للمأكولات، الأسعار، التكلفة، أقسام المطبخ وربط المخزون",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }

            // Add Product Button
            Button(
                onClick = {
                    editingProduct = null
                    editingRecipeItems = emptyList()
                    showProductDialog = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_product_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = AppStrings.get("add_product"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. Stats summary bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MenuStatBadge(
                title = "إجمالي الأطباق",
                count = "${products.size}",
                icon = Icons.Default.Fastfood,
                tint = GoldPrimary,
                modifier = Modifier.weight(1f)
            )
            MenuStatBadge(
                title = "المتوفر في الكاشير",
                count = "$availableCount",
                icon = Icons.Default.CheckCircle,
                tint = StatusGreen,
                modifier = Modifier.weight(1f)
            )
            MenuStatBadge(
                title = "المعطل والمخفي",
                count = "$unavailableCount",
                icon = Icons.Default.VisibilityOff,
                tint = StatusAmber,
                modifier = Modifier.weight(1f)
            )
            MenuStatBadge(
                title = "مرتبط بالمخزون",
                count = "$withRecipeCount",
                icon = Icons.Default.Inventory2,
                tint = StatusBlue,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. Search and Filters Bar ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث بالاسم، التصنيف، أو الباركود...", color = TextMuted, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
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

            // Availability Filter Chips
            FilterChip(
                selected = availabilityFilter == "ALL",
                onClick = { availabilityFilter = "ALL" },
                label = { Text("الكل", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = Color.Black,
                    containerColor = DarkSurface,
                    labelColor = TextWhite
                )
            )
            FilterChip(
                selected = availabilityFilter == "AVAILABLE",
                onClick = { availabilityFilter = "AVAILABLE" },
                label = { Text("متوفر", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusGreen,
                    selectedLabelColor = Color.Black,
                    containerColor = DarkSurface,
                    labelColor = TextWhite
                )
            )
            FilterChip(
                selected = availabilityFilter == "UNAVAILABLE",
                onClick = { availabilityFilter = "UNAVAILABLE" },
                label = { Text("معطل / مخفي", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = StatusAmber,
                    selectedLabelColor = Color.Black,
                    containerColor = DarkSurface,
                    labelColor = TextWhite
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Horizontal Slider
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCategoryId == null,
                    onClick = { selectedCategoryId = null },
                    label = { Text("جميع الأصناف (${products.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextWhite
                    )
                )
            }
            items(categories) { cat ->
                val catCount = products.count { it.categoryId == cat.id }
                val isSelected = selectedCategoryId == cat.id
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryId = cat.id },
                    label = { Text("${cat.nameAr} ($catCount)", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = Color.Black,
                        containerColor = DarkSurfaceVariant,
                        labelColor = TextWhite
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 4. Products List ---
        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لا توجد أطباق متطابقة مع شروط البحث",
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts, key = { it.id }) { product ->
                    val category = categories.find { it.id == product.categoryId }
                    val productRecipes = allRecipeItems.filter { it.productId == product.id }

                    ProductManagementRow(
                        product = product,
                        categoryName = category?.nameAr ?: "غير محدد",
                        recipeCount = productRecipes.size,
                        currencyFormat = currencyFormat,
                        onToggleAvailability = { isAvail ->
                            onToggleAvailability(product.id, isAvail)
                        },
                        onEdit = {
                            editingProduct = product
                            onLoadRecipe(product.id) { recipes ->
                                editingRecipeItems = recipes
                                showProductDialog = true
                            }
                        },
                        onDelete = {
                            productToDelete = product
                        }
                    )
                }
            }
        }
    }

    // --- Product Add/Edit Dialog ---
    if (showProductDialog) {
        ProductFormDialog(
            initialProduct = editingProduct,
            initialRecipes = editingRecipeItems,
            categories = categories,
            inventoryItems = inventoryItems,
            onDismiss = { showProductDialog = false },
            onSave = { product, recipes ->
                onSaveProduct(product, recipes)
                showProductDialog = false
            }
        )
    }

    // --- Delete Confirmation Dialog ---
    if (productToDelete != null) {
        val prod = productToDelete!!
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusAmber,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "إلغاء أو حذف: ${prod.nameAr}",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "وفقاً للنظام المحاسبي لمطعم القصر الذهبي:",
                        color = GoldLight,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "• إذا كان هذا الطبق مستخدماً في طلبات أو فواتير سابقة، سيتم تعطيله وإخفاؤه من شاشة الكاشير فوراً، مع الاحتفاظ ببياناته التاريخية في التقارير المالية والجبائية.",
                        color = TextWhite,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "• إذا كان المنتج جديداً ولم يتم استخدامه في أي طلب بيع، فسيتم حذفه نهائياً من قاعدة البيانات المحلية.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProduct(prod)
                        productToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusRed,
                        contentColor = TextWhite
                    )
                ) {
                    Text("تأكيد العملية")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToDelete = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
fun MenuStatBadge(
    title: String,
    count: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = count, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                Text(text = title, fontSize = 10.sp, color = TextMuted)
            }
        }
    }
}

@Composable
fun ProductManagementRow(
    product: ProductEntity,
    categoryName: String,
    recipeCount: Int,
    currencyFormat: DecimalFormat,
    onToggleAvailability: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val stationBadgeColor = when (product.kitchenStation) {
        "GRILL" -> StatusAmber
        "DRINKS" -> StatusBlue
        "DESSERT" -> StatusPurple
        else -> GoldPrimary
    }

    val stationLabel = when (product.kitchenStation) {
        "GRILL" -> "المشاوي"
        "DRINKS" -> "المشروبات"
        "DESSERT" -> "الحلويات"
        else -> "المطبخ الرئيسي"
    }

    val profitMargin = if (product.price > 0 && product.cost > 0) {
        (((product.price - product.cost) / product.price) * 100).toInt()
    } else 0

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (product.available) DarkBorder else StatusAmber.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Food Icon / Visual representation
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getFoodIcon(product.iconName),
                    contentDescription = null,
                    tint = if (product.available) GoldPrimary else TextMuted,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Names & Category
            Column(modifier = Modifier.weight(1.3f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.nameAr,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (product.available) TextWhite else TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!product.available) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = StatusAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "مخفي من الكاشير",
                                color = StatusAmber,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.nameFr.isNotBlank()) {
                        Text(
                            text = product.nameFr,
                            fontSize = 11.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (product.barcode.isNotBlank()) {
                        Text(
                            text = "• [${product.barcode}]",
                            fontSize = 10.sp,
                            color = GoldLight.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Category & Station Badges
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start
            ) {
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = categoryName,
                        fontSize = 11.sp,
                        color = TextWhite,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(stationBadgeColor)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stationLabel,
                        fontSize = 10.sp,
                        color = stationBadgeColor
                    )
                    if (recipeCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• $recipeCount مكونات",
                            fontSize = 10.sp,
                            color = StatusBlue
                        )
                    }
                }
            }

            // Price, Cost, Margin
            Column(
                modifier = Modifier.weight(0.9f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${currencyFormat.format(product.price)} دج",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary
                )
                if (product.cost > 0) {
                    Text(
                        text = "التكلفة: ${currencyFormat.format(product.cost)} دج (+$profitMargin%)",
                        fontSize = 10.sp,
                        color = StatusGreen
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Actions: Quick Toggle Available, Edit, Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Switch / Toggle availability
                Switch(
                    checked = product.available,
                    onCheckedChange = { onToggleAvailability(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = GoldPrimary,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.scale(0.8f)
                )

                // Edit Button
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل",
                        tint = GoldLight,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete Button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف",
                        tint = StatusRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductFormDialog(
    initialProduct: ProductEntity?,
    initialRecipes: List<RecipeItemEntity>,
    categories: List<CategoryEntity>,
    inventoryItems: List<InventoryItemEntity>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity, List<RecipeItemEntity>) -> Unit
) {
    val isEditing = initialProduct != null

    var nameAr by remember { mutableStateOf(initialProduct?.nameAr ?: "") }
    var nameFr by remember { mutableStateOf(initialProduct?.nameFr ?: "") }
    var nameEn by remember { mutableStateOf(initialProduct?.nameEn ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(initialProduct?.categoryId ?: categories.firstOrNull()?.id ?: "")
    }
    var priceText by remember { mutableStateOf(initialProduct?.price?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var costText by remember { mutableStateOf(initialProduct?.cost?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var kitchenStation by remember { mutableStateOf(initialProduct?.kitchenStation ?: "MAIN_KITCHEN") }
    var isAvailable by remember { mutableStateOf(initialProduct?.available ?: true) }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }
    var selectedIconName by remember { mutableStateOf(initialProduct?.iconName ?: "dinner_dining") }

    // Recipe Ingredients List
    var recipeItems by remember { mutableStateOf(initialRecipes.toMutableList()) }

    // Temporary input for adding recipe ingredient
    var selectedInventoryItemId by remember { mutableStateOf(inventoryItems.firstOrNull()?.id ?: "") }
    var recipeQuantityText by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Photo picker launcher (0-permission Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedIconName = uri.toString()
        }
    }

    val availableIcons = listOf(
        "dinner_dining" to "طبق رئيسي",
        "outdoor_grill" to "مشاوي",
        "set_meal" to "وجبة سمك/لحم",
        "soup_kitchen" to "شوربة/مرق",
        "lunch_dining" to "برجر/ساندويتش",
        "local_pizza" to "بيتزا/فطائر",
        "bakery_dining" to "خبز/عجائن",
        "cake" to "حلويات",
        "local_bar" to "مشروبات"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.94f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isEditing) "تعديل طبق: ${initialProduct?.nameAr}" else "إضافة صنف جديد لقائمة الطعام",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }
                }

                // Scrollable Form Content
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Error Notice
                    if (errorMessage != null) {
                        item {
                            Surface(
                                color = StatusRedBg,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusRed)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = errorMessage!!, color = StatusRed, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    // Section 1: Names (AR, FR, EN)
                    item {
                        Text(
                            text = "1. أسماء الطبق / المنتَج",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = nameAr,
                                onValueChange = { nameAr = it },
                                label = { Text("الاسم بالعربية * (مثال: شواء مشكل)") },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                colors = getFormTextFieldColors()
                            )
                            OutlinedTextField(
                                value = nameFr,
                                onValueChange = { nameFr = it },
                                label = { Text("بالفرنسية (Grillade Mixte)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = getFormTextFieldColors()
                            )
                            OutlinedTextField(
                                value = nameEn,
                                onValueChange = { nameEn = it },
                                label = { Text("بالإنجليزية (Mixed Grill)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = getFormTextFieldColors()
                            )
                        }
                    }

                    // Section 2: Category, Price, Cost, Station
                    item {
                        Text(
                            text = "2. التصنيف والأسعار وقسم المطبخ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Category Selection Chips
                        Text(text = "اختر التصنيف:", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategoryId == cat.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategoryId = cat.id },
                                    label = { Text(cat.nameAr, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = GoldPrimary,
                                        selectedLabelColor = Color.Black,
                                        containerColor = DarkSurfaceVariant,
                                        labelColor = TextWhite
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) priceText = it },
                                label = { Text("سعر البيع (دج) *") },
                                trailingIcon = { Text("دج", color = GoldPrimary, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = getFormTextFieldColors()
                            )

                            OutlinedTextField(
                                value = costText,
                                onValueChange = { if (it.all { ch -> ch.isDigit() }) costText = it },
                                label = { Text("التكلفة التقديرية (دج)") },
                                trailingIcon = { Text("دج", color = StatusGreen, fontSize = 12.sp, modifier = Modifier.padding(end = 8.dp)) },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = getFormTextFieldColors()
                            )

                            // Barcode optional with generator
                            OutlinedTextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                label = { Text("الباركود (اختياري)") },
                                trailingIcon = {
                                    IconButton(onClick = {
                                        barcode = "613" + (100000000..999999999).random().toString()
                                    }) {
                                        Icon(Icons.Default.QrCode, contentDescription = "توليد باركود", tint = GoldLight)
                                    }
                                },
                                singleLine = true,
                                modifier = Modifier.weight(1.2f),
                                colors = getFormTextFieldColors()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Kitchen Station Selector
                        Text(text = "القسم المستلم للطلب (KDS):", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val stations = listOf(
                                "MAIN_KITCHEN" to "المطبخ الرئيسي",
                                "GRILL" to "قسم المشاوي",
                                "DRINKS" to "بار المشروبات",
                                "DESSERT" to "ركن الحلويات"
                            )
                            stations.forEach { (code, title) ->
                                val isSelected = kitchenStation == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { kitchenStation = code },
                                    label = { Text(title, fontSize = 12.sp) },
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

                    // Section 3: Availability & Visual Icon/Image
                    item {
                        Text(
                            text = "3. التوفر والمظهر المرئي",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isAvailable) "المنتج متوفر ومفعّل للبيع" else "المنتج غير متوفر (مخفي من شاشة الكاشير)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isAvailable) StatusGreen else StatusAmber,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "عند التعطيل لن يظهر للكاشير في قائمة البيع، ويبقى محفوظاً بالتقارير السابقة.",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            Switch(
                                checked = isAvailable,
                                onCheckedChange = { isAvailable = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.Black,
                                    checkedTrackColor = GoldPrimary,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = DarkSurface
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Icon / Photo Picker
                        Text(text = "أيقونة أو صورة الطبق:", fontSize = 12.sp, color = TextMuted)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Selected icon preview
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getFoodIcon(selectedIconName),
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            // Pick Photo button
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight)
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("رفع صورة محلياً", fontSize = 12.sp)
                            }

                            // Preset icon chips
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(availableIcons) { (iconKey, label) ->
                                    val isSelected = selectedIconName == iconKey
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedIconName = iconKey },
                                        label = { Text(label, fontSize = 11.sp) },
                                        leadingIcon = {
                                            Icon(getFoodIcon(iconKey), contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("وصف الطبق والمكونات الإضافية (اختياري)") },
                            placeholder = { Text("مثال: قطع لحم طازجة مشوية على الفحم مع توابل جزائرية أصيلة وبطاطا مقلية...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3,
                            colors = getFormTextFieldColors()
                        )
                    }

                    // Section 4: Recipe & Inventory Link (خصم تلقائي من المخزون)
                    item {
                        Surface(
                            color = DarkSurfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusBlue.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = null,
                                            tint = StatusBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "4. وصفة المكونات والخصم الآلي من المخزون",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = StatusBlue
                                        )
                                    }

                                    Text(
                                        text = "${recipeItems.size} مكونات مضافة",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "عند بيع هذا الطبق، يقوم نظام القصر الذهبي بخصم هذه المقادير من المخزون تلقائياً بدون تدخل يدوي.",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Add ingredient row
                                if (inventoryItems.isNotEmpty()) {
                                    val selectedInventory = inventoryItems.find { it.id == selectedInventoryItemId }
                                        ?: inventoryItems.first()

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Item dropdown chip / preview
                                        Box(modifier = Modifier.weight(1.4f)) {
                                            var expanded by remember { mutableStateOf(false) }
                                            OutlinedButton(
                                                onClick = { expanded = true },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "${selectedInventory.nameAr} (${selectedInventory.unit})",
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                            }

                                            DropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false },
                                                modifier = Modifier.background(DarkSurface)
                                            ) {
                                                inventoryItems.forEach { invItem ->
                                                    DropdownMenuItem(
                                                        text = {
                                                            Text(
                                                                text = "${invItem.nameAr} (${invItem.unit}) - متوفر: ${invItem.currentStock}",
                                                                color = TextWhite,
                                                                fontSize = 13.sp
                                                            )
                                                        },
                                                        onClick = {
                                                            selectedInventoryItemId = invItem.id
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Quantity needed input
                                        OutlinedTextField(
                                            value = recipeQuantityText,
                                            onValueChange = { recipeQuantityText = it },
                                            label = { Text("الكمية (${selectedInventory.unit})") },
                                            placeholder = { Text("0.25") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1f),
                                            colors = getFormTextFieldColors()
                                        )

                                        // Add button
                                        Button(
                                            onClick = {
                                                val qty = recipeQuantityText.toDoubleOrNull()
                                                if (qty != null && qty > 0) {
                                                    val newItem = RecipeItemEntity(
                                                        id = UUID.randomUUID().toString(),
                                                        productId = initialProduct?.id ?: "",
                                                        inventoryItemId = selectedInventory.id,
                                                        quantityNeeded = qty,
                                                        unit = selectedInventory.unit
                                                    )
                                                    recipeItems = (recipeItems + newItem).toMutableList()
                                                    recipeQuantityText = ""
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = StatusBlue,
                                                contentColor = TextWhite
                                            )
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("إضافة", fontSize = 12.sp)
                                        }
                                    }
                                }

                                // List of current added recipe ingredients
                                if (recipeItems.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        recipeItems.forEach { item ->
                                            val inv = inventoryItems.find { it.id == item.inventoryItemId }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(DarkSurface, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = StatusGreen,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = inv?.nameAr ?: "عنصر مخزون",
                                                        fontSize = 13.sp,
                                                        color = TextWhite,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = "${item.quantityNeeded} ${item.unit}",
                                                        fontSize = 12.sp,
                                                        color = GoldLight
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        recipeItems = recipeItems.filterNot { it.id == item.id }.toMutableList()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.DeleteOutline,
                                                        contentDescription = "إزالة",
                                                        tint = StatusRed,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Footer Buttons: Cancel & Save
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = TextMuted, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (nameAr.isBlank()) {
                                errorMessage = "يرجى إدخال اسم الطبق بالعربية على الأقل"
                                return@Button
                            }
                            val price = priceText.toDoubleOrNull()
                            if (price == null || price <= 0) {
                                errorMessage = "يرجى إدخال سعر بيع صحيح بالدينار الجزائري"
                                return@Button
                            }
                            val cost = costText.toDoubleOrNull() ?: 0.0

                            val productId = initialProduct?.id ?: ("prod_" + UUID.randomUUID().toString().take(8))

                            val product = ProductEntity(
                                id = productId,
                                nameAr = nameAr.trim(),
                                nameFr = nameFr.trim(),
                                nameEn = nameEn.trim(),
                                categoryId = selectedCategoryId,
                                price = price,
                                cost = cost,
                                barcode = barcode.trim(),
                                kitchenStation = kitchenStation,
                                available = isAvailable,
                                description = description.trim(),
                                iconName = selectedIconName
                            )

                            val updatedRecipes = recipeItems.map { it.copy(productId = productId) }
                            onSave(product, updatedRecipes)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_product_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEditing) "حفظ التعديلات" else "إضافة الطبق للقائمة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getFormTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GoldPrimary,
    unfocusedBorderColor = DarkBorder,
    focusedContainerColor = DarkSurfaceVariant,
    unfocusedContainerColor = DarkSurfaceVariant,
    focusedTextColor = TextWhite,
    unfocusedTextColor = TextWhite,
    focusedLabelColor = GoldLight,
    unfocusedLabelColor = TextMuted
)

fun getFoodIcon(name: String): ImageVector {
    return when (name.lowercase()) {
        "outdoor_grill", "grill" -> Icons.Default.OutdoorGrill
        "set_meal" -> Icons.Default.SetMeal
        "soup_kitchen" -> Icons.Default.SoupKitchen
        "lunch_dining" -> Icons.Default.LunchDining
        "local_pizza" -> Icons.Default.LocalPizza
        "bakery_dining" -> Icons.Default.BakeryDining
        "cake" -> Icons.Default.Cake
        "local_bar", "drinks" -> Icons.Default.LocalBar
        else -> Icons.Default.DinnerDining
    }
}
