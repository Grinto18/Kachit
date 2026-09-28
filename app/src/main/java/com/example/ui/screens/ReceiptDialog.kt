package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.dao.OrderWithItems
import com.example.i18n.AppStrings
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceiptDialog(
    orderWithItems: OrderWithItems,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var printMode by remember { mutableStateOf("80MM") } // 80MM or A4
    val order = orderWithItems.order
    val items = orderWithItems.items
    val currencyFormat = DecimalFormat("#,##0")
    val dateFormat = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.US)
    val formattedDate = dateFormat.format(Date(order.paidAt ?: order.createdAt))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "معاينة الفاتورة (Receipt Preview)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GoldLight
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = printMode == "58MM",
                        onClick = { printMode = "58MM" },
                        label = { Text("58mm حراري", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = printMode == "80MM",
                        onClick = { printMode = "80MM" },
                        label = { Text("80mm حراري", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = printMode == "A4",
                        onClick = { printMode = "A4" },
                        label = { Text("A4 تقرير", fontSize = 10.sp) }
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Receipt Paper Container (Thermal Paper Style)
                Surface(
                    color = Color(0xFFFAFAFA),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0D0D0)),
                    modifier = Modifier
                        .fillMaxWidth(if (printMode == "58MM") 0.85f else 1f)
                        .align(Alignment.CenterHorizontally)
                        .padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header
                        Text(
                            text = "القصر الذهبي",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "عند الجيجلي • حسين داي",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "شارع بلهوشات، حسين داي، الجزائر\nبجانب فندق Oasis",
                            fontSize = 10.sp,
                            color = Color(0xFF555555),
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "الهاتف: 0791755614",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )

                        ReceiptDottedDivider()

                        // Invoice & Order Meta
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "فاتورة: ${order.invoiceNumber ?: order.orderNumber}", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            Text(text = formattedDate, fontSize = 10.sp, color = Color.Black)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val orderTypeLabel = when (order.orderType) {
                                "DINE_IN" -> "محلي (طاولة ${order.tableNumber ?: 1})"
                                "TAKEAWAY" -> "سفري"
                                else -> "توصيل"
                            }
                            Text(text = "النوع: $orderTypeLabel", fontSize = 10.sp, color = Color.Black)
                            Text(text = "الكاشير: ${order.cashierName.take(15)}", fontSize = 10.sp, color = Color.Black)
                        }

                        ReceiptDottedDivider()

                        // Table Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "الصنف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.weight(0.5f))
                            Text(text = "الكمية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center, modifier = Modifier.weight(0.2f))
                            Text(text = "السعر", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End, modifier = Modifier.weight(0.3f))
                        }

                        ReceiptDottedDivider()

                        // Itemized entries
                        items.forEach { item ->
                            val itemTotal = item.unitPrice * item.quantity
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(0.5f)) {
                                    Text(text = item.productName, fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                                    if (item.note.isNotBlank()) {
                                        Text(text = "(${item.note})", fontSize = 9.sp, color = Color.DarkGray)
                                    }
                                }
                                Text(
                                    text = "${item.quantity}",
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(0.2f)
                                )
                                Text(
                                    text = "${currencyFormat.format(itemTotal)}",
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.End,
                                    modifier = Modifier.weight(0.3f)
                                )
                            }
                        }

                        ReceiptDottedDivider()

                        // Totals & Calculations
                        ReceiptRowDark("المجموع الفرعي:", "${currencyFormat.format(order.subtotal)} دج")
                        if (order.discount > 0) {
                            ReceiptRowDark("الخصم:", "-${currencyFormat.format(order.discount)} دج")
                        }
                        if (order.deliveryFee > 0) {
                            ReceiptRowDark("التوصيل:", "+${currencyFormat.format(order.deliveryFee)} دج")
                        }

                        ReceiptDottedDivider()

                        // Final Total
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "الإجمالي الصافي:", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            Text(text = "${currencyFormat.format(order.total)} دج", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }

                        // Payment Details
                        val methodArabic = when (order.paymentMethod) {
                            "CASH" -> "نقداً (Cash)"
                            "CARD" -> "بطاقة بنكية"
                            "BARIDIMOB" -> "بريدي موب"
                            "CCP" -> "حساب بريدي"
                            else -> "أخرى"
                        }
                        ReceiptRowDark("طريقة السداد:", methodArabic)

                        if (order.paymentMethod == "CASH" && order.amountReceived > 0) {
                            ReceiptRowDark("المبلغ المدفوع:", "${currencyFormat.format(order.amountReceived)} دج")
                            ReceiptRowDark("الباقي (الصرف):", "${currencyFormat.format(order.changeAmount)} دج")
                        }

                        ReceiptDottedDivider()

                        // Footer
                        Text(
                            text = "شكراً لزيارتكم • صحة وهنا",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "www.palaisdore-alger.dz",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Share Receipt intent
                OutlinedButton(
                    onClick = {
                        shareReceiptText(context, orderWithItems)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مشاركة")
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طباعة الفاتورة")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}

@Composable
private fun ReceiptRowDark(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF444444))
        Text(text = value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
    }
}

@Composable
private fun ReceiptDottedDivider() {
    Text(
        text = "- - - - - - - - - - - - - - - - - - - - - - - - - - -",
        fontSize = 10.sp,
        color = Color(0xFFAAAAAA),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    )
}

private fun shareReceiptText(context: Context, orderWithItems: OrderWithItems) {
    val order = orderWithItems.order
    val items = orderWithItems.items
    val currencyFormat = DecimalFormat("#,##0")

    val sb = StringBuilder()
    sb.appendLine("═══════════════════════════════")
    sb.appendLine("       القصر الذهبي")
    sb.appendLine("  عند الجيجلي • حسين داي")
    sb.appendLine("  شارع بلهوشات، الجزائر")
    sb.appendLine("  الهاتف: 0791755614")
    sb.appendLine("═══════════════════════════════")
    sb.appendLine("فاتورة: ${order.invoiceNumber ?: order.orderNumber}")
    sb.appendLine("التاريخ: ${SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US).format(Date())}")
    sb.appendLine("الكاشير: ${order.cashierName}")
    sb.appendLine("───────────────────────────────")
    items.forEach { item ->
        sb.appendLine("${item.quantity}x ${item.productName} = ${currencyFormat.format(item.unitPrice * item.quantity)} دج")
    }
    sb.appendLine("───────────────────────────────")
    sb.appendLine("المجموع: ${currencyFormat.format(order.total)} دج")
    sb.appendLine("طريقة الدفع: ${order.paymentMethod ?: "CASH"}")
    sb.appendLine("═══════════════════════════════")
    sb.appendLine("شكراً لزيارتكم • صحة وهنا")

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "فاتورة مطعم القصر الذهبي")
        putExtra(Intent.EXTRA_TEXT, sb.toString())
    }
    context.startActivity(Intent.createChooser(intent, "مشاركة الفاتورة"))
}
