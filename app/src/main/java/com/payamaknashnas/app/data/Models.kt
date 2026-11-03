package com.payamaknashnas.app.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

/**
 * پروفایل عمومی کاربر. هرگز شماره موبایل یا هویت واقعی را در بر نمی‌گیرد.
 */
data class UserProfile(
    val uid: String = "",
    val alias: String = "",
    val aliasLower: String = "",
    val allowMessages: Boolean = true,
    val bio: String = "",
    val createdAt: Long = 0L,
    val colorIndex: Int = 0,
    val banned: Boolean = false
) {
    /** حرف اول نام مستعار برای آواتار */
    val initial: String get() = alias.trim().take(1)
}

/** پیام ناشناس بین دو کاربر عضو. */
data class Message(
    val id: String = "",
    val fromUid: String = "",
    val fromAlias: String = "",
    val toUid: String = "",
    val toAlias: String = "",
    val text: String = "",
    val createdAt: Long = 0L,
    val readAt: Long = 0L,
    val threadId: String = "",
    val replyToId: String = "",
    val replyToSnippet: String = "",
    val deletedForFrom: Boolean = false,
    val deletedForTo: Boolean = false
) {
    val isRead: Boolean get() = readAt > 0L
}

/** کاربری که توسط کاربر جاری مسدود شده است. */
data class BlockedUser(
    val uid: String = "",
    val alias: String = "",
    val blockedAt: Long = 0L
)

object Mapper {
    private fun ts(snap: DocumentSnapshot, field: String): Long =
        (snap.get(field) as? Timestamp)?.toDate()?.time ?: 0L

    fun user(snap: DocumentSnapshot): UserProfile = UserProfile(
        uid = snap.getString("uid") ?: snap.id,
        alias = snap.getString("alias") ?: "کاربر ناشناس",
        aliasLower = snap.getString("aliasLower") ?: "",
        allowMessages = snap.getBoolean("allowMessages") ?: true,
        bio = snap.getString("bio") ?: "",
        createdAt = ts(snap, "createdAt"),
        colorIndex = (snap.getLong("colorIndex") ?: 0L).toInt(),
        banned = snap.getBoolean("banned") ?: false
    )

    fun message(snap: DocumentSnapshot): Message = Message(
        id = snap.id,
        fromUid = snap.getString("fromUid") ?: "",
        fromAlias = snap.getString("fromAlias") ?: "ناشناس",
        toUid = snap.getString("toUid") ?: "",
        toAlias = snap.getString("toAlias") ?: "",
        text = snap.getString("text") ?: "",
        createdAt = ts(snap, "createdAt"),
        readAt = snap.getLong("readAt") ?: 0L,
        threadId = snap.getString("threadId") ?: "",
        replyToId = snap.getString("replyToId") ?: "",
        replyToSnippet = snap.getString("replyToSnippet") ?: "",
        deletedForFrom = snap.getBoolean("deletedForFrom") ?: false,
        deletedForTo = snap.getBoolean("deletedForTo") ?: false
    )
}

/** ساخت شناسه یکتای گفتگو برای یک جفت کاربر (مستقل از ترتیب). */
fun threadIdOf(a: String, b: String): String =
    if (a < b) "$a|$b" else "$b|$a"

const val MAX_MESSAGE_LENGTH = 500
const val RATE_LIMIT_MAX = 20
const val RATE_LIMIT_WINDOW_MS = 60_000L
