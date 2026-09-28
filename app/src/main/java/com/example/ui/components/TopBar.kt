package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserEntity
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen

@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    currentUser: UserEntity?,
    currentLanguage: AppLanguage,
    lowStockCount: Int,
    onNavigate: (AppScreen) -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    onLogout: () -> Unit
) {
    var showLangMenu by remember { mutableStateOf(false) }

    Surface(
        color = DarkSurface,
        border = null,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Location
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(GoldContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = "Logo",
                        tint = GoldPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = AppStrings.get("app_title"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = GoldLight
                    )
                    Text(
                        text = AppStrings.get("app_subtitle"),
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Center: Screen Title
            val screenTitleKey = when (currentScreen) {
                AppScreen.POS -> "pos"
                AppScreen.TABLES -> "tables"
                AppScreen.KITCHEN -> "kitchen"
                AppScreen.ORDERS -> "orders"
                AppScreen.MENU_MANAGEMENT -> "menu_management"
                AppScreen.INVENTORY -> "inventory"
                AppScreen.REPORTS -> "reports"
                AppScreen.CASH_REGISTER -> "cash_register"
                AppScreen.EXPENSES -> "expenses"
                AppScreen.SETTINGS -> "settings"
                AppScreen.LOGIN -> "login_title"
            }
            Surface(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = AppStrings.get(screenTitleKey),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhite,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Right side: Alerts, Language Switcher, User Role, Logout
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Low Stock Badge if any
                if (lowStockCount > 0) {
                    Surface(
                        color = StatusRedBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable { onNavigate(AppScreen.INVENTORY) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = StatusRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "$lowStockCount مواد ناقصة",
                                fontSize = 11.sp,
                                color = StatusRed,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Language Switcher Dropdown
                Box {
                    Surface(
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { showLangMenu = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = currentLanguage.displayName,
                                fontSize = 12.sp,
                                color = TextWhite
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false }
                    ) {
                        AppLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.displayName) },
                                onClick = {
                                    onLanguageChange(lang)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }

                // Current User info badge
                if (currentUser != null) {
                    Surface(
                        color = GoldContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User",
                                tint = GoldLight,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = currentUser.fullName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        }
                    }

                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = StatusRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
