package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.CashSessionEntity
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CashRegisterScreen(
    currentSession: CashSessionEntity?,
    onOpenShift: (openingCash: Double, notes: String) -> Unit
) {
    var showOpenShiftDialog by remember { mutableStateOf(false) }
    var openingCashText by remember { mutableStateOf("20000") }
    var shiftNotesText by remember { mutableStateOf("افتتاح الصندوق ليوم العمل") }

    val currencyFormat = DecimalFormat("#,##0")
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        if (currentSession != null && currentSession.status == "OPEN") {
            // Active Shift Card
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الوردية النشطة حالياً (Active Shift)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GoldLight)
                            Text("الكاشير: ${currentSession.cashierName}", fontSize = 12.sp, color = TextMuted)
                        }

                        Surface(color = StatusGreenBg, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                "الصندوق مفتوح",
                                color = StatusGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    CashMetricRow("وقت افتتاح الصندوق:", dateFormat.format(Date(currentSession.openedAt)))
                    CashMetricRow("مبلغ البداية (Opening Cash):", "${currencyFormat.format(currentSession.openingCash)} دج")
                    CashMetricRow("المبيعات النقدية المقبوضة (Cash Sales):", "${currencyFormat.format(currentSession.cashSales)} دج")
                    CashMetricRow("المصاريف النقدية المدفوعة من الصندوق:", "-${currencyFormat.format(currentSession.cashExpenses)} دج")

                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))

                    val expectedInDrawer = currentSession.openingCash + currentSession.cashSales - currentSession.cashExpenses
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("الرصيد النقدي المتوقع في الدرج:", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 14.sp)
                        Text("${currencyFormat.format(expectedInDrawer)} دج", fontWeight = FontWeight.ExtraBold, color = GoldLight, fontSize = 18.sp)
                    }
                }
            }
        } else {
            // No shift currently open
            Surface(
                color = DarkSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = StatusAmber, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("الصندوق مغلق حالياً", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("يرجى إدخال الرصيد الافتتاحي للدرج لبدء استقبال الطلبات", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showOpenShiftDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                    ) {
                        Text("افتتاح وردية جديدة")
                    }
                }
            }
        }
    }

    if (showOpenShiftDialog) {
        AlertDialog(
            onDismissRequest = { showOpenShiftDialog = false },
            title = { Text("افتتاح الصندوق النقدي", color = GoldLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = openingCashText,
                        onValueChange = { openingCashText = it },
                        label = { Text("الرصيد الافتتاحي (دج)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = shiftNotesText,
                        onValueChange = { shiftNotesText = it },
                        label = { Text("ملاحظات الوردية") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = openingCashText.toDoubleOrNull() ?: 20000.0
                        onOpenShift(amount, shiftNotesText)
                        showOpenShiftDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("تأكيد الافتتاح")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOpenShiftDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun CashMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
    }
}
