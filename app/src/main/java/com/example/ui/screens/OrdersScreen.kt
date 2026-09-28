package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.OrderWithItems
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrdersScreen(
    orders: List<OrderWithItems>,
    onReprintReceipt: (OrderWithItems) -> Unit,
    onRefundOrder: (orderId: String, reason: String) -> Unit,
    onCancelOrder: (orderId: String, reason: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedOrderForDetails by remember { mutableStateOf<OrderWithItems?>(null) }
    var showRefundDialog by remember { mutableStateOf(false) }
    var refundReason by remember { mutableStateOf("") }

    val currencyFormat = DecimalFormat("#,##0")
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)

    val filteredOrders = orders.filter { orderWithItems ->
        val matchesStatus = selectedStatusFilter == "ALL" || orderWithItems.order.status == selectedStatusFilter
        val matchesSearch = searchQuery.isBlank() ||
                orderWithItems.order.orderNumber.contains(searchQuery, ignoreCase = true) ||
                (orderWithItems.order.invoiceNumber?.contains(searchQuery, ignoreCase = true) == true) ||
                (orderWithItems.order.customerName?.contains(searchQuery, ignoreCase = true) == true) ||
                (orderWithItems.order.tableNumber?.toString() == searchQuery)
        matchesStatus && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        // Search & Status filters
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث برقم الطلب، الفاتورة، الطاولة، أو اسم الزبون...", fontSize = 12.sp, color = TextMuted) },
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

        Spacer(modifier = Modifier.height(10.dp))

        // Status Tabs
        val filterTabs = listOf(
            "ALL" to "الكل (${orders.size})",
            "PAID" to "مسددة (Paid)",
            "PREPARING" to "قيد التحضير",
            "READY" to "جاهزة",
            "CANCELLED" to "ملغية"
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filterTabs) { (key, label) ->
                val isSelected = selectedStatusFilter == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedStatusFilter = key },
                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
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

        // Orders List
        if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد طلبات مطابقة للبحث", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOrders, key = { it.order.id }) { item ->
                    val order = item.order
                    val statusColor = when (order.status) {
                        "PAID" -> StatusGreen
                        "PREPARING" -> StatusAmber
                        "READY" -> StatusBlue
                        "CANCELLED" -> StatusRed
                        else -> TextMuted
                    }

                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOrderForDetails = item }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = order.orderNumber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = TextWhite
                                    )
                                    Surface(
                                        color = statusColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = order.status,
                                            color = statusColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "${if (order.orderType == "DINE_IN") "طاولة ${order.tableNumber}" else order.orderType} • ${item.items.size} أصناف • ${dateFormat.format(Date(order.createdAt))}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${currencyFormat.format(order.total)} دج",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldLight
                                )
                                Text(
                                    text = order.paymentMethod ?: "غير مدفوع",
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Order Details & Actions Dialog
    if (selectedOrderForDetails != null) {
        val details = selectedOrderForDetails!!
        val order = details.order

        AlertDialog(
            onDismissRequest = { selectedOrderForDetails = null },
            title = {
                Text(
                    text = "تفاصيل الطلب: ${order.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "فاتورة: ${order.invoiceNumber ?: "غير مصدرة"}", fontSize = 12.sp, color = TextWhite)
                    Text(text = "الكاشير: ${order.cashierName}", fontSize = 12.sp, color = TextWhite)

                    HorizontalDivider(color = DarkBorder)

                    // Items breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        details.items.forEach { itm ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "${itm.quantity}x ${itm.productName}", fontSize = 12.sp, color = TextWhite)
                                Text(text = "${currencyFormat.format(itm.unitPrice * itm.quantity)} دج", fontSize = 12.sp, color = GoldLight)
                            }
                        }
                    }

                    HorizontalDivider(color = DarkBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "الإجمالي:", fontWeight = FontWeight.Bold, color = TextWhite)
                        Text(text = "${currencyFormat.format(order.total)} دج", fontWeight = FontWeight.Bold, color = GoldLight)
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onReprintReceipt(details)
                                selectedOrderForDetails = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة الفاتورة", fontSize = 11.sp)
                        }

                        if (order.status == "PAID") {
                            OutlinedButton(
                                onClick = {
                                    showRefundDialog = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("استرجاع (Refund)", fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedOrderForDetails = null }) {
                    Text("إغلاق", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }

    // Refund Confirmation Dialog
    if (showRefundDialog && selectedOrderForDetails != null) {
        val details = selectedOrderForDetails!!
        AlertDialog(
            onDismissRequest = { showRefundDialog = false },
            title = { Text("استرجاع فاتورة (Refund)", color = StatusRed, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("هل أنت متأكد من استرجاع مبلغ ${details.order.total} دج؟ سيتم تسجيل العملية في سجل الرقابة (Audit Log).", fontSize = 12.sp, color = TextWhite)
                    OutlinedTextField(
                        value = refundReason,
                        onValueChange = { refundReason = it },
                        placeholder = { Text("سبب الاسترجاع (إجباري)...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRefundOrder(details.order.id, refundReason.ifBlank { "طلب الزبون" })
                        showRefundDialog = false
                        selectedOrderForDetails = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text("تأكيد الاسترجاع")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRefundDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}
