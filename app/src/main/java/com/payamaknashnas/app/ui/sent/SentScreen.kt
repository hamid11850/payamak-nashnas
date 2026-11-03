package com.payamaknashnas.app.ui.sent

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.data.Message
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.components.Avatar
import com.payamaknashnas.app.ui.components.EmptyState
import com.payamaknashnas.app.ui.components.GoldButton
import com.payamaknashnas.app.ui.components.LuxScreen
import com.payamaknashnas.app.ui.theme.Lux

@Composable
fun SentScreen(
    app: AppState,
    onOpenChat: (String, String) -> Unit,
    onCompose: () -> Unit
) {
    var sent by remember { mutableStateOf<List<Message>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(app.sentTotal) {
        loading = true
        val r = app.repo.fetchOutbox()
        sent = if (r is OpResult.Ok) r.value else emptyList()
        loading = false
    }

    LuxScreen(
        title = "پیام‌های ارسال‌شده",
        subtitle = PersianDate.toPersianDigits(sent.count { it.isRead }.toString()) + " پیام خوانده شده"
    ) {
        if (loading) {
            com.payamaknashnas.app.ui.components.LoadingBlock()
        } else if (sent.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Send,
                title = "هنوز پیامی نفرستاده‌اید",
                description = "یک کاربر را انتخاب کنید و اولین پیام ناشناس خود را بفرستید."
            ) {
                GoldButton("نوشتن پیام", onClick = onCompose, modifier = Modifier.padding(horizontal = 24.dp))
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item { Spacer(Modifier.height(6.dp)) }
                items(sent.size) { i ->
                    val m = sent[i]
                    SentCard(m) { onOpenChat(m.toUid, m.toAlias) }
                    Spacer(Modifier.height(9.dp))
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}

@Composable
private fun SentCard(m: Message, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Lux.Surface.copy(alpha = 0.86f))
            .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(m.toAlias, m.toAlias.length % 8, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "به " + m.toAlias,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    PersianDate.relative(m.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                )
            }
            Text(
                m.text,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                modifier = Modifier.padding(top = 3.dp)
            )
            Row(Modifier.padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (m.isRead) Icons.Filled.DoneAll else Icons.Filled.MarkEmailUnread,
                    contentDescription = null,
                    tint = if (m.isRead) Lux.Success else Lux.TextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    if (m.isRead) "خوانده شد" else "ارسال شد — هنوز خوانده نشده",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = if (m.isRead) Lux.Success else Lux.TextMuted
                )
            }
        }
    }
}
