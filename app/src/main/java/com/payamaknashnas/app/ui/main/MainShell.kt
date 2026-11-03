package com.payamaknashnas.app.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.blocked.BlockedScreen
import com.payamaknashnas.app.ui.chat.ChatScreen
import com.payamaknashnas.app.ui.components.LuxBottomBar
import com.payamaknashnas.app.ui.home.HomeScreen
import com.payamaknashnas.app.ui.inbox.InboxScreen
import com.payamaknashnas.app.ui.profile.ProfileScreen
import com.payamaknashnas.app.ui.report.ReportScreen
import com.payamaknashnas.app.ui.search.SearchScreen
import com.payamaknashnas.app.ui.send.ComposeMessageScreen
import com.payamaknashnas.app.ui.sent.SentScreen
import com.payamaknashnas.app.ui.settings.SettingsScreen

/** مقصدهای ناوبری برنامه. */
sealed interface Route {
    data object Home : Route
    data object Inbox : Route
    data object Sent : Route
    data object Settings : Route
    data object Search : Route
    data object Blocked : Route
    data object Profile : Route
    data class Compose(val toUid: String, val toAlias: String) : Route
    data class Chat(val otherUid: String, val otherAlias: String) : Route
    data class Report(val targetUid: String, val targetAlias: String, val messageId: String) : Route
}

@Composable
fun MainShell(app: AppState, onSignOut: () -> Unit) {
    var route by remember { mutableStateOf<Route>(Route.Home) }
    val tabs = remember {
        listOf(
            Triple("home", Icons.Filled.Home, "خانه"),
            Triple("inbox", Icons.Filled.MailOutline, "صندوق"),
            Triple("sent", Icons.Filled.Send, "ارسال‌ها"),
            Triple("settings", Icons.Filled.Settings, "تنظیمات")
        )
    }
    val activeTab = when (route) {
        Route.Home -> "home"
        Route.Inbox, is Route.Chat -> "inbox"
        Route.Sent -> "sent"
        else -> "settings"
    }

    BackHandler(enabled = route != Route.Home) {
        route = when (route) {
            is Route.Chat -> Route.Inbox
            Route.Search, Route.Blocked -> Route.Settings
            else -> Route.Home
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f)) {
            AnimatedContent(
                targetState = route,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(120)) },
                label = "route"
            ) { r ->
                when (r) {
                    Route.Home -> HomeScreen(
                        app = app,
                        onCompose = { route = Route.Search },
                        onOpenInbox = { route = Route.Inbox },
                        onOpenChat = { uid, alias -> route = Route.Chat(uid, alias) }
                    )
                    Route.Inbox -> InboxScreen(
                        app = app,
                        onOpenChat = { uid, alias -> route = Route.Chat(uid, alias) },
                        onSearch = { route = Route.Search }
                    )
                    Route.Sent -> SentScreen(
                        app = app,
                        onOpenChat = { uid, alias -> route = Route.Chat(uid, alias) },
                        onCompose = { route = Route.Search }
                    )
                    Route.Settings -> SettingsScreen(
                        app = app,
                        onBlocked = { route = Route.Blocked },
                        onProfile = { route = Route.Profile },
                        onReport = { route = Route.Report("", "", "") },
                        onSignOut = onSignOut
                    )
                    Route.Search -> SearchScreen(
                        app = app,
                        onBack = { route = Route.Home },
                        onPick = { uid, alias -> route = Route.Compose(uid, alias) }
                    )
                    Route.Blocked -> BlockedScreen(app, onBack = { route = Route.Settings })
                    Route.Profile -> ProfileScreen(app, onBack = { route = Route.Settings })
                    is Route.Compose -> ComposeMessageScreen(
                        app = app,
                        toUid = r.toUid,
                        toAlias = r.toAlias,
                        onBack = { route = Route.Search },
                        onSent = { route = Route.Sent }
                    )
                    is Route.Chat -> ChatScreen(
                        app = app,
                        otherUid = r.otherUid,
                        otherAlias = r.otherAlias,
                        onBack = { route = Route.Inbox },
                        onReport = { route = Route.Report(r.otherUid, r.otherAlias, "") }
                    )
                    is Route.Report -> ReportScreen(
                        app = app,
                        targetUid = r.targetUid,
                        targetAlias = r.targetAlias,
                        messageId = r.messageId,
                        onBack = { route = Route.Settings }
                    )
                }
            }
        }
        Box(Modifier.navigationBarsPadding()) {
            LuxBottomBar(
                items = tabs,
                selectedRoute = activeTab,
                unread = app.unread,
                onSelect = { key ->
                    route = when (key) {
                        "home" -> Route.Home
                        "inbox" -> Route.Inbox
                        "sent" -> Route.Sent
                        else -> Route.Settings
                    }
                }
            )
        }
    }
}
