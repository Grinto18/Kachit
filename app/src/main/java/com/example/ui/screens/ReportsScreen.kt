package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.OrderWithItems
import com.example.data.entity.ExpenseEntity
import com.example.ui.theme.*
import java.text.DecimalFormat

@Composable
fun ReportsScreen(
    orders: List<OrderWithItems>,
    expenses: List<ExpenseEntity>
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf("TODAY") } // TODAY, WEEK, MONTH, ALL
    val currencyFormat = DecimalFormat("#,##0")

    // Filter by period
    val now = System.currentTimeMillis()
    val periodMillis = when (selectedPeriod) {
        "TODAY" -> 24 * 3600 * 1000L
        "WEEK" -> 7 * 24 * 3600 * 1000L
        "MONTH" -> 30 * 24 * 3600 * 1000L
        else -> Long.MAX_VALUE
    }

    val periodOrders = orders.filter { now - it.order.createdAt <= periodMillis }
    val periodExpenses = expenses.filter { now - it.timestamp <= periodMillis }

    val paidOrders = periodOrders.filter { it.order.status == "PAID" }
    val grossSales = paidOrders.sumOf { it.order.subtotal }
    val totalDiscounts = paidOrders.sumOf { it.order.discount }
    val refundedAmount = periodOrders.filter { it.order.status == "REFUNDED" }.sumOf { it.order.total }
    val netSales = (grossSales - totalDiscounts - refundedAmount).coerceAtLeast(0.0)

    val totalExpenses = periodExpenses.sumOf { it.amount }
    val estimatedFoodCost = netSales * 0.38 // ~38% standard restaurant food cost
    val estimatedProfit = netSales - estimatedFoodCost - totalExpenses

    // Top Products
    val productSalesMap = mutableMapOf<String, Int>()
    paidOrders.forEach { ord ->
        ord.items.forEach { itm ->
            productSalesMap[itm.productName] = (productSalesMap[itm.productName] ?: 0) + itm.quantity
        }
    }
    val topProducts = productSalesMap.toList().sortedByDescending { it.second }.take(5)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        // Period Filter Tabs & Export button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val periods = listOf(
                    "TODAY" to "اليوم",
                    "WEEK" to "هذا الأسبوع",
                    "MONTH" to "هذا الشهر",
                    "ALL" to "الكل"
                )
                periods.forEach { (key, label) ->
                    val isSelected = selectedPeriod == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedPeriod = key },
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

            OutlinedButton(
                onClick = {
                    shareReportSummary(context, selectedPeriod, netSales, totalExpenses, estimatedProfit, paidOrders.size)
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تصدير التقرير", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Financial Summary Card
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ملخص العمليات المالية والمبيعات",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        FinancialMetricRow("إجمالي المبيعات (Gross Sales):", "${currencyFormat.format(grossSales)} دج", TextWhite)
                        FinancialMetricRow("إجمالي الخصومات (Discounts):", "-${currencyFormat.format(totalDiscounts)} دج", StatusGreen)
                        FinancialMetricRow("الطلبات المسترجعة (Refunds):", "-${currencyFormat.format(refundedAmount)} دج", StatusRed)

                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 6.dp))

                        FinancialMetricRow("صافي المبيعات (Net Sales):", "${currencyFormat.format(netSales)} دج", GoldLight, isBold = true)
                        FinancialMetricRow("التكلفة التقديرية للمواد (Food Cost ~38%):", "-${currencyFormat.format(estimatedFoodCost)} دج", TextMuted)
                        FinancialMetricRow("إجمالي المصاريف المسجلة:", "-${currencyFormat.format(totalExpenses)} دج", StatusAmber)

                        HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الربح الصافي التقديري:", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                            Text(
                                text = "${currencyFormat.format(estimatedProfit)} دج",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (estimatedProfit >= 0) StatusGreen else StatusRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "* تنبيه: الأرباح المعروضة تقديرية وفقاً للمبيعات والتكاليف المسجلة في النظام ولا تشكل إقراراً محاسبياً قانونياً رسمياً.",
                            fontSize = 10.sp,
                            color = TextMuted,
                            lineHeight = 13.sp
                        )
                    }
                }
            }

            // Key Performance Indicators (KPIs)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "عدد الطلبات المكتملة",
                        value = "${paidOrders.size} طلب",
                        icon = Icons.Default.ReceiptLong,
                        color = GoldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    val avgTicket = if (paidOrders.isNotEmpty()) netSales / paidOrders.size else 0.0
                    KpiCard(
                        title = "متوسط قيمة الطلب",
                        value = "${currencyFormat.format(avgTicket)} دج",
                        icon = Icons.Default.TrendingUp,
                        color = StatusBlue,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Top Sold Products
            item {
                Surface(
                    color = DarkSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "الأطباق الأكثر طلباً (Top Products)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (topProducts.isEmpty()) {
                            Text("لا توجد مبيعات مسجلة في هذه الفترة بعد", color = TextMuted, fontSize = 12.sp)
                        } else {
                            topProducts.forEachIndexed { index, (name, count) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "${index + 1}. $name", fontSize = 12.sp, color = TextWhite)
                                    Surface(color = GoldContainer, shape = RoundedCornerShape(4.dp)) {
                                        Text(
                                            text = "$count وجبة",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GoldLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
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
}

@Composable
fun FinancialMetricRow(label: String, value: String, color: Color, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = if (isBold) TextWhite else TextMuted, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal)
        Text(text = value, fontSize = 12.sp, fontWeight = if (isBold) FontWeight.ExtraBold else FontWeight.SemiBold, color = color)
    }
}

@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(text = title, fontSize = 10.sp, color = TextMuted)
                Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            }
        }
    }
}

private fun shareReportSummary(
    context: Context,
    period: String,
    netSales: Double,
    expenses: Double,
    profit: Double,
    orderCount: Int
) {
    val currencyFormat = DecimalFormat("#,##0")
    val text = """
        تقرير أداء مطعم القصر الذهبي - حسين داي
        الفترة: $period
        ───────────────────────────────
        عدد الطلبات: $orderCount
        صافي المبيعات: ${currencyFormat.format(netSales)} دج
        المصاريف: ${currencyFormat.format(expenses)} دج
        الربح التقديري: ${currencyFormat.format(profit)} دج
        ───────────────────────────────
        تم التصدير من نظام كاشير القصر الذهبي POS
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "تقرير مبيعات القصر الذهبي")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "تصدير التقرير"))
}
