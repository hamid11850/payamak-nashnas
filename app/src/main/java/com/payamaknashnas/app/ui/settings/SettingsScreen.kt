package com.payamaknashnas.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.components.Avatar
import com.payamaknashnas.app.ui.components.DividerLine
import com.payamaknashnas.app.ui.components.LuxCard
import com.payamaknashnas.app.ui.components.LuxScreen
import com.payamaknashnas.app.ui.components.SettingRow
import com.payamaknashnas.app.ui.components.ToggleRow
import com.payamaknashnas.app.ui.theme.Lux
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    app: AppState,
    onBlocked: () -> Unit,
    onProfile: () -> Unit,
    onReport: () -> Unit,
    onSignOut: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val me = app.me
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    LuxScreen(title = "حریم خصوصی و تنظیمات", subtitle = "کنترل کامل دست شماست") {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            LuxCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(me?.alias ?: "؟", me?.colorIndex ?: 0, size = 54.dp)
                    Spacer(Modifier.width(13.dp))
                    Column(Modifier.weight(1f)) {
                        Text(me?.alias ?: "—", style = MaterialTheme.typography.titleLarge)
                        Text(
                            if (me?.allowMessages == true) "دریافت پیام: فعال" else "دریافت پیام: غیرفعال",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (me?.allowMessages == true) Lux.Success else Lux.Danger
                        )
                    }
                    Text(
                        "ویرایش",
                        style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onProfile() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("دریافت پیام", style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold.copy(alpha = 0.85f)))
            Spacer(Modifier.height(6.dp))
            LuxCard(padding = 4.dp) {
                Column {
                    ToggleRow(
                        title = "اجازه دریافت پیام ناشناس",
                        description = "اگر خاموش شود، هیچ‌کس نمی‌تواند به شما پیام بفرستد. این محدودیت در سرور هم اعمال می‌شود.",
                        checked = me?.allowMessages ?: false
                    ) { enabled ->
                        busy = true
                        scope.launch {
                            val r = app.repo.setAllowMessages(enabled)
                            busy = false
                            when (r) {
                                is OpResult.Ok -> {
                                    app.reloadProfile()
                                    app.flash(
                                        if (enabled) "دریافت پیام فعال شد." else "دریافت پیام غیرفعال شد.",
                                        isError = false
                                    )
                                }
                                is OpResult.Err -> app.flash(r.message)
                            }
                        }
                    }
                    DividerLine(Modifier.padding(horizontal = 14.dp))
                    SettingRow(
                        icon = Icons.Filled.Block,
                        title = "کاربران مسدودشده",
                        subtitle = "مدیریت فهرست سیاه شما",
                        onClick = onBlocked
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("امنیت و گزارش", style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold.copy(alpha = 0.85f)))
            Spacer(Modifier.height(6.dp))
            LuxCard(padding = 4.dp) {
                Column {
                    SettingRow(
                        icon = Icons.Filled.Person,
                        title = "پروفایل و نام مستعار",
                        subtitle = me?.alias ?: "",
                        onClick = onProfile
                    )
                    DividerLine(Modifier.padding(horizontal = 14.dp))
                    SettingRow(
                        icon = Icons.Filled.Report,
                        title = "گزارش تخلف",
                        subtitle = "گزارش پیام یا کاربر مزاحم",
                        tint = Lux.Danger,
                        onClick = onReport
                    )
                    DividerLine(Modifier.padding(horizontal = 14.dp))
                    SettingRow(
                        icon = Icons.Filled.Shield,
                        title = "نکات امنیتی",
                        subtitle = "شماره شما برای دیگران نمایش داده نمی‌شود",
                        tint = Lux.Success,
                        onClick = {
                            app.flash("هویت واقعی و شماره شما فقط برای مدیر سامانه قابل بررسی است.", isError = false)
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("حساب کاربری", style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold.copy(alpha = 0.85f)))
            Spacer(Modifier.height(6.dp))
            LuxCard(padding = 4.dp) {
                Column {
                    SettingRow(
                        icon = Icons.Filled.Logout,
                        title = "خروج از حساب",
                        subtitle = "شماره شما در دستگاه ذخیره نمی‌شود",
                        onClick = { confirmSignOut = true }
                    )
                    DividerLine(Modifier.padding(horizontal = 14.dp))
                    SettingRow(
                        icon = Icons.Filled.DeleteForever,
                        title = "حذف حساب کاربری",
                        subtitle = "پروفایل و دسترسی شما حذف می‌شود",
                        tint = Lux.Danger,
                        onClick = { confirmDelete = true }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Lux.Surface.copy(alpha = 0.55f))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.VisibilityOff, null, tint = Lux.Gold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(9.dp))
                Column {
                    Text(
                        "ناشناس در برابر دریافت‌کننده، نه در برابر سامانه",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp)
                    )
                    Text(
                        "این برنامه شماره فرستنده را جعل نمی‌کند و از خط مخفی برای SMS واقعی استفاده نمی‌کند. پیام‌ها درون برنامه جابه‌جا می‌شوند و شماره فرستنده برای هیچ کاربری نمایش داده نمی‌شود؛ اما برای پیگیری تخلف در اختیار مدیر سامانه است.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            if (app.demoMode) {
                Spacer(Modifier.height(12.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Lux.GoldGhost)
                        .border(1.dp, Lux.Gold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        .padding(13.dp)
                ) {
                    Text(
                        "حالت نمایشی: داده‌ها محلی و موقت هستند و به Firebase متصل نیستید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Lux.Gold
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "نسخه ۱.۰.۰ · " + PersianDate.toPersianDigits("2026"),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("حذف حساب کاربری") },
            text = {
                Text(
                    "پروفایل عمومی، نام مستعار و داده خصوصی شما حذف می‌شود و دیگر کسی نمی‌تواند به شما پیام بفرستد. این عمل قابل بازگشت نیست.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    busy = true
                    scope.launch {
                        val r = app.repo.deleteMyAccount()
                        busy = false
                        when (r) {
                            is OpResult.Ok -> {
                                app.flash("حساب شما حذف شد.", isError = false)
                                onSignOut()
                            }
                            is OpResult.Err -> app.flash(r.message)
                        }
                    }
                }) { Text("حذف کن", color = Lux.Danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("انصراف", color = Lux.TextSecondary) }
            },
            containerColor = Lux.Surface,
            titleContentColor = Lux.TextPrimary,
            textContentColor = Lux.TextSecondary
        )
    }

    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text("خروج از حساب") },
            text = {
                Text(
                    "برای ورود دوباره باید کد پیامکی را تأیید کنید. پیام‌های شما در سرور باقی می‌مانند.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmSignOut = false
                    onSignOut()
                }) { Text("خروج", color = Lux.Gold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmSignOut = false }) { Text("انصراف", color = Lux.TextSecondary) }
            },
            containerColor = Lux.Surface,
            titleContentColor = Lux.TextPrimary,
            textContentColor = Lux.TextSecondary
        )
    }
}
