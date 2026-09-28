package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.entity.ExpenseEntity
import com.example.ui.theme.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpensesScreen(
    expenses: List<ExpenseEntity>,
    onAddExpense: (category: String, amount: Double, desc: String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("مواد غذائية") }
    var amountText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }

    val currencyFormat = DecimalFormat("#,##0")
    val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.US)
    val totalExpenses = expenses.sumOf { it.amount }

    val categories = listOf(
        "مواد غذائية وخضار",
        "كهرباء وغاز وماء",
        "صيانة ومعدات",
        "رواتب ويوميات عمال",
        "نقل وتوصيل",
        "أخرى"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(14.dp)
    ) {
        // Header summary & Add button
        Surface(
            color = DarkSurface,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("إجمالي المصاريف المسجلة", fontSize = 12.sp, color = TextMuted)
                    Text("${currencyFormat.format(totalExpenses)} دج", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = StatusAmber)
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تسجيل مصروف جديد")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد مصاريف مسجلة حتى الآن", color = TextMuted)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses, key = { it.id }) { item ->
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(item.category, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextWhite)
                                Text("${item.description} • بواسطة: ${item.userName}", fontSize = 11.sp, color = TextMuted)
                                Text(dateFormat.format(Date(item.timestamp)), fontSize = 10.sp, color = TextMuted.copy(alpha = 0.7f))
                            }

                            Text(
                                text = "-${currencyFormat.format(item.amount)} دج",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = StatusAmber
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("تسجيل مصروف للمطعم", color = GoldLight, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("تصنيف المصروف:", color = TextMuted, fontSize = 12.sp)
                    // Category radio chips
                    categories.forEach { cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldPrimary)
                            )
                            Text(cat, fontSize = 12.sp, color = TextWhite)
                        }
                    }

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("المبلغ (دج)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descText,
                        onValueChange = { descText = it },
                        label = { Text("البيان / الوصف") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (amount > 0) {
                            onAddExpense(selectedCategory, amount, descText.ifBlank { selectedCategory })
                            showAddDialog = false
                            amountText = ""
                            descText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = Color.Black)
                ) {
                    Text("حفظ المصروف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            },
            containerColor = DarkSurface
        )
    }
}
