package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.i18n.AppStrings
import com.example.ui.theme.*
import java.text.DecimalFormat

@Composable
fun PaymentDialog(
    totalAmount: Double,
    onDismiss: () -> Unit,
    onConfirmPayment: (paymentMethod: String, receivedAmount: Double, changeAmount: Double) -> Unit
) {
    var selectedMethod by remember { mutableStateOf("CASH") }
    var isSplitPaymentMode by remember { mutableStateOf(false) } // Multi-payment method (Cash + Card)
    var splitCashText by remember { mutableStateOf((totalAmount / 2).toInt().toString()) }
    var splitCardText by remember { mutableStateOf((totalAmount - (totalAmount / 2).toInt()).toInt().toString()) }

    var receivedAmountText by remember { mutableStateOf(totalAmount.toInt().toString()) }
    var isSplitBillMode by remember { mutableStateOf(false) }
    var splitCount by remember { mutableIntStateOf(2) }
    var isSubmitting by remember { mutableStateOf(false) }

    val currencyFormat = DecimalFormat("#,##0")
    val receivedAmount = receivedAmountText.toDoubleOrNull() ?: 0.0
    val changeAmount = (receivedAmount - totalAmount).coerceAtLeast(0.0)

    val splitCash = splitCashText.toDoubleOrNull() ?: 0.0
    val splitCard = splitCardText.toDoubleOrNull() ?: 0.0
    val totalSplit = splitCash + splitCard
    val isSplitValid = kotlin.math.abs(totalSplit - totalAmount) < 0.01

    val methods = listOf(
        "CASH" to ("نقداً (Cash)" to Icons.Default.Payments),
        "CARD" to ("بطاقة بنكية" to Icons.Default.CreditCard),
        "BARIDIMOB" to ("بريدي موب" to Icons.Default.AccountBalance),
        "CCP" to ("حساب بريدي" to Icons.Default.ReceiptLong),
        "OTHER" to ("أخرى" to Icons.Default.MoreHoriz)
    )

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إتمام الدفع وسداد الفاتورة",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = GoldLight
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = {
                        isSplitPaymentMode = !isSplitPaymentMode
                        if (isSplitPaymentMode) isSplitBillMode = false
                    }) {
                        Text(
                            text = if (isSplitPaymentMode) "دفع مفرد" else "دفع متعدد (Split)",
                            color = if (isSplitPaymentMode) StatusBlue else GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    TextButton(onClick = {
                        isSplitBillMode = !isSplitBillMode
                        if (isSplitBillMode) isSplitPaymentMode = false
                    }) {
                        Text(
                            text = if (isSplitBillMode) "كامل" else "تقسيم الحساب",
                            color = GoldPrimary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total to pay banner
                Surface(
                    color = GoldContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "المبلغ الإجمالي المستحق",
                            fontSize = 12.sp,
                            color = GoldLight
                        )
                        Text(
                            text = "${currencyFormat.format(totalAmount)} دج",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldLight
                        )
                    }
                }

                // If Multi-method Split Mode is enabled (e.g. Cash + Card)
                if (isSplitPaymentMode) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "دفع مجزأ بأكثر من وسيلة (Multi-Payment):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusBlue
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = splitCashText,
                                    onValueChange = { splitCashText = it },
                                    label = { Text("نقداً (Cash)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = splitCardText,
                                    onValueChange = { splitCardText = it },
                                    label = { Text("بطاقة / بريدي موب") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المجموع المحصل: ${currencyFormat.format(totalSplit)} دج",
                                    fontSize = 12.sp,
                                    color = if (isSplitValid) StatusGreen else StatusRed,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isSplitValid) {
                                    val diff = totalAmount - totalSplit
                                    Text(
                                        text = if (diff > 0) "المتبقي: ${currencyFormat.format(diff)} دج" else "الزيادة: ${currencyFormat.format(-diff)} دج",
                                        fontSize = 11.sp,
                                        color = StatusRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = "مكتمل تماماً ✓",
                                        fontSize = 12.sp,
                                        color = StatusGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // If Split Bill is enabled
                if (isSplitBillMode) {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "تقسيم الفاتورة بالتساوي:",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (2..5).forEach { count ->
                                    val isSelected = splitCount == count
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) GoldPrimary else DarkSurface,
                                        modifier = Modifier.clickable { splitCount = count }
                                    ) {
                                        Text(
                                            text = "$count أفراد",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else TextWhite,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            val perPerson = totalAmount / splitCount
                            Text(
                                text = "حصة كل شخص: ${currencyFormat.format(perPerson)} دج",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusGreen
                            )
                        }
                    }
                }

                // Payment Method Selector (when not multi-method split)
                if (!isSplitPaymentMode) {
                    Text(
                        text = "اختر طريقة الدفع:",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        methods.forEach { (key, pair) ->
                            val (label, icon) = pair
                            val isSelected = selectedMethod == key
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) GoldPrimary else DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) GoldLight else DarkBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedMethod = key }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.Black else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = label.split(" ").first(),
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextWhite,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Cash Calculations (Amount Received & Change)
                    if (selectedMethod == "CASH") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = receivedAmountText,
                                onValueChange = { receivedAmountText = it },
                                label = { Text("المبلغ المستلم من الزبون (دج)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Quick cash denominations chips:
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val presets = listOf(
                                    totalAmount.toInt(),
                                    2000,
                                    3000,
                                    5000,
                                    10000
                                ).distinct().filter { it >= totalAmount.toInt() }.take(4)

                                presets.forEach { preset ->
                                    Surface(
                                        color = DarkSurfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                        modifier = Modifier.clickable {
                                            receivedAmountText = preset.toString()
                                        }
                                    ) {
                                        Text(
                                            text = "${currencyFormat.format(preset)} دج",
                                            fontSize = 11.sp,
                                            color = TextWhite,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Change Due Box
                            Surface(
                                color = if (changeAmount > 0) StatusGreenBg else DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (changeAmount > 0) StatusGreen else DarkBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "الباقي للعميل (الصرف):",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (changeAmount > 0) StatusGreen else TextWhite
                                    )
                                    Text(
                                        text = "${currencyFormat.format(changeAmount)} دج",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (changeAmount > 0) StatusGreen else TextWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val canSubmit = if (isSplitPaymentMode) {
                isSplitValid && !isSubmitting
            } else {
                (selectedMethod != "CASH" || receivedAmount >= totalAmount) && !isSubmitting
            }

            Button(
                onClick = {
                    if (isSubmitting) return@Button
                    isSubmitting = true
                    val finalMethod = if (isSplitPaymentMode) {
                        "SPLIT: نقداً (${currencyFormat.format(splitCash)} دج) + بطاقة (${currencyFormat.format(splitCard)} دج)"
                    } else selectedMethod

                    val finalReceived = if (isSplitPaymentMode) totalAmount else (if (selectedMethod == "CASH") receivedAmount else totalAmount)
                    val finalChange = if (isSplitPaymentMode) 0.0 else (if (selectedMethod == "CASH") changeAmount else 0.0)

                    onConfirmPayment(finalMethod, finalReceived, finalChange)
                },
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    text = if (isSubmitting) "جارٍ تسجيل العملية..." else "تأكيد الدفع وطباعة الفاتورة",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("إلغاء", color = TextMuted)
            }
        },
        containerColor = DarkSurface
    )
}
