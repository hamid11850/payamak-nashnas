package com.payamaknashnas.app.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.components.GoldButton
import com.payamaknashnas.app.ui.components.LuxCard
import com.payamaknashnas.app.ui.components.LuxField
import com.payamaknashnas.app.ui.components.LuxScreen
import com.payamaknashnas.app.ui.components.NoticeBar
import com.payamaknashnas.app.ui.theme.Lux
import kotlinx.coroutines.launch

private val reasons = listOf(
    "توهین یا فحش",
    "تهدید و ارعاب",
    "مزاحمت مکرر",
    "محتوای نامناسب",
    "کلاهبرداری یا فریب",
    "سایر موارد"
)

@Composable
fun ReportScreen(
    app: AppState,
    targetUid: String,
    targetAlias: String,
    messageId: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var alias by remember { mutableStateOf(targetAlias) }
    var reason by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var alsoBlock by remember { mutableStateOf(true) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

    LuxScreen(title = "گزارش تخلف", subtitle = "گزارش شما محرمانه بررسی می‌شود", onBack = onBack) {
        if (done) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(60.dp))
                Box(
                    Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(Lux.Success.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Check, null, tint = Lux.Success, modifier = Modifier.size(38.dp))
                }
                Text(
                    "گزارش ثبت شد",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 18.dp)
                )
                Text(
                    "گزارش شما برای بررسی ارسال شد. اگر این کاربر را مسدود کرده باشید، دیگر نمی‌تواند به شما پیام بدهد.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(22.dp))
                GoldButton("بازگشت", onClick = onBack)
            }
            return@LuxScreen
        }

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(6.dp))
            LuxCard {
                Column {
                    Text("کاربر گزارش‌شده", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (targetUid.isBlank()) {
                        LuxField(
                            value = alias,
                            onValueChange = { alias = it },
                            placeholder = "نام مستعار کاربر را وارد کنید"
                        )
                        Text(
                            "اگر از داخل گفت‌وگو گزارش می‌دهید، این بخش خودکار پر می‌شود.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Lux.GoldGhost),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(alias.take(1), color = Lux.Gold, style = MaterialTheme.typography.titleMedium)
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(alias, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("دلیل گزارش", style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold.copy(alpha = 0.85f)))
            Spacer(Modifier.height(6.dp))
            LuxCard(padding = 8.dp) {
                Column {
                    reasons.forEach { r ->
                        val selected = reason == r
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) Lux.GoldGhost else Color.Transparent)
                                .clickable { reason = r }
                                .padding(horizontal = 12.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (selected) Lux.Gold else Color.Transparent)
                                    .border(1.5.dp, if (selected) Lux.Gold else Lux.Stroke, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) {
                                    Icon(Icons.Filled.Check, null, tint = Color(0xFF1A1305), modifier = Modifier.size(12.dp))
                                }
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(r, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("توضیحات (اختیاری)", style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold.copy(alpha = 0.85f)))
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { if (it.length <= 400) note = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("اگر توضیح بیشتری دارید بنویسید…", style = MaterialTheme.typography.bodySmall, color = Lux.TextMuted)
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.Rtl, textAlign = TextAlign.Right),
                minLines = 4,
                maxLines = 7,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Lux.Gold.copy(alpha = 0.7f),
                    unfocusedBorderColor = Lux.Stroke,
                    focusedContainerColor = Lux.Surface.copy(alpha = 0.6f),
                    unfocusedContainerColor = Lux.Surface.copy(alpha = 0.4f),
                    cursorColor = Lux.Gold,
                    focusedTextColor = Lux.TextPrimary,
                    unfocusedTextColor = Lux.TextPrimary
                )
            )
            Text(
                PersianDate.toPersianDigits(note.length.toString()) + " / ۴۰۰",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp, start = 4.dp)
            )

            Spacer(Modifier.height(12.dp))
            LuxCard(padding = 6.dp) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { alsoBlock = !alsoBlock }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("مسدود کردن این کاربر", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "پس از گزارش، دیگر پیامی از او دریافت نمی‌کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Switch(
                        checked = alsoBlock,
                        onCheckedChange = { alsoBlock = it },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFF1A1305),
                            checkedTrackColor = Lux.Gold,
                            uncheckedThumbColor = Lux.TextMuted,
                            uncheckedTrackColor = Lux.SurfaceHigh,
                            uncheckedBorderColor = Lux.Stroke
                        )
                    )
                }
            }

            NoticeBar(error)

            Spacer(Modifier.height(14.dp))
            GoldButton(
                text = "ثبت گزارش",
                loading = busy,
                enabled = reason != null
            ) {
                busy = true; error = null
                scope.launch {
                    val r = if (alsoBlock && targetUid.isNotBlank()) {
                        app.repo.reportAndBlock(targetUid, alias, messageId, reason ?: "", note)
                    } else {
                        app.repo.report(targetUid, alias, messageId, reason ?: "", note)
                    }
                    busy = false
                    when (r) {
                        is OpResult.Ok -> done = true
                        is OpResult.Err -> error = r.message
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Lux.Surface.copy(alpha = 0.5f))
                    .padding(13.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.Warning, null, tint = Lux.Gold, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "گزارش‌های ساختگی یا تکراری ممکن است باعث محدود شدن حساب گزارش‌دهنده شود.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}
