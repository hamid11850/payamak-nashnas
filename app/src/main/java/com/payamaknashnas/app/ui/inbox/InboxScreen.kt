package com.payamaknashnas.app.ui.inbox

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.launch

@Composable
fun InboxScreen(
    app: AppState,
    onOpenChat: (String, String) -> Unit,
    onSearch: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var threads by remember { mutableStateOf<List<Message>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey, app.unread) {
        loading = true
        val r = app.repo.fetchInbox()
        threads = if (r is OpResult.Ok) r.value else emptyList()
        loading = false
    }

    LuxScreen(
        title = "صندوق پیام‌ها",
        subtitle = "${PersianDate.toPersianDigits(app.unread.toString())} پیام خوانده‌نشده",
        actions = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, contentDescription = "جست‌وجوی کاربر", tint = Lux.Gold)
            }
        }
    ) {
        if (loading) {
            com.payamaknashnas.app.ui.components.LoadingBlock()
        } else if (threads.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.MailOutline,
                title = "صندوق خالی است",
                description = "هنوز پیام ناشناسی دریافت نکرده‌اید. می‌توانید خودتان اولین پیام را بفرستید."
            ) {
                GoldButton("ارسال پیام ناشناس", onClick = onSearch, modifier = Modifier.padding(horizontal = 24.dp))
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(Modifier.height(6.dp))
                    if (app.me?.allowMessages == false) {
                        ClosedBanner()
                        Spacer(Modifier.height(10.dp))
                    }
                }
                items(threads.size) { index ->
                    val m = threads[index]
                    MessageCard(
                        m = m,
                        onClick = {
                            scope.launch {
                                app.repo.markRead(m.id)
                                app.refreshCounters()
                                reloadKey++
                            }
                            onOpenChat(m.fromUid, m.fromAlias)
                        },
                        onDelete = {
                            scope.launch {
                                app.repo.deleteForMe(m)
                                app.refreshCounters()
                                reloadKey++
                            }
                        }
                    )
                    Spacer(Modifier.height(9.dp))
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}

@Composable
private fun ClosedBanner() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Lux.DangerGhost)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "دریافت پیام شما غیرفعال است؛ کاربران نمی‌توانند به شما پیام بفرستند.",
            style = MaterialTheme.typography.bodySmall,
            color = Lux.Danger
        )
    }
}

@Composable
private fun MessageCard(m: Message, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (m.isRead) Lux.Surface.copy(alpha = 0.82f) else Lux.SurfaceHigh)
            .border(
                1.dp,
                if (m.isRead) Lux.StrokeSoft else Lux.Gold.copy(alpha = 0.32f),
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(m.fromAlias, m.fromAlias.length % 8, size = 46.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    m.fromAlias,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (m.isRead) FontWeight.Medium else FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    PersianDate.relative(m.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                )
            }
            Text(
                m.text,
                style = MaterialTheme.typography.bodySmall.copy(color = Lux.TextSecondary),
                maxLines = 2,
                modifier = Modifier.padding(top = 3.dp)
            )
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (m.isRead) Lux.TextMuted else Lux.Gold)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    if (m.isRead) "خوانده‌شده · " + PersianDate.fullDateTime(m.createdAt)
                    else "جدید · " + PersianDate.fullDateTime(m.createdAt),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                )
            }
        }
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "حذف پیام",
                tint = Lux.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
