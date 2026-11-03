package com.payamaknashnas.app.ui.home

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
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.data.Message
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.components.Avatar
import com.payamaknashnas.app.ui.theme.Lux
import java.util.Calendar

@Composable
fun HomeScreen(
    app: AppState,
    onCompose: () -> Unit,
    onOpenInbox: () -> Unit,
    onOpenChat: (String, String) -> Unit,
    onPrivacy: () -> Unit = {}
) {
    val me = app.me
    var recent by remember { mutableStateOf<List<Message>>(emptyList()) }

    LaunchedEffect(me?.uid, app.unread) {
        val r = app.repo.fetchInbox()
        if (r is OpResult.Ok) recent = r.value.take(3)
    }

    val hour = remember {
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }
    val greeting = when (hour) {
        in 4..11 -> "صبح بخیر"
        in 12..16 -> "ظهر بخیر"
        in 17..20 -> "عصر بخیر"
        else -> "شب بخیر"
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
        ) {
            item {
                Spacer(Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(greeting, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            me?.alias ?: "مهمان",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Avatar(me?.alias ?: "؟", me?.colorIndex ?: 0, size = 52.dp)
                }
                if (app.demoMode) {
                    Spacer(Modifier.height(12.dp))
                    DemoBanner()
                }
                Spacer(Modifier.height(20.dp))
            }

            item {
                HeroCard(unread = app.unread, onOpenInbox = onOpenInbox)
                Spacer(Modifier.height(14.dp))
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction(
                        title = "ارسال پیام",
                        subtitle = "به یک ناشناس",
                        icon = Icons.Filled.Send,
                        accent = Lux.Gold,
                        modifier = Modifier.weight(1f),
                        onClick = onCompose
                    )
                    QuickAction(
                        title = "حریم خصوصی",
                        subtitle = if (me?.allowMessages == true) "دریافت باز است" else "دریافت بسته است",
                        icon = Icons.Filled.Lock,
                        accent = Lux.Success,
                        modifier = Modifier.weight(1f),
                        onClick = onPrivacy
                    )
                }
                Spacer(Modifier.height(22.dp))
            }

            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "آخرین پیام‌ها",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = Lux.Gold.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "مشاهده همه",
                        style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenInbox() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
            }

            if (recent.isEmpty()) {
                item { EmptyRecent(onOpenInbox) }
            } else {
                items(recent.size) { index ->
                    val m = recent[index]
                    RecentRow(m) { onOpenChat(m.fromUid, m.fromAlias) }
                    Spacer(Modifier.height(8.dp))
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }

        NoticeOverlay(app)
    }
}

@Composable
fun NoticeOverlay(app: AppState) {
    if (app.notice != null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Row(
                Modifier
                    .padding(horizontal = 18.dp, vertical = 18.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (app.noticeIsError) Lux.DangerGhost else Lux.GoldGhost)
                    .border(
                        1.dp,
                        (if (app.noticeIsError) Lux.Danger else Lux.Gold).copy(alpha = 0.4f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable { app.clearNotice() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    app.notice ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (app.noticeIsError) Lux.Danger else Lux.Gold,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DemoBanner() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Lux.GoldGhost)
            .border(1.dp, Lux.Gold.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.VisibilityOff, null, tint = Lux.Gold, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "حالت نمایشی — Firebase وصل نشده است",
            style = MaterialTheme.typography.bodySmall,
            color = Lux.Gold
        )
    }
}

@Composable
private fun HeroCard(unread: Int, onOpenInbox: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF1C1509), Color(0xFF14181F)),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 600f)
                )
            )
            .border(1.dp, Lux.Gold.copy(alpha = 0.22f), RoundedCornerShape(28.dp))
            .clickable { onOpenInbox() }
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Lux.Gold.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Inbox, null, tint = Lux.Gold, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    "صندوق پیام‌های ناشناس",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    PersianDate.toPersianDigits(unread.toString()),
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold),
                    color = Lux.Gold
                )
                Text(
                    if (unread > 0) "  پیام خوانده‌نشده" else "  پیام جدیدی نیست",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "پیام‌ها با نام مستعار فرستنده نمایش داده می‌شوند؛ شماره هیچ‌کس دیده نمی‌شود.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun QuickAction(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(Lux.Surface.copy(alpha = 0.9f))
            .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun RecentRow(m: Message, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Lux.Surface.copy(alpha = 0.85f))
            .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(m.fromAlias, (m.fromAlias.length) % 8, size = 44.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    m.fromAlias,
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
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        if (!m.isRead) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Lux.Gold)
            )
        }
    }
}

@Composable
private fun EmptyRecent(onOpenInbox: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Lux.Surface.copy(alpha = 0.6f))
            .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(22.dp))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("هنوز پیامی ندارید", style = MaterialTheme.typography.titleMedium)
        Text(
            "وقتی کسی برای شما پیام ناشناس بفرستد، اینجا نمایش داده می‌شود.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            "رفتن به صندوق",
            style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold),
            modifier = Modifier
                .padding(top = 12.dp)
                .clickable { onOpenInbox() }
        )
    }
}
