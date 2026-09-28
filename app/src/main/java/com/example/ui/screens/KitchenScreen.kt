package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.OrderWithItems
import com.example.data.entity.OrderItemEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun KitchenScreen(
    orders: List<OrderWithItems>,
    onUpdateItemStatus: (itemId: String, status: String) -> Unit,
    onMarkOrderReady: (orderId: String) -> Unit,
    onCompleteOrder: (orderId: String) -> Unit
) {
    var selectedStation by remember { mutableStateOf("ALL") }

    val stations = listOf(
        "ALL" to "جميع الأقسام",
        "MAIN_KITCHEN" to "المطبخ الرئيسي",
        "GRILL" to "المشاوي والشواء",
        "DRINKS" to "المشروبات والعصائر",
        "DESSERT" to "الحلويات"
    )

    // Filter orders having items in selected station
    val filteredOrders = orders.filter { orderWithItems ->
        if (selectedStation == "ALL") true
        else orderWithItems.items.any { it.kitchenStation == selectedStation }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        // Station Filter Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(stations) { (key, label) ->
                    val isSelected = selectedStation == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedStation = key },
                        label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.Black,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextWhite
                        )
                    )
                }
            }

            Surface(
                color = StatusAmberBg,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "${filteredOrders.size} طلبات قيد التحضير",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusAmber,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "All Clear",
                        tint = StatusGreen,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد طلبات معلقة للمطبخ حالياً!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "جميع الطلبات السابقة تم تجهيزها وتقديمها للزبائن بنجاح.",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredOrders, key = { it.order.id }) { orderWithItems ->
                    KitchenOrderCard(
                        orderWithItems = orderWithItems,
                        stationFilter = selectedStation,
                        onUpdateItemStatus = onUpdateItemStatus,
                        onMarkOrderReady = onMarkOrderReady,
                        onCompleteOrder = onCompleteOrder
                    )
                }
            }
        }
    }
}

@Composable
fun KitchenOrderCard(
    orderWithItems: OrderWithItems,
    stationFilter: String,
    onUpdateItemStatus: (itemId: String, status: String) -> Unit,
    onMarkOrderReady: (orderId: String) -> Unit,
    onCompleteOrder: (orderId: String) -> Unit
) {
    val order = orderWithItems.order
    val displayItems = if (stationFilter == "ALL") {
        orderWithItems.items
    } else {
        orderWithItems.items.filter { it.kitchenStation == stationFilter }
    }

    val elapsedMinutes = ((System.currentTimeMillis() - order.createdAt) / 60000).coerceAtLeast(0)
    val timeColor = if (elapsedMinutes > 20) StatusRed else if (elapsedMinutes > 10) StatusAmber else StatusGreen

    val isAllReady = displayItems.all { it.status == "READY" } || order.status == "READY"

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isAllReady) StatusGreen else if (order.status == "NEW") StatusAmber else DarkBorder
        ),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = GoldContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (order.orderType == "DINE_IN") "طاولة ${order.tableNumber ?: 1}" else if (order.orderType == "DELIVERY") "توصيل" else "سفري",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldLight,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    Text(
                        text = order.orderNumber,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextWhite
                    )
                }

                // Elapsed time timer badge
                Surface(
                    color = timeColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = timeColor, modifier = Modifier.size(14.dp))
                        Text(
                            text = "منذ $elapsedMinutes دقيقة",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = timeColor
                        )
                    }
                }
            }

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = StatusAmberBg,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ملاحظة عامة: ${order.notes}",
                        fontSize = 11.sp,
                        color = StatusAmber,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 10.dp))

            // Order Items
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                displayItems.forEach { item ->
                    KitchenItemRow(
                        item = item,
                        onStatusToggle = {
                            val nextStatus = if (item.status == "READY") "PREPARING" else "READY"
                            onUpdateItemStatus(item.id, nextStatus)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isAllReady) {
                    Button(
                        onClick = { onMarkOrderReady(order.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الطلب جاهز للتقديم (Ready)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onCompleteOrder(order.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تسليم الطلب وإنهاء (Complete)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KitchenItemRow(
    item: OrderItemEntity,
    onStatusToggle: () -> Unit
) {
    val isReady = item.status == "READY"

    Surface(
        color = if (isReady) StatusGreenBg else DarkSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isReady) StatusGreen else DarkBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onStatusToggle() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quantity circle badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(GoldPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${item.quantity}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Column {
                    Text(
                        text = item.productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isReady) StatusGreen else TextWhite
                    )
                    if (item.note.isNotBlank()) {
                        Text(
                            text = "ملاحظة: ${item.note}",
                            fontSize = 11.sp,
                            color = StatusAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Surface(
                color = if (isReady) StatusGreen else DarkBorder,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = if (isReady) "جاهز ✓" else "قيد الطهي...",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isReady) Color.White else TextMuted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
