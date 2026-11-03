package com.payamaknashnas.app.data

import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

/**
 * پیاده‌سازی نمایشی (بدون Firebase) برای بررسی ظاهر و جریان صفحات.
 * داده‌ها فقط در حافظه است و با بستن برنامه پاک می‌شود.
 * این حالت هرگز در نسخه نهایی منتشرشده استفاده نمی‌شود؛ در صورت وجود
 * google-services.json واقعی، FirestoreRepo جای آن را می‌گیرد.
 */
class DemoRepo : Repo {

    var pendingPhone: String = ""

    private var signedIn = false
    private var me: UserProfile? = null
    private val directory = mutableListOf<UserProfile>()
    private val messages = mutableListOf<Message>()
    private val blocked = mutableListOf<BlockedUser>()
    private var allowMessages = true
    private var rateWindowStart = 0L
    private var rateCount = 0
    private var counter = 0

    private val demoUid = "demo-me"

    private val seedAliases = listOf(
        "شب‌گرد", "آوای باران", "مسافر خاموش", "نیمه‌شب", "پرنده مهاجر",
        "راز سرخ", "کوچه باغ", "ابر نقره‌ای", "ستاره دنباله‌دار", "نغمه"
    )

    init {
        seedAliases.forEachIndexed { i, alias ->
            directory += UserProfile(
                uid = "demo-u$i",
                alias = alias,
                aliasLower = alias.lowercase(),
                allowMessages = i != 3,
                bio = if (i % 3 == 0) "اینجا برای شنیدن، نه قضاوت." else "",
                createdAt = System.currentTimeMillis() - i * 86_400_000L,
                colorIndex = i % 8
            )
        }
    }

    fun signInDemo() {
        signedIn = true
        if (me == null) {
            me = UserProfile(
                uid = demoUid, alias = "مهمان ناشناس", aliasLower = "مهمان ناشناس",
                allowMessages = true, createdAt = System.currentTimeMillis(), colorIndex = 2
            )
            directory += me!!
        }
        if (messages.isEmpty()) {
            val now = System.currentTimeMillis()
            messages += Message(
                id = "m1", fromUid = "demo-u0", fromAlias = "شب‌گرد",
                toUid = demoUid, toAlias = "مهمان ناشناس",
                text = "سلام. نمی‌دانم کسی هست که این را بخواند یا نه، اما دلم می‌خواست بگویم صدای باران امشب شبیه خاطره بود.",
                createdAt = now - 3_600_000L, readAt = 0,
                threadId = threadIdOf("demo-u0", demoUid)
            )
            messages += Message(
                id = "m2", fromUid = "demo-u1", fromAlias = "آوای باران",
                toUid = demoUid, toAlias = "مهمان ناشناس",
                text = "یک سؤال بی‌پرده: اگر هیچ‌کس نمی‌فهمید چه کسی هستی، چه می‌گفتی؟",
                createdAt = now - 26 * 3_600_000L, readAt = now - 25 * 3_600_000L,
                threadId = threadIdOf("demo-u1", demoUid)
            )
            messages += Message(
                id = "m3", fromUid = demoUid, fromAlias = "مهمان ناشناس",
                toUid = "demo-u2", toAlias = "مسافر خاموش",
                text = "سلام مسافر. مسیرت به کجا ختم می‌شود؟",
                createdAt = now - 7_200_000L, readAt = now - 6_000_000L,
                threadId = threadIdOf(demoUid, "demo-u2")
            )
        }
    }

    private fun requireMe(): UserProfile =
        me ?: throw IllegalStateException("برای ادامه وارد شوید.")

    override fun currentUid(): String? = if (signedIn) demoUid else null
    override fun isSignedIn(): Boolean = signedIn

    override fun signOut() {
        signedIn = false
    }

    private suspend fun tick() = delay(220)

    override suspend fun fetchMyProfile(): OpResult<UserProfile> {
        tick(); return OpResult.Ok(requireMe())
    }

    override suspend fun fetchProfile(userId: String): OpResult<UserProfile> {
        tick()
        val u = directory.firstOrNull { it.uid == userId }
            ?: return OpResult.Err("کاربر یافت نشد.")
        return OpResult.Ok(u)
    }

    override suspend fun createProfile(alias: String, phone: String): OpResult<UserProfile> {
        tick()
        val clean = alias.trim()
        if (clean.length !in 3..20) return OpResult.Err("نام مستعار باید بین ۳ تا ۲۰ نویسه باشد.")
        val profile = UserProfile(
            uid = demoUid, alias = clean, aliasLower = clean.lowercase(),
            allowMessages = true, createdAt = System.currentTimeMillis(),
            colorIndex = clean.hashCode().absoluteValue % 8
        )
        me = profile
        directory.removeAll { it.uid == demoUid }
        directory.add(0, profile)
        return OpResult.Ok(profile)
    }

    override suspend fun updateAlias(alias: String): OpResult<Unit> {
        tick()
        val clean = alias.trim()
        if (clean.length !in 3..20) return OpResult.Err("نام مستعار باید بین ۳ تا ۲۰ نویسه باشد.")
        me = requireMe().copy(alias = clean, aliasLower = clean.lowercase())
        val idx = directory.indexOfFirst { it.uid == demoUid }
        if (idx >= 0) directory[idx] = me!!
        return OpResult.Ok(Unit)
    }

    override suspend fun updateBio(bio: String): OpResult<Unit> {
        tick(); me = requireMe().copy(bio = bio); return OpResult.Ok(Unit)
    }

