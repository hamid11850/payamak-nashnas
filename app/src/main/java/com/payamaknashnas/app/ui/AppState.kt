package com.payamaknashnas.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.payamaknashnas.app.data.AppContainer
import com.payamaknashnas.app.data.AuthApi
import com.payamaknashnas.app.data.Message
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.Repo
import com.payamaknashnas.app.data.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** وضعیت مشترک برنامه: پروفایل جاری، شمارنده‌ها و پیام‌های کوتاه. */
class AppState(
    val repo: Repo,
    val auth: AuthApi,
    val scope: CoroutineScope
) {
    val demoMode: Boolean get() = AppContainer.demoMode

    var me by mutableStateOf<UserProfile?>(null)
        private set
    var booting by mutableStateOf(true)
        private set
    var unread by mutableStateOf(0)
        private set
    var sentTotal by mutableStateOf(0)
        private set
    var notice by mutableStateOf<String?>(null)
        private set
    var noticeIsError by mutableStateOf(true)
        private set

    fun flash(message: String, isError: Boolean = true) {
        notice = message
        noticeIsError = isError
    }

    fun clearNotice() { notice = null }

    fun setProfile(profile: UserProfile?) {
        me = profile
        if (profile != null) refreshCounters()
    }

    fun boot() {
        scope.launch {
            if (repo.isSignedIn()) {
                when (val r = repo.fetchMyProfile()) {
                    is OpResult.Ok -> me = r.value
                    is OpResult.Err -> me = null
                }
            }
            booting = false
            refreshCounters()
        }
    }

    fun refreshCounters() {
        scope.launch {
            val inbox = repo.fetchInbox()
            if (inbox is OpResult.Ok) {
                unread = inbox.value.count { !it.isRead && !it.deletedForTo }
            }
            val out = repo.fetchOutbox()
            if (out is OpResult.Ok) sentTotal = out.value.count { !it.deletedForFrom }
        }
    }

    fun signOutAndReset() {
        repo.signOut()
        me = null
        unread = 0
        sentTotal = 0
    }

    fun reloadProfile() {
        scope.launch {
            val r = repo.fetchMyProfile()
            if (r is OpResult.Ok) me = r.value
        }
    }
}

@Composable
fun rememberAppState(): AppState {
    val scope = rememberCoroutineScope()
    val state = remember {
        AppState(AppContainer.repo, AppContainer.auth, scope).also { it.boot() }
    }
    return state
}

/** آخرین پیام هر گفتگو برای نمایش در صندوق. */
fun List<Message>.latestPerThread(): List<Message> =
    groupBy { it.threadId.ifBlank { it.fromUid } }
        .mapNotNull { (_, list) -> list.maxByOrNull { it.createdAt } }
        .sortedByDescending { it.createdAt }
