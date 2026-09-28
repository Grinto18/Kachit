package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserEntity
import com.example.i18n.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

data class NavItem(
    val screen: AppScreen,
    val titleKey: String,
    val icon: ImageVector,
    val allowedRoles: List<String>
)

val allNavItems = listOf(
    NavItem(AppScreen.POS, "pos", Icons.Default.PointOfSale, listOf("ADMIN", "MANAGER", "CASHIER", "WAITER")),
    NavItem(AppScreen.TABLES, "tables", Icons.Default.TableRestaurant, listOf("ADMIN", "MANAGER", "CASHIER", "WAITER")),
    NavItem(AppScreen.KITCHEN, "kitchen", Icons.Default.SoupKitchen, listOf("ADMIN", "MANAGER", "KITCHEN")),
    NavItem(AppScreen.ORDERS, "orders", Icons.Default.ReceiptLong, listOf("ADMIN", "MANAGER", "CASHIER")),
    NavItem(AppScreen.MENU_MANAGEMENT, "menu_management", Icons.Default.RestaurantMenu, listOf("ADMIN", "MANAGER")),
    NavItem(AppScreen.INVENTORY, "inventory", Icons.Default.Inventory2, listOf("ADMIN", "MANAGER")),
    NavItem(AppScreen.REPORTS, "reports", Icons.Default.Assessment, listOf("ADMIN", "MANAGER")),
    NavItem(AppScreen.CASH_REGISTER, "cash_register", Icons.Default.AccountBalanceWallet, listOf("ADMIN", "MANAGER", "CASHIER")),
    NavItem(AppScreen.EXPENSES, "expenses", Icons.Default.PriceCheck, listOf("ADMIN", "MANAGER")),
    NavItem(AppScreen.SETTINGS, "settings", Icons.Default.Settings, listOf("ADMIN"))
)

@Composable
fun AppSideNavRail(
    currentScreen: AppScreen,
    currentUser: UserEntity?,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val userRole = currentUser?.role ?: "CASHIER"
    val visibleItems = allNavItems.filter { it.allowedRoles.contains(userRole) }

    Surface(
        color = DarkSurface,
        modifier = modifier.width(90.dp).fillMaxHeight()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            visibleItems.forEach { item ->
                val isSelected = currentScreen == item.screen
                val bgColor = if (isSelected) GoldPrimary else DarkSurfaceVariant
                val contentColor = if (isSelected) DarkBackground else TextMuted

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .clickable { onNavigate(item.screen) }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = AppStrings.get(item.titleKey),
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.get(item.titleKey).take(12),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = contentColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppBottomNav(
    currentScreen: AppScreen,
    currentUser: UserEntity?,
    onNavigate: (AppScreen) -> Unit
) {
    val userRole = currentUser?.role ?: "CASHIER"
    val visibleItems = allNavItems.filter { it.allowedRoles.contains(userRole) }.take(5)

    NavigationBar(
        containerColor = DarkSurface,
        contentColor = TextWhite
    ) {
        visibleItems.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.screen) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = AppStrings.get(item.titleKey),
                        tint = if (isSelected) GoldPrimary else TextMuted
                    )
                },
                label = {
                    Text(
                        text = AppStrings.get(item.titleKey),
                        color = if (isSelected) GoldPrimary else TextMuted,
                        fontSize = 10.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GoldContainer
                )
            )
        }
    }
}
