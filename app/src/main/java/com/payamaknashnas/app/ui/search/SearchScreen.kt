package com.payamaknashnas.app.ui.search

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
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
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
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.data.UserProfile
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.components.Avatar
import com.payamaknashnas.app.ui.components.LuxField
import com.payamaknashnas.app.ui.components.LuxScreen
import com.payamaknashnas.app.ui.components.UserRow
import com.payamaknashnas.app.ui.theme.Lux
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    app: AppState,
    onBack: () -> Unit,
    onPick: (String, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    var hint by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val r = app.repo.suggestUsers(12)
        if (r is OpResult.Ok) suggestions = r.value
    }

    LaunchedEffect(query) {
        if (query.trim().length < 2) {
            results = emptyList()
            hint = null
            return@LaunchedEffect
        }
        searching = true
        delay(320)
        val r = app.repo.searchUsers(query)
        searching = false
        when (r) {
            is OpResult.Ok -> {
                results = r.value
                hint = if (r.value.isEmpty()) "کاربری با این نام مستعار پیدا نشد." else null
            }
            is OpResult.Err -> hint = r.message
        }
    }

    LuxScreen(
        title = "جست‌وجوی کاربر",
        subtitle = "با نام مستعار جست‌وجو کنید",
        onBack = onBack
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.padding(horizontal = 16.dp)) {
                LuxField(
                    value = query,
                    onValueChange = { if (it.length <= 20) query = it },
                    placeholder = "نام مستعار (حداقل ۲ نویسه)",
                    leading = {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = null,
                            tint = if (query.isBlank()) Lux.TextMuted else Lux.Gold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                if (searching) "در حال جست‌وجو…"
                else PersianDate.toPersianDigits(results.size.toString()) + " نتیجه",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 22.dp)
            )

            if (hint != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Lux.GoldGhost)
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Text(hint ?: "", style = MaterialTheme.typography.bodySmall, color = Lux.Gold)
                }
            }

            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
            ) {
                if (query.trim().length < 2) {
                    item {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 26.dp, start = 8.dp, end = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.PersonSearch,
                                    null,
                                    tint = Lux.Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(7.dp))
                                Text("پیشنهاد برای شروع", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                "این کاربران تازه‌ترین اعضای برنامه هستند.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 3.dp, bottom = 8.dp)
                            )
                        }
                    }
                    items(suggestions.size) { i ->
                        val u = suggestions[i]
                        Box(
                            Modifier
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(18.dp))
                        ) {
                            UserRow(u, onClick = { onPick(u.uid, u.alias) }) {
                                Avatar("↑", 0, size = 30.dp)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                } else {
                    items(results.size) { i ->
                        val u = results[i]
                        Box(
                            Modifier
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(18.dp))
                                .clickable { onPick(u.uid, u.alias) }
                        ) {
                            UserRow(u, onClick = { onPick(u.uid, u.alias) }) {
                                Text(
                                    "پیام",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Lux.Gold),
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}
