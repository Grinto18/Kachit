package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.OrderWithItems
import com.example.data.entity.TableEntity
import com.example.ui.theme.*

@Composable
fun TablesScreen(
    tables: List<TableEntity>,
    activeOrders: List<OrderWithItems>,
    onSelectTableForOrder: (TableEntity) -> Unit,
    onTransferTable: (fromTableId: String, toTableId: String) -> Unit
) {
    var selectedTableForAction by remember { mutableStateOf<TableEntity?>(null) }
    var showTransferDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        // Status Legend Header
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusLegendItem(color = StatusGreen, label = "متاحة (Available)")
                StatusLegendItem(color = GoldPrimary, label = "مشغولة (Occupied)")
                StatusLegendItem(color = StatusAmber, label = "انتظار الدفع")
                StatusLegendItem(color = StatusPurple, label = "محجوزة (Reserved)")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tables Visual Grid
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tables, key = { it.id }) { table ->
                val activeOrder = activeOrders.find { it.order.tableId == table.id }
                TableCard(
                    table = table,
                    activeOrder = activeOrder,
                    onClick = {
                        selectedTableForAction = table
                    }
                )
            }
        }
    }

    // Table Actions Dialog
    if (selectedTableForAction != null) {
        val table = selectedTableForAction!!
        val activeOrder = activeOrders.find { it.order.tableId == table.id }

        AlertDialog(
            onDismissRequest = { selectedTableForAction = null },
            title = {
                Text(
                    text = "طاولة رقم ${table.tableNumber} - ${table.notes}",
                    fontWeight = FontWeight.Bold,
                    color = GoldLight,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "السعة: ${table.capacity} كراسي • الحالة: ${table.status}",
                        fontSize = 13.sp,
                        color = TextWhite
                    )

                    if (activeOrder != null) {
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "الطلب النشط: ${activeOrder.order.orderNumber}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                                Text(
                                    text = "إجمالي الحساب: ${activeOrder.order.total} دج",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "عدد الأصناف: ${activeOrder.items.sumOf { it.quantity }}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    // Action buttons inside dialog
                    Button(
                        onClick = {
                            onSelectTableForOrder(table)
                            selectedTableForAction = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (activeOrder != null) "فتح الطلب في الكاشير / إضافة عناصر" else "إنشاء طلب جديد للطاولة")
                    }

                    if (activeOrder != null) {
                        OutlinedButton(
                            onClick = {
                                showTransferDialog = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("نقل الطلب إلى طاولة أخرى (Transfer)")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedTableForAction = null }) {
                    Text("إغلاق", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Transfer Table Dialog
    if (showTransferDialog && selectedTableForAction != null) {
        val fromTable = selectedTableForAction!!
        val availableTables = tables.filter { it.status == "AVAILABLE" && it.id != fromTable.id }

        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = {
                Text(
                    text = "نقل الطلب من طاولة ${fromTable.tableNumber}",
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("اختر الطاولة الجديدة الشاغرة:", color = TextMuted, fontSize = 12.sp)
                    if (availableTables.isEmpty()) {
                        Text("لا توجد طاولات شاغرة حالياً للنقل إليها.", color = StatusRed)
                    } else {
                        availableTables.forEach { toTable ->
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onTransferTable(fromTable.id, toTable.id)
                                        showTransferDialog = false
                                        selectedTableForAction = null
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "طاولة ${toTable.tableNumber} (${toTable.notes})",
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "سعة ${toTable.capacity} مقاعد",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun TableCard(
    table: TableEntity,
    activeOrder: OrderWithItems?,
    onClick: () -> Unit
) {
    val statusColor = when (table.status) {
        "OCCUPIED" -> GoldPrimary
        "WAITING_PAYMENT" -> StatusAmber
        "RESERVED" -> StatusPurple
        else -> StatusGreen
    }

    val statusBg = when (table.status) {
        "OCCUPIED" -> GoldContainer
        "WAITING_PAYMENT" -> StatusAmberBg
        "RESERVED" -> StatusPurple.copy(alpha = 0.2f)
        else -> StatusGreenBg
    }

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, statusColor),
        modifier = Modifier
            .height(130.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Table Number Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (table.status == "AVAILABLE") "متاحة" else if (table.status == "OCCUPIED") "مشغولة" else table.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.People, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Text(text = "${table.capacity}", fontSize = 11.sp, color = TextMuted)
                }
            }

            // Big Table Number
            Text(
                text = "${table.tableNumber}",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextWhite,
                textAlign = TextAlign.Center
            )

            // Bottom info (Order Total or Notes)
            if (activeOrder != null) {
                Text(
                    text = "${activeOrder.order.total.toInt()} دج",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            } else {
                Text(
                    text = table.notes,
                    fontSize = 10.sp,
                    color = TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun StatusLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(3.dp))
        )
        Text(text = label, fontSize = 11.sp, color = TextWhite)
    }
}
