package com.payamaknashnas.app.data

/**
 * قرارداد لایه داده. دو پیاده‌سازی وجود دارد:
 *  - FirestoreRepo: اتصال واقعی به Firebase
 *  - DemoRepo: داده‌های محلی و حافظه‌ای برای زمانی که google-services.json تنظیم نشده است
 */
interface Repo {
    fun currentUid(): String?
    fun isSignedIn(): Boolean
    fun signOut()

    suspend fun fetchMyProfile(): OpResult<UserProfile>
    suspend fun fetchProfile(userId: String): OpResult<UserProfile>
    suspend fun createProfile(alias: String, phone: String): OpResult<UserProfile>
    suspend fun updateAlias(alias: String): OpResult<Unit>
    suspend fun updateBio(bio: String): OpResult<Unit>
    suspend fun setAllowMessages(enabled: Boolean): OpResult<Unit>

    suspend fun searchUsers(query: String, limit: Long = 20): OpResult<List<UserProfile>>
    suspend fun suggestUsers(limit: Long = 12): OpResult<List<UserProfile>>

    suspend fun blockUser(targetUid: String, targetAlias: String): OpResult<Unit>
    suspend fun unblockUser(targetUid: String): OpResult<Unit>
    suspend fun blockedUsers(): OpResult<List<BlockedUser>>

    suspend fun sendMessage(
        toUid: String,
        toAlias: String,
        text: String,
        replyTo: Message? = null
    ): OpResult<Unit>

    suspend fun fetchInbox(): OpResult<List<Message>>
    suspend fun fetchOutbox(): OpResult<List<Message>>
    suspend fun markRead(messageId: String): OpResult<Unit>
    suspend fun deleteForMe(message: Message): OpResult<Unit>
    suspend fun threadWith(otherUid: String): OpResult<List<Message>>

    suspend fun report(
        targetUid: String,
        targetAlias: String,
        messageId: String,
        reason: String,
        note: String
    ): OpResult<Unit>

    suspend fun reportAndBlock(
        targetUid: String,
        targetAlias: String,
        messageId: String,
        reason: String,
        note: String
    ): OpResult<Unit>

    suspend fun deleteMyAccount(): OpResult<Unit>
    suspend fun quotaState(): OpResult<Pair<Int, Long>>
}
