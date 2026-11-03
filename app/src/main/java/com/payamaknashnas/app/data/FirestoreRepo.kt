package com.payamaknashnas.app.data

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.Date

/** نتیجه عملیات با پیام خطای فارسی آماده نمایش. */
sealed interface OpResult<out T> {
    data class Ok<T>(val value: T) : OpResult<T>
    data class Err(val message: String) : OpResult<Nothing>
}

private inline fun <T> runOp(block: () -> T): OpResult<T> = try {
    OpResult.Ok(block())
} catch (t: Throwable) {
    OpResult.Err(FaErrors.humanize(t))
}

object FaErrors {
    fun humanize(t: Throwable): String {
        val raw = t.message ?: ""
        val code = (t as? com.google.firebase.firestore.FirebaseFirestoreException)?.code
        return when {
            raw.contains("PERMISSION_DENIED", true) ->
                "دسترسی رد شد. ممکن است کاربر دریافت‌کننده پیام را بسته باشد، شما را مسدود کرده باشد یا محدودیت ارسال فعال شده باشد."
            raw.contains("UNAVAILABLE", true) || raw.contains("network", true) ->
                "اتصال به سرور برقرار نشد. اینترنت خود را بررسی کنید."
            code == com.google.firebase.firestore.FirebaseFirestoreException.Code.FAILED_PRECONDITION ->
                "این عملیات به تنظیمات بیشتری در Firebase نیاز دارد (ایندکس یا احراز هویت)."
            raw.contains("invalid-verification-code", true) -> "کد تأیید نادرست است."
            raw.contains("session-expired", true) -> "زمان کد تأیید تمام شده؛ دوباره درخواست کنید."
            raw.contains("too-many-requests", true) -> "تعداد تلاش‌ها زیاد است. کمی بعد دوباره امتحان کنید."
            raw.contains("invalid-phone-number", true) -> "شماره موبایل نامعتبر است."
            raw.contains("quota", true) -> "سهمیه ارسال پیامک تأیید تمام شده است."
            else -> if (raw.isBlank()) "خطای نامشخص رخ داد." else raw
        }
    }
}

/**
 * لایه دسترسی به داده. تمام بررسی‌های امنیتی حساس (رضایت دریافت، مسدودسازی،
 * سقف ارسال) علاوه بر این لایه، در Firestore Security Rules نیز اعمال می‌شوند.
 */
