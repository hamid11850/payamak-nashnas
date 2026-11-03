package com.payamaknashnas.app.ui.blocked

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Block
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
import com.payamaknashnas.app.data.BlockedUser
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.components.Avatar
import com.payamaknashnas.app.ui.components.EmptyState
import com.payamaknashnas.app.ui.components.LuxScreen
import com.payamaknashnas.app.ui.theme.Lux
import kotlinx.coroutines.launch

@Composable
fun BlockedScreen(app: AppState, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var list by remember { mutableStateOf<List<BlockedUser>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var key by remember { mutableStateOf(0) }

    LaunchedEffect(key) {
        loading = true
        val r = app.repo.blockedUsers()
        list = if (r is OpResult.Ok) r.value else emptyList()
        loading = false
    }

    LuxScreen(
        title = "کاربران مسدودشده",
        subtitle = "کاربر مسدودشده نمی‌تواند به شما پیام بدهد",
        onBack = onBack
    ) {
        if (loading) {
            com.payamaknashnas.app.ui.components.LoadingBlock()
        } else if (list.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Block,
                title = "فهرست مسدودها خالی است",
                description = "برای مسدود کردن یک کاربر، در صفحه گفت‌وگو منوی بالای صفحه را باز کنید."
            )
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                item { Spacer(Modifier.height(6.dp)) }
                items(list.size) { i ->
                    val b = list[i]
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Lux.Surface.copy(alpha = 0.86f))
                            .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(18.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(b.alias, b.alias.length % 8, size = 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(b.alias, style = MaterialTheme.typography.titleMedium)
                            Text(
                                if (b.blockedAt > 0) "مسدود شده در " + PersianDate.dateOnly(b.blockedAt) else "مسدود شده",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp)
                            )
                        }
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Lux.GoldGhost)
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                "رفع مسدودی",
                                style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
                item { Spacer(Modifier.height(18.dp)) }
            }
        }
    }
}
