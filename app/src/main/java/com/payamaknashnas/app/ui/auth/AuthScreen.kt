package com.payamaknashnas.app.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.data.AppContainer
import com.payamaknashnas.app.data.OpResult
import com.payamaknashnas.app.data.PersianDate
import com.payamaknashnas.app.data.UserProfile
import com.payamaknashnas.app.ui.AppState
import com.payamaknashnas.app.ui.components.GhostButton
import com.payamaknashnas.app.ui.components.GoldButton
import com.payamaknashnas.app.ui.components.LuxBackground
import com.payamaknashnas.app.ui.components.LuxCard
import com.payamaknashnas.app.ui.components.LuxField
import com.payamaknashnas.app.ui.components.NoticeBar
import com.payamaknashnas.app.ui.theme.Lux
import kotlinx.coroutines.launch

private enum class Step { PHONE, CODE, ALIAS }

@Composable
fun AuthScreen(app: AppState, onDone: (UserProfile) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(Step.PHONE) }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    fun e164(input: String): String {
        val digits = PersianDate.toEnglishDigits(input).filter { it.isDigit() }
        return when {
            input.trim().startsWith("+") -> "+" + input.filter { it.isDigit() }
            input.startsWith("0098") -> "+" + input.drop(4).trimStart('0')
            input.startsWith("98") && input.length > 10 -> "+$input"
            digits.startsWith("0") -> "+98" + digits.drop(1)
            digits.startsWith("9") -> "+98$digits"
            else -> "+98$digits"
        }
    }

    LuxBackground {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))
            Box(
                Modifier
                    .size(74.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Lux.GoldGhost),
                contentAlignment = Alignment.Center
            ) {
                Text("؟", style = MaterialTheme.typography.displaySmall.copy(fontSize = 32.sp), color = Lux.Gold)
            }
            Text(
                "پیامک ناشناس",
                style = MaterialTheme.typography.displaySmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                "پیام بفرست، بدون آن‌که هویتت دیده شود.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 26.dp)
            )

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(120)) },
                label = "authStep"
            ) { current ->
                when (current) {
                    Step.PHONE -> Column(Modifier.fillMaxWidth()) {
                        LuxCard {
                            Column {
                                Text("ورود یا ثبت‌نام", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "شماره موبایل شما فقط برای احراز هویت استفاده می‌شود و هرگز برای کاربران دیگر نمایش داده نمی‌شود.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                                )
                                LuxField(
                                    value = phone,
                                    onValueChange = { if (it.length <= 13) phone = it },
                                    placeholder = "۰۹۱۲۱۲۳۴۵۶۷",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                )
                                NoticeBar(error)
                                Spacer(Modifier.height(14.dp))
                                GoldButton(
                                    text = "دریافت کد تأیید",
                                    loading = busy,
                                    enabled = PersianDate.toEnglishDigits(phone).count { it.isDigit() } >= 10
                                ) {
                                    val activity = context as? Activity
                                    if (activity == null) {
                                        error = "خطای محیط اجرا."
                                        return@GoldButton
                                    }
                                    busy = true; error = null
                                    scope.launch {
                                        val r = app.auth.sendCode(activity, e164(phone))
                                        busy = false
                                        when (r) {
                                            is OpResult.Ok -> {
                                                step = Step.CODE
                                                info = "کد تأیید به شماره شما پیامک شد."
                                            }
                                            is OpResult.Err -> error = r.message
                                        }
                                    }
                                }
                            }
                        }
                        if (AppContainer.demoMode) {
                            Spacer(Modifier.height(14.dp))
                            DemoHint("حالت نمایشی: بدون Firebase. کد تأیید ۱۲۳۴۵۶ است.")
                        }
                    }

                    Step.CODE -> Column(Modifier.fillMaxWidth()) {
                        LuxCard {
                            Column {
                                Text("کد تأیید", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "کد شش‌رقمی ارسال‌شده به ${PersianDate.toPersianDigits(phone)} را وارد کنید.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                                )
                                LuxField(
                                    value = code,
                                    onValueChange = { v ->
                                        val d = PersianDate.toEnglishDigits(v).filter { it.isDigit() }
                                        if (d.length <= 6) code = d
                                    },
                                    placeholder = "۱۲۳۴۵۶",
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword)
                                )
                                if (info != null) {
                                    Text(
                                        info ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Lux.Success,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                NoticeBar(error)
                                Spacer(Modifier.height(14.dp))
                                GoldButton(
                                    text = "تأیید و ادامه",
                                    loading = busy,
                                    enabled = code.length == 6
                                ) {
                                    busy = true; error = null
                                    scope.launch {
                                        val r = app.auth.verifyCode(e164(phone), code)
                                        busy = false
                                        when (r) {
                                            is OpResult.Ok -> {
                                                val existing = app.repo.fetchMyProfile()
                                                if (existing is OpResult.Ok) {
                                                    onDone(existing.value)
                                                } else {
                                                    step = Step.ALIAS
                                                }
                                            }
                                            is OpResult.Err -> error = r.message
                                        }
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                GhostButton("تغییر شماره") {
                                    error = null; code = ""; step = Step.PHONE
                                }
                            }
                        }
                    }

                    Step.ALIAS -> Column(Modifier.fillMaxWidth()) {
                        LuxCard {
                            Column {
                                Text("یک نام مستعار انتخاب کن", style = MaterialTheme.typography.titleLarge)
                                Text(
                                    "این نام به‌جای شماره و هویت واقعی شما به دیگران نمایش داده می‌شود. بعداً هم می‌توانید تغییرش دهید.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)
                                )
                                LuxField(
                                    value = alias,
                                    onValueChange = { if (it.length <= 20) alias = it },
                                    placeholder = "مثلاً: مسافر خاموش"
                                )
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("۳ تا ۲۰ نویسه", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        PersianDate.toPersianDigits(alias.length.toString()) + " / ۲۰",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                NoticeBar(error)
                                Spacer(Modifier.height(14.dp))
                                GoldButton(
                                    text = "ورود به برنامه",
                                    loading = busy,
                                    enabled = alias.trim().length >= 3
                                ) {
                                    busy = true; error = null
                                    scope.launch {
                                        val r = app.repo.createProfile(alias, e164(phone))
                                        busy = false
                                        when (r) {
                                            is OpResult.Ok -> onDone(r.value)
                                            is OpResult.Err -> error = r.message
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "با ورود، می‌پذیرید که ارسال پیام توهین‌آمیز، تهدید یا مزاحمت ممنوع است و گزارش تخلف بررسی می‌شود.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 30.dp)
            )
        }
    }
}

@Composable
fun DemoHint(text: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Lux.GoldGhost)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = Lux.Gold
        )
    }
}