class FirestoreRepo(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : Repo {
    private val uid: String? get() = auth.currentUser?.uid
    private fun requireUid(): String = uid ?: throw IllegalStateException("کاربر وارد نشده است.")

    private fun users() = db.collection("users")
    private fun messages() = db.collection("messages")
    private fun privateDir() = db.collection("private")
    private fun blocks() = db.collection("blocks")
    private fun rateLimits() = db.collection("rateLimits")
    private fun reports() = db.collection("reports")

    // ---------------------------------------------------------------- profile
    override suspend fun fetchProfile(userId: String): OpResult<UserProfile> = runOp {
        val snap = users().document(userId).get().await()
        require(snap.exists()) { "پروفایل کاربر یافت نشد." }
        Mapper.user(snap)
    }

    override suspend fun fetchMyProfile(): OpResult<UserProfile> = runOp {
        val id = requireUid()
        val snap = users().document(id).get().await()
        require(snap.exists()) { "پروفایل یافت نشد." }
        Mapper.user(snap)
    }

    /** ثبت‌نام: ساخت پروفایل عمومی + نگه‌داشتن شماره فقط در سند خصوصی. */
    override suspend fun createProfile(alias: String, phone: String): OpResult<UserProfile> = runOp {
        val id = requireUid()
        val cleanAlias = alias.trim()
        require(cleanAlias.length in 3..20) { "نام مستعار باید بین ۳ تا ۲۰ نویسه باشد." }
        val colorIndex = (cleanAlias.hashCode().let { if (it < 0) -it else it }) % 8
        val publicData = mapOf(
            "uid" to id,
            "alias" to cleanAlias,
            "aliasLower" to cleanAlias.lowercase(),
            "allowMessages" to true,
            "bio" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "colorIndex" to colorIndex,
            "banned" to false,
            "lastSeen" to FieldValue.serverTimestamp()
        )
        users().document(id).set(publicData).await()
        privateDir().document(id).set(
            mapOf("phone" to phone, "updatedAt" to FieldValue.serverTimestamp()),
            SetOptions.merge()
        ).await()
        UserProfile(
            uid = id, alias = cleanAlias, aliasLower = cleanAlias.lowercase(),
            allowMessages = true, createdAt = System.currentTimeMillis(), colorIndex = colorIndex
        )
    }

    override suspend fun updateAlias(alias: String): OpResult<Unit> = runOp {
        val id = requireUid()
        val cleanAlias = alias.trim()
        require(cleanAlias.length in 3..20) { "نام مستعار باید بین ۳ تا ۲۰ نویسه باشد." }
        users().document(id).update(
            mapOf(
                "alias" to cleanAlias,
                "aliasLower" to cleanAlias.lowercase(),
                "colorIndex" to (cleanAlias.hashCode().let { if (it < 0) -it else it }) % 8
            )
        ).await()
        Unit
    }

    override suspend fun setAllowMessages(enabled: Boolean): OpResult<Unit> = runOp {
        users().document(requireUid()).update(mapOf("allowMessages" to enabled)).await()
        Unit
    }

    override suspend fun updateBio(bio: String): OpResult<Unit> = runOp {
        require(bio.length <= 80) { "بیوگرافی حداکثر ۸۰ نویسه است." }
        users().document(requireUid()).update(mapOf("bio" to bio)).await()
        Unit
    }

    suspend fun touchLastSeen() {
        val id = uid ?: return
        runCatching { users().document(id).update(mapOf("lastSeen" to FieldValue.serverTimestamp())).await() }
    }

    // ----------------------------------------------------------------- search
    override suspend fun searchUsers(query: String, limit: Long): OpResult<List<UserProfile>> = runOp {
        val id = requireUid()
        val q = query.trim().lowercase()
        if (q.length < 2) return@runOp emptyList()
        val snap = users()
            .orderBy("aliasLower")
            .whereGreaterThanOrEqualTo("aliasLower", q)
            .whereLessThanOrEqualTo("aliasLower", q + "\uf8ff")
            .limit(limit)
            .get()
            .await()
        snap.documents
            .filter { it.id != id }
            .map { Mapper.user(it) }
            .filter { !it.banned }
    }

    override suspend fun suggestUsers(limit: Long): OpResult<List<UserProfile>> = runOp {
        val id = requireUid()
        val snap = users().orderBy("createdAt", Query.Direction.DESCENDING).limit(30).get().await()
        snap.documents.filter { it.id != id }.map { Mapper.user(it) }.take(limit.toInt())
    }

    // ------------------------------------------------------------------ block
    override suspend fun blockUser(targetUid: String, targetAlias: String): OpResult<Unit> = runOp {
        val id = requireUid()
        require(targetUid != id) { "نمی‌توانید خودتان را مسدود کنید." }
        blocks().document("${id}__$targetUid").set(
            mapOf(
                "blockerUid" to id,
                "blockedUid" to targetUid,
                "blockedAlias" to targetAlias,
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Unit
    }

    override suspend fun unblockUser(targetUid: String): OpResult<Unit> = runOp {
        blocks().document("${requireUid()}__$targetUid").delete().await()
        Unit
    }

    override suspend fun blockedUsers(): OpResult<List<BlockedUser>> = runOp {
        val id = requireUid()
        val snap = blocks().whereEqualTo("blockerUid", id).get().await()
        snap.documents.map { d ->
            BlockedUser(
                uid = d.getString("blockedUid") ?: "",
                alias = d.getString("blockedAlias") ?: "کاربر مسدودشده",
                blockedAt = (d.get("createdAt") as? Timestamp)?.toDate()?.time ?: 0L
            )
        }.sortedByDescending { it.blockedAt }
    }

    suspend fun isBlocked(targetUid: String): Boolean {
        val id = uid ?: return false
        return runCatching {
            blocks().document("${id}__$targetUid").get().await().exists()
        }.getOrDefault(false)
    }

    // ------------------------------------------------------------------- send
    /**
     * ارسال پیام در یک تراکنش: هم‌زمان پیام نوشته می‌شود و شمارنده سقف ارسال
     * به‌روزرسانی می‌شود تا از ارسال انبوه جلوگیری شود.
     */
    override suspend fun sendMessage(
        toUid: String,
        toAlias: String,
        text: String,
        replyTo: Message?
    ): OpResult<Unit> = runOp {
        val id = requireUid()
        val body = text.trim()
        require(body.isNotEmpty()) { "متن پیام خالی است." }
        require(body.length <= MAX_MESSAGE_LENGTH) { "حداکثر طول پیام ۵۰۰ نویسه است." }
        require(toUid != id) { "ارسال پیام به خودتان ممکن نیست." }

        val mySnap = users().document(id).get().await()
        require(mySnap.exists()) { "پروفایل شما یافت نشد. دوباره وارد شوید." }
        val myAlias = mySnap.getString("alias") ?: "ناشناس"

        val targetSnap = users().document(toUid).get().await()
        require(targetSnap.exists()) { "کاربر مورد نظر یافت نشد." }
        val target = Mapper.user(targetSnap)
        require(target.allowMessages) { "این کاربر دریافت پیام را غیرفعال کرده است." }

        val iBlockedThem = blocks().document("${id}__$toUid").get().await().exists()
        require(!iBlockedThem) { "ابتدا این کاربر را از لیست مسدودها خارج کنید." }

        val messageRef = messages().document()
        val rateRef = rateLimits().document(id)
        val now = System.currentTimeMillis()

        db.runTransaction { txn ->
            val rateSnap = txn.get(rateRef)
            val oldStart = (rateSnap.get("windowStart") as? Timestamp)?.toDate()?.time ?: 0L
            val oldCount = rateSnap.getLong("count") ?: 0L
            val fresh = now - oldStart > RATE_LIMIT_WINDOW_MS
            val newCount = if (fresh) 1L else oldCount + 1L
            require(newCount <= RATE_LIMIT_MAX) {
                "محدودیت ارسال: حداکثر ۲۰ پیام در هر دقیقه. کمی صبر کنید."
            }
            txn.set(
                rateRef,
                mapOf("windowStart" to Timestamp(Date(now)), "count" to newCount)
            )
            txn.set(
                messageRef,
                mapOf(
                    "fromUid" to id,
                    "fromAlias" to myAlias,
                    "toUid" to toUid,
                    "toAlias" to toAlias,
                    "text" to body,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "readAt" to 0L,
                    "threadId" to threadIdOf(id, toUid),
                    "replyToId" to (replyTo?.id ?: ""),
                    "replyToSnippet" to (replyTo?.text?.take(80) ?: ""),
                    "deletedForFrom" to false,
                    "deletedForTo" to false
                )
            )
        }.await()
        Unit
    }

    // ------------------------------------------------------------------ inbox
    fun inboxQuery() = messages()
        .whereEqualTo("toUid", requireUid())
        .limit(200)

    fun outboxQuery() = messages()
        .whereEqualTo("fromUid", requireUid())
        .limit(200)

    override suspend fun fetchInbox(): OpResult<List<Message>> = runOp {
        inboxQuery().get().await().documents.map { Mapper.message(it) }.sortedByDescending { it.createdAt }
    }

    override suspend fun fetchOutbox(): OpResult<List<Message>> = runOp {
        outboxQuery().get().await().documents.map { Mapper.message(it) }.sortedByDescending { it.createdAt }
    }

    override suspend fun markRead(messageId: String): OpResult<Unit> = runOp {
        messages().document(messageId).update(
            mapOf("readAt" to System.currentTimeMillis())
        ).await()
        Unit
    }

    override suspend fun deleteForMe(message: Message): OpResult<Unit> = runOp {
        val id = requireUid()
        val data = if (message.toUid == id) {
            mapOf("deletedForTo" to true)
        } else {
            mapOf("deletedForFrom" to true)
        }
        messages().document(message.id).update(data).await()
        Unit
    }

    override suspend fun threadWith(otherUid: String): OpResult<List<Message>> = runOp {
        val id = requireUid()
        val sentQ = messages().whereEqualTo("fromUid", id)
            .whereEqualTo("toUid", otherUid).limit(200).get().await()
        val receivedQ = messages().whereEqualTo("fromUid", otherUid)
            .whereEqualTo("toUid", id).limit(200).get().await()
        val all = mutableListOf<Message>()
        sentQ.documents.forEach { all += Mapper.message(it) }
        receivedQ.documents.forEach { all += Mapper.message(it) }
        all.sortedBy { it.createdAt }
    }

    // ---------------------------------------------------------------- reports
    override suspend fun report(
        targetUid: String,
        targetAlias: String,
        messageId: String,
        reason: String,
        note: String
    ): OpResult<Unit> = runOp {
        val id = requireUid()
        require(targetUid.isNotBlank()) { "کاربر گزارش‌شده مشخص نیست." }
        require(reason.isNotBlank()) { "دلیل گزارش را انتخاب کنید." }
        require(note.length <= 400) { "توضیحات حداکثر ۴۰۰ نویسه است." }
        reports().add(
            mapOf(
                "reporterUid" to id,
                "targetUid" to targetUid,
                "targetAlias" to targetAlias,
                "messageId" to messageId,
                "reason" to reason,
                "note" to note,
                "status" to "open",
                "createdAt" to FieldValue.serverTimestamp()
            )
        ).await()
        Unit
    }

    /** گزارش + مسدودسازی هم‌زمان (پیشنهاد پیش‌فرض پس از گزارش). */
    override suspend fun reportAndBlock(
        targetUid: String,
        targetAlias: String,
        messageId: String,
        reason: String,
        note: String
    ): OpResult<Unit> = runOp {
        val r = report(targetUid, targetAlias, messageId, reason, note)
        if (r is OpResult.Err) throw IllegalStateException(r.message)
        val b = blockUser(targetUid, targetAlias)
        if (b is OpResult.Err) throw IllegalStateException(b.message)
        Unit
    }

    // --------------------------------------------------------- delete account
    /**
     * حذف حساب: پروفایل عمومی، داده خصوصی و بلوک‌ها حذف می‌شوند و پیام‌ها
     * برای طرف مقابل به‌صورت «حذف‌شده برای فرستنده/گیرنده» علامت می‌خورند.
     * حذف کامل پیام‌ها و حذف سند احراز هویت نیازمند Cloud Functions است.
     */
    override suspend fun deleteMyAccount(): OpResult<Unit> = runOp {
        val id = requireUid()
        val inbox = messages().whereEqualTo("toUid", id).limit(200).get().await()
        val outbox = messages().whereEqualTo("fromUid", id).limit(200).get().await()
        db.runBatch { batch ->
            inbox.documents.forEach { batch.update(it.reference, "deletedForTo", true) }
            outbox.documents.forEach { batch.update(it.reference, "deletedForFrom", true) }
        }.await()
        blocks().whereEqualTo("blockerUid", id).limit(200).get().await().documents.forEach {
            runCatching { it.reference.delete().await() }
        }
        privateDir().document(id).delete().await()
        users().document(id).delete().await()
        Unit
    }

    override suspend fun quotaState(): OpResult<Pair<Int, Long>> = runOp {
        val id = requireUid()
        val snap = rateLimits().document(id).get().await()
        val count = (snap.getLong("count") ?: 0L).toInt()
        val start = (snap.get("windowStart") as? Timestamp)?.toDate()?.time ?: 0L
        val remaining = if (System.currentTimeMillis() - start > RATE_LIMIT_WINDOW_MS) RATE_LIMIT_MAX
        else (RATE_LIMIT_MAX - count).coerceAtLeast(0)
        remaining to start
    }

    override fun currentUid(): String? = uid
    override fun signOut() = auth.signOut()
    override fun isSignedIn(): Boolean = auth.currentUser != null

    suspend fun lookupSnapshot(path: String): DocumentSnapshot? =
        runCatching { db.document(path).get().await() }.getOrNull()
}
