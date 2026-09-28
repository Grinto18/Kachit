package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.InventoryItemEntity
import com.example.ui.theme.*
import java.text.DecimalFormat

@Composable
fun InventoryScreen(
    inventoryItems: List<InventoryItemEntity>,
    onRestock: (itemId: String, qty: Double, cost: Double) -> Unit,
    onRecordWaste: (itemId: String, qty: Double, reason: String) -> Unit = { _, _, _ -> },
    onRecordPhysicalStockCount: (itemId: String, actualStock: Double, notes: String) -> Unit = { _, _, _ -> }
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItemForRestock by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var restockQtyText by remember { mutableStateOf("") }
    var restockCostText by remember { mutableStateOf("") }

    var selectedItemForWaste by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var wasteQtyText by remember { mutableStateOf("") }
    var wasteReasonText by remember { mutableStateOf("تالف أثناء التخزين") }

    var selectedItemForCount by remember { mutableStateOf<InventoryItemEntity?>(null) }
    var actualCountText by remember { mutableStateOf("") }
    var countNotesText by remember { mutableStateOf("جرد دوري") }

    val currencyFormat = DecimalFormat("#,##0")
    val lowStockCount = inventoryItems.count { it.currentStock <= it.minStock }

    val filteredItems = inventoryItems.filter {
        searchQuery.isBlank() ||
                it.nameAr.contains(searchQuery, ignoreCase = true) ||
                it.nameFr.contains(searchQuery, ignoreCase = true) ||
                it.sku.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        // Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("إجمالي المواد بالمخزون", fontSize = 11.sp, color = TextMuted)
                    Text("${inventoryItems.size} مادة أولية", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                }
            }

            Surface(
                color = if (lowStockCount > 0) StatusRedBg else DarkSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("تنبيهات نقص المخزون", fontSize = 11.sp, color = if (lowStockCount > 0) StatusRed else TextMuted)
                    Text("$lowStockCount مواد أوشكت على النفاد", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (lowStockCount > 0) StatusRed else TextWhite)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن مادة أولية أو مكون بالاسم أو الرمز...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Inventory Items List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                val isLow = item.currentStock <= item.minStock
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLow) StatusRed else DarkBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.nameAr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextWhite
                                )
                                if (isLow) {
                                    Surface(
                                        color = StatusRedBg,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "مخزون منخفض!",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusRed,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${item.nameFr} • SKU: ${item.sku} • التكلفة: ${currencyFormat.format(item.unitCost)} دج / ${item.unit}",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${item.currentStock} ${item.unit}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isLow) StatusRed else GoldLight
                                )
                                Text(
                                    text = "الحد الأدنى: ${item.minStock} ${item.unit}",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = {
                                        selectedItemForRestock = item
                                        restockQtyText = ""
                                        restockCostText = item.unitCost.toString()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = GoldContainer,
                                        contentColor = GoldLight
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("توريد +", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        selectedItemForWaste = item
                                        wasteQtyText = ""
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusRed.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("هالك -", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        selectedItemForCount = item
                                        actualCountText = item.currentStock.toString()
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusBlue),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusBlue.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text("جرد", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Restock Dialog
    if (selectedItemForRestock != null) {
        val item = selectedItemForRestock!!
        AlertDialog(
            onDismissRequest = { selectedItemForRestock = null },
            title = {
                Text(
                    text = "إضافة كمية جديدة (توريد): ${item.nameAr}",
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("المخزون الحالي: ${item.currentStock} ${item.unit}", color = TextMuted, fontSize = 12.sp)
                    OutlinedTextField(
                        value = restockQtyText,
                        onValueChange = { restockQtyText = it },
                        label = { Text("الكمية المضافة (${item.unit})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = restockCostText,
                        onValueChange = { restockCostText = it },
                        label = { Text("سعر التكلفة للوحدة (دج)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = restockQtyText.toDoubleOrNull() ?: 0.0
                        val cost = restockCostText.toDoubleOrNull() ?: item.unitCost
                        if (qty > 0) {
                            onRestock(item.id, qty, cost)
                            selectedItemForRestock = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("تأكيد التوريد")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForRestock = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Waste / Damaged Stock Dialog
    if (selectedItemForWaste != null) {
        val item = selectedItemForWaste!!
        AlertDialog(
            onDismissRequest = { selectedItemForWaste = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تسجيل هالك / تالف: ${item.nameAr}",
                        fontWeight = FontWeight.Bold,
                        color = StatusRed
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("المخزون المتوفر: ${item.currentStock} ${item.unit}", color = TextMuted, fontSize = 12.sp)
                    OutlinedTextField(
                        value = wasteQtyText,
                        onValueChange = { wasteQtyText = it },
                        label = { Text("الكمية التالفة للخصم (${item.unit}) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = wasteReasonText,
                        onValueChange = { wasteReasonText = it },
                        label = { Text("سبب الإتلاف / الهدر *") },
                        placeholder = { Text("مثال: منتهي الصلاحية، تلف أثناء التحضير...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = wasteQtyText.toDoubleOrNull() ?: 0.0
                        if (qty > 0) {
                            onRecordWaste(item.id, qty, wasteReasonText)
                            selectedItemForWaste = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed, contentColor = TextWhite)
                ) {
                    Text("خصم وتوثيق الهالك")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForWaste = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Physical Stock Count Dialog
    if (selectedItemForCount != null) {
        val item = selectedItemForCount!!
        val actualStock = actualCountText.toDoubleOrNull() ?: item.currentStock
        val difference = actualStock - item.currentStock

        AlertDialog(
            onDismissRequest = { selectedItemForCount = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FactCheck, contentDescription = null, tint = StatusBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "جرد المخزون الفعلي: ${item.nameAr}",
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("الرصيد الدفتري المسجل بالنظام: ${item.currentStock} ${item.unit}", fontSize = 12.sp, color = TextWhite)
                            Spacer(modifier = Modifier.height(4.dp))
                            val diffColor = if (difference == 0.0) StatusGreen else if (difference < 0) StatusRed else StatusAmber
                            val diffSign = if (difference > 0) "+" else ""
                            Text(
                                text = "الفارق الناتج: $diffSign${DecimalFormat("#,##0.##").format(difference)} ${item.unit}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = diffColor
                            )
                        }
                    }

                    OutlinedTextField(
                        value = actualCountText,
                        onValueChange = { actualCountText = it },
                        label = { Text("الكمية الفعلية المحصية بالمخزن (${item.unit}) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = countNotesText,
                        onValueChange = { countNotesText = it },
                        label = { Text("ملاحظات الجرد الدوري") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val stock = actualCountText.toDoubleOrNull()
                        if (stock != null && stock >= 0) {
                            onRecordPhysicalStockCount(item.id, stock, countNotesText)
                            selectedItemForCount = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("اعتماد وتصحيح الرصيد")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedItemForCount = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}
