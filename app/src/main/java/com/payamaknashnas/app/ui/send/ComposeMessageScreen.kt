package com.payamaknashnas.app.ui.send

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import com.payamaknashnas.app.data.MAX_MESSAGE_LENGTH
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.data.UserProfile
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
fun ComposeMessageScreen(
    app: AppState,
    toUid: String,
    toAlias: String,
    onBack: () -> Unit,
    onSent: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var target by remember { mutableStateOf<UserProfile?>(null) }
    var remaining by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(toUid) {
        val p = app.repo.fetchProfile(toUid)
        if (p is OpResult.Ok) target = p.value
        val q = app.repo.quotaState()
        if (q is OpResult.Ok) remaining = q.value.first
    }

    LuxScreen(title = "ارسال پیام ناشناس", subtitle = "هویت شما پنهان می‌ماند", onBack = onBack) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            LuxCard {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(target?.alias ?: toAlias, target?.colorIndex ?: (toAlias.length % 8), size = 50.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(target?.alias ?: toAlias, style = MaterialTheme.typography.titleLarge)
                            Text(
                                when {
                                    target == null -> "در حال دریافت اطلاعات…"
                                    target?.allowMessages == false -> "این کاربر دریافت پیام را بسته است"
                                    else -> "آماده دریافت پیام"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (target?.allowMessages == false) Lux.Danger else Lux.TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            LuxField(
                value = text,
                onValueChange = { if (it.length <= MAX_MESSAGE_LENGTH) text = it },
                placeholder = "متن پیام خود را بنویسید…",
                singleLine = false,
                minLines = 6,
                maxLines = 9
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    PersianDate.toPersianDigits(text.length.toString()) + " / ۵۰۰",
                    style = MaterialTheme.typography.bodySmall
                )
                if (remaining != null) {
                    Text(
                        "سهمیه ارسال: " + PersianDate.toPersianDigits(remaining.toString()) + " پیام",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            NoticeBar(error)

            Spacer(Modifier.height(14.dp))
            GoldButton(
                text = "ارسال پیام",
                loading = busy,
                enabled = text.trim().isNotEmpty() && target?.allowMessages != false
            ) {
                busy = true; error = null
                scope.launch {
                    val r = app.repo.sendMessage(toUid, target?.alias ?: toAlias, text)
                    busy = false
                    when (r) {
                        is OpResult.Ok -> {
                            app.flash("پیام شما ارسال شد.", isError = false)
                            app.refreshCounters()
                            onSent()
                        }
                        is OpResult.Err -> error = r.message
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Lux.Surface.copy(alpha = 0.6f))
                    .padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Lux.Gold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(9.dp))
                Column {
                    Text(
                        "ناشناس در برابر دریافت‌کننده",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp)
                    )
                    Text(
                        "دریافت‌کننده فقط نام مستعار شما را می‌بیند. شماره موبایل شما هرگز نمایش داده نمی‌شود؛ با این حال مدیر سامانه برای بررسی تخلف به آن دسترسی دارد.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
