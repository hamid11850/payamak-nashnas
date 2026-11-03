package com.payamaknashnas.app.ui.chat

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.data.MAX_MESSAGE_LENGTH
import com.payamaknashnas.app.data.Message
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.components.Avatar
import com.payamaknashnas.app.ui.components.LuxField
import com.payamaknashnas.app.ui.components.LuxScreen
import com.payamaknashnas.app.ui.theme.Lux
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    app: AppState,
    otherUid: String,
    otherAlias: String,
    onBack: () -> Unit,
    onReport: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var draft by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(otherUid, reloadKey) {
        val r = app.repo.threadWith(otherUid)
        messages = if (r is OpResult.Ok) r.value else emptyList()
        messages.filter { it.toUid == app.me?.uid && !it.isRead }.forEach { app.repo.markRead(it.id) }
        app.refreshCounters()
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    fun send() {
        val text = draft.trim()
        if (text.isEmpty() || busy) return
        busy = true; error = null
        scope.launch {
            val r = app.repo.sendMessage(otherUid, otherAlias, text)
            busy = false
            when (r) {
                is OpResult.Ok -> {
                    draft = ""
                    reloadKey++
                }
                is OpResult.Err -> error = r.message
            }
        }
    }

    LuxScreen(
        title = otherAlias,
        subtitle = "گفت‌وگوی ناشناس",
        onBack = onBack,
        actions = {
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "گزینه‌ها", tint = Lux.Gold)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("گزارش تخلف") },
                        leadingIcon = { Icon(Icons.Filled.Report, null, tint = Lux.Danger) },
                        onClick = { menuOpen = false; onReport() }
                    )
                    DropdownMenuItem(
                        text = { Text("مسدود کردن کاربر") },
                        leadingIcon = { Icon(Icons.Filled.Block, null, tint = Lux.Danger) },
                        onClick = {
                            menuOpen = false
                            scope.launch {
                                val r = app.repo.blockUser(otherUid, otherAlias)
                                if (r is OpResult.Ok) {
                                    app.flash("کاربر مسدود شد.", isError = false)
                                    onBack()
                                } else if (r is OpResult.Err) {
                                    app.flash(r.message)
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        Column(Modifier.fillMaxSize().imePadding()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Avatar(otherAlias, otherAlias.length % 8, size = 58.dp)
                        Text(otherAlias, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                        Text(
                            "هویت واقعی این کاربر برای شما نمایش داده نمی‌شود.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
                items(messages.size) { index ->
                    val m = messages[index]
                    val mine = m.fromUid == app.me?.uid
                    Bubble(m, mine)
                    Spacer(Modifier.height(8.dp))
                }
                item { Spacer(Modifier.height(10.dp)) }
            }

            if (error != null) {
                Text(
                    error ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Lux.Danger,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(Modifier.weight(1f)) {
                    LuxField(
                        value = draft,
                        onValueChange = { if (it.length <= MAX_MESSAGE_LENGTH) draft = it },
                        placeholder = "پیام خود را بنویسید…",
                        singleLine = false,
                        minLines = 1,
                        maxLines = 4
                    )
                }
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (draft.isBlank()) Lux.SurfaceHigh else Lux.Gold)
                        .clickable(enabled = draft.isNotBlank() && !busy) { send() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "ارسال",
                        tint = if (draft.isBlank()) Lux.TextMuted else Color(0xFF1A1305),
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun Bubble(m: Message, mine: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.Start else Arrangement.End
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (mine) 6.dp else 20.dp,
                        bottomEnd = if (mine) 20.dp else 6.dp
                    )
                )
                .background(if (mine) Lux.Gold.copy(alpha = 0.16f) else Lux.Surface)
                .border(
                    1.dp,
                    if (mine) Lux.Gold.copy(alpha = 0.32f) else Lux.StrokeSoft,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            if (m.replyToSnippet.isNotBlank()) {
                Text(
                    m.replyToSnippet,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = Lux.TextMuted,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                )
            }
            Text(m.text, style = MaterialTheme.typography.bodyLarge, color = Lux.TextPrimary)
            Text(
                PersianDate.fullDateTime(m.createdAt),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                modifier = Modifier.padding(top = 5.dp)
            )
        }
    }
}
