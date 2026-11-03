package com.payamaknashnas.app.data

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** لایه احراز هویت: ارسال کد پیامکی و تأیید آن. */
interface AuthApi {
    suspend fun sendCode(activity: Activity, phoneE164: String): OpResult<Unit>
    suspend fun verifyCode(phone: String, code: String): OpResult<Unit>
    fun currentUid(): String?
    fun signOut()
    fun isSignedIn(): Boolean
}

class FirebaseAuthApi(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthApi {

    private var verificationId: String? = null

    override suspend fun sendCode(activity: Activity, phoneE164: String): OpResult<Unit> = try {
        val id = suspendCancellableCoroutine<String> { cont ->
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // تأیید خودکار (Auto-retrieval) روی برخی دستگاه‌ها
                    auth.signInWithCredential(credential)
                    if (cont.isActive) cont.resume(verificationId ?: "")
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    if (cont.isActive) cont.resumeWithException(e)
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    if (cont.isActive) cont.resume(id)
                }
            }
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneE164)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
        if (id.isNotBlank()) verificationId = id
        OpResult.Ok(Unit)
    } catch (t: Throwable) {
        OpResult.Err(FaErrors.humanize(t))
    }

    override suspend fun verifyCode(phone: String, code: String): OpResult<Unit> {
        val vid = verificationId
            ?: return OpResult.Err("جلسه تأیید منقضی شده است. کد را دوباره درخواست کنید.")
        return try {
            val credential = PhoneAuthProvider.getCredential(vid, code)
            suspendCancellableCoroutine<Unit> { cont ->
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                    .addOnFailureListener {
                        verificationId = null
                        if (cont.isActive) cont.resumeWithException(it)
                    }
            }
            OpResult.Ok(Unit)
        } catch (t: Throwable) {
            OpResult.Err(FaErrors.humanize(t))
        }
    }

    override fun currentUid(): String? = auth.currentUser?.uid
    override fun signOut() = auth.signOut()
    override fun isSignedIn(): Boolean = auth.currentUser != null
}

/** احراز هویت نمایشی: کد ثابت ۱۲۳۴۵۶ — فقط برای تست ظاهر برنامه بدون Firebase. */
class DemoAuthApi(private val demo: DemoRepo) : AuthApi {
    override suspend fun sendCode(activity: Activity, phoneE164: String): OpResult<Unit> {
        demo.pendingPhone = phoneE164
        return OpResult.Ok(Unit)
    }

    override suspend fun verifyCode(phone: String, code: String): OpResult<Unit> =
        if (code == "123456") {
            demo.signInDemo()
            OpResult.Ok(Unit)
        } else {
            OpResult.Err("در حالت نمایشی کد تأیید ۱۲۳۴۵۶ است.")
        }

    override fun currentUid(): String? = demo.currentUid()
    override fun signOut() = demo.signOut()
    override fun isSignedIn(): Boolean = demo.isSignedIn()
}
