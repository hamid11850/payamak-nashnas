package com.payamaknashnas.app.ui.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.payamaknashnas.app.ui.components.GoldButton
import com.payamaknashnas.app.ui.components.LuxCard
import com.payamaknashnas.app.ui.components.LuxField
import com.payamaknashnas.app.ui.components.LuxScreen
import com.payamaknashnas.app.ui.components.NoticeBar
import com.payamaknashnas.app.ui.theme.Lux
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(app: AppState, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val me = app.me
    var alias by remember { mutableStateOf(me?.alias ?: "") }
    var bio by remember { mutableStateOf(me?.bio ?: "") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }

    LaunchedEffect(me?.uid) {
        alias = me?.alias ?: ""
        bio = me?.bio ?: ""
    }

    LuxScreen(title = "پروفایل من", subtitle = "نام مستعار شما همان چیزی است که دیگران می‌بینند", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            Column(
                Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Avatar(alias.ifBlank { "؟" }, me?.colorIndex ?: 0, size = 88.dp)
                Text(
                    alias.ifBlank { "بدون نام" },
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 12.dp)
                )
                Row(
                    Modifier.padding(top = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Lock, null, tint = Lux.Success, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("شماره شما برای دیگران پنهان است", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp))
                }
            }

            Spacer(Modifier.height(20.dp))
            LuxCard {
                Column {
                    Text("نام مستعار", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "این نام در فهرست کاربران و روی پیام‌های شما دیده می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 3.dp, bottom = 10.dp)
                    )
                    LuxField(
                        value = alias,
                        onValueChange = { if (it.length <= 20) { alias = it; saved = false } },
                        placeholder = "نام مستعار تازه"
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("۳ تا ۲۰ نویسه", style = MaterialTheme.typography.bodySmall)
                        Text(
                            PersianDate.toPersianDigits(alias.length.toString()) + " / ۲۰",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            LuxCard {
                Column {
                    Text("درباره من", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "یک جمله کوتاه که در پروفایل شما نمایش داده می‌شود.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 3.dp, bottom = 10.dp)
                    )
                    LuxField(
                        value = bio,
                        onValueChange = { if (it.length <= 80) { bio = it; saved = false } },
                        placeholder = "مثلاً: اینجا برای شنیدن، نه قضاوت."
                    )
                    Text(
                        PersianDate.toPersianDigits(bio.length.toString()) + " / ۸۰",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            NoticeBar(error)
            if (saved) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Lux.Success.copy(alpha = 0.1f))
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, null, tint = Lux.Success, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("تغییرات ذخیره شد.", style = MaterialTheme.typography.bodySmall, color = Lux.Success)
                }
            }

            Spacer(Modifier.height(14.dp))
            GoldButton(
                text = "ذخیره تغییرات",
                loading = busy,
                enabled = alias.trim().length >= 3
            ) {
                busy = true; error = null
                scope.launch {
                    var failed: String? = null
                    if (alias.trim() != me?.alias) {
                        val r = app.repo.updateAlias(alias)
                        if (r is OpResult.Err) failed = r.message
                    }
                    if (failed == null && bio != me?.bio) {
                        val r = app.repo.updateBio(bio)
                        if (r is OpResult.Err) failed = r.message
                    }
                    busy = false
                    if (failed == null) {
                        app.reloadProfile()
                        saved = true
                        app.flash("پروفایل به‌روزرسانی شد.", isError = false)
                    } else {
                        error = failed
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Lux.Surface.copy(alpha = 0.5f))
                    .padding(13.dp)
            ) {
                Text(
                    "تغییر نام مستعار روی پیام‌های قبلی اثر نمی‌گذارد؛ پیام‌های قدیمی با نامی که در زمان ارسال داشتید نمایش داده می‌شوند.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