    override suspend fun setAllowMessages(enabled: Boolean): OpResult<Unit> {
        tick(); allowMessages = enabled; return OpResult.Ok(Unit)
    }

    override suspend fun searchUsers(query: String, limit: Long): OpResult<List<UserProfile>> {
        tick()
        val q = query.trim().lowercase()
        if (q.length < 2) return OpResult.Ok(emptyList())
        return OpResult.Ok(directory.filter { it.uid != demoUid && it.aliasLower.contains(q) }.take(limit.toInt()))
    }

    override suspend fun suggestUsers(limit: Long): OpResult<List<UserProfile>> {
        tick()
        return OpResult.Ok(directory.filter { it.uid != demoUid }.take(limit.toInt()))
    }

    override suspend fun blockUser(targetUid: String, targetAlias: String): OpResult<Unit> {
        tick()
        if (blocked.none { it.uid == targetUid }) {
            blocked += BlockedUser(targetUid, targetAlias, System.currentTimeMillis())
        }
        return OpResult.Ok(Unit)
    }

    override suspend fun unblockUser(targetUid: String): OpResult<Unit> {
        tick(); blocked.removeAll { it.uid == targetUid }; return OpResult.Ok(Unit)
    }

    override suspend fun blockedUsers(): OpResult<List<BlockedUser>> {
        tick(); return OpResult.Ok(blocked.toList())
    }

    override suspend fun sendMessage(
        toUid: String, toAlias: String, text: String, replyTo: Message?
    ): OpResult<Unit> {
        tick()
        val body = text.trim()
        if (body.isEmpty()) return OpResult.Err("متن پیام خالی است.")
        if (body.length > MAX_MESSAGE_LENGTH) return OpResult.Err("حداکثر طول پیام ۵۰۰ نویسه است.")
        val target = directory.firstOrNull { it.uid == toUid }
            ?: return OpResult.Err("کاربر مورد نظر یافت نشد.")
        if (!target.allowMessages) return OpResult.Err("این کاربر دریافت پیام را غیرفعال کرده است.")
        if (blocked.any { it.uid == toUid }) return OpResult.Err("ابتدا این کاربر را از لیست مسدودها خارج کنید.")

        val now = System.currentTimeMillis()
        if (now - rateWindowStart > RATE_LIMIT_WINDOW_MS) {
            rateWindowStart = now; rateCount = 0
        }
        rateCount++
        if (rateCount > RATE_LIMIT_MAX) return OpResult.Err("محدودیت ارسال: حداکثر ۲۰ پیام در هر دقیقه.")

        counter++
        messages += Message(
            id = "demo-s$counter", fromUid = demoUid, fromAlias = requireMe().alias,
            toUid = toUid, toAlias = toAlias, text = body, createdAt = now, readAt = 0,
            threadId = threadIdOf(demoUid, toUid),
            replyToId = replyTo?.id ?: "", replyToSnippet = replyTo?.text?.take(80) ?: ""
        )
        return OpResult.Ok(Unit)
    }

    override suspend fun fetchInbox(): OpResult<List<Message>> {
        tick()
        return OpResult.Ok(messages.filter { it.toUid == demoUid && !it.deletedForTo }.sortedByDescending { it.createdAt })
    }

    override suspend fun fetchOutbox(): OpResult<List<Message>> {
        tick()
        return OpResult.Ok(messages.filter { it.fromUid == demoUid && !it.deletedForFrom }.sortedByDescending { it.createdAt })
    }

    override suspend fun markRead(messageId: String): OpResult<Unit> {
        val i = messages.indexOfFirst { it.id == messageId }
        if (i >= 0) messages[i] = messages[i].copy(readAt = System.currentTimeMillis())
        return OpResult.Ok(Unit)
    }

    override suspend fun deleteForMe(message: Message): OpResult<Unit> {
        val i = messages.indexOfFirst { it.id == message.id }
        if (i >= 0) {
            messages[i] = if (messages[i].toUid == demoUid)
                messages[i].copy(deletedForTo = true) else messages[i].copy(deletedForFrom = true)
        }
        return OpResult.Ok(Unit)
    }

    override suspend fun threadWith(otherUid: String): OpResult<List<Message>> {
        tick()
        return OpResult.Ok(
            messages.filter {
                (it.fromUid == demoUid && it.toUid == otherUid) ||
                    (it.toUid == demoUid && it.fromUid == otherUid)
            }.sortedBy { it.createdAt }
        )
    }

    override suspend fun report(
        targetUid: String, targetAlias: String, messageId: String, reason: String, note: String
    ): OpResult<Unit> {
        tick(); return OpResult.Ok(Unit)
    }

    override suspend fun reportAndBlock(
        targetUid: String, targetAlias: String, messageId: String, reason: String, note: String
    ): OpResult<Unit> = blockUser(targetUid, targetAlias)

    override suspend fun deleteMyAccount(): OpResult<Unit> {
        tick()
        me = null; signedIn = false
        messages.removeAll { it.fromUid == demoUid || it.toUid == demoUid }
        blocked.clear()
        return OpResult.Ok(Unit)
    }

    override suspend fun quotaState(): OpResult<Pair<Int, Long>> {
        val remaining = if (System.currentTimeMillis() - rateWindowStart > RATE_LIMIT_WINDOW_MS)
            RATE_LIMIT_MAX else (RATE_LIMIT_MAX - rateCount).coerceAtLeast(0)
        return OpResult.Ok(remaining to rateWindowStart)
    }
}
