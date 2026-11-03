package com.payamaknashnas.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.data.UserProfile
import com.payamaknashnas.app.ui.theme.Lux

/** پس‌زمینه اصلی: گرادیان تیره + هاله طلایی ملایم. */
@Composable
fun LuxBackground(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Lux.Ink, Lux.InkSoft, Color(0xFF080B10))))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width * 0.78f, size.height * 0.06f)
            val radius = size.width * 0.85f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Lux.GoldGhost, Color.Transparent),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
        }
        content()
    }
}

/** قالب صفحه با نوار بالای اختصاصی (بدون ActionBar پیش‌فرض اندروید). */
@Composable
fun LuxScreen(
    title: String? = null,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    LuxBackground {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            if (title != null || onBack != null) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "بازگشت",
                                tint = Lux.Gold
                            )
                        }
                    }
                    Column(Modifier.weight(1f)) {
                        if (title != null) {
                            Text(
                                title,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Right
                            )
                        }
                        if (subtitle != null) {
                            Text(
                                subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    if (actions != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) { actions() }
                    }
                }
            }
            content()
        }
    }
}

// --------------------------------------------------------------------- buttons
@Composable
fun GoldButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (enabled && !loading) Brush.horizontalGradient(listOf(Lux.Gold, Color(0xFFE6C98F)))
                else Brush.horizontalGradient(listOf(Lux.SurfaceHigh, Lux.SurfaceHigh))
            )
            .clickable(enabled = enabled && !loading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Lux.GoldDeep,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                color = if (enabled) Color(0xFF1A1305) else Lux.TextMuted
            )
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = Lux.Gold,
    onClick: () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, if (enabled) tint.copy(alpha = 0.45f) else Lux.Stroke, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) tint else Lux.TextMuted
        )
    }
}

// ----------------------------------------------------------------------- cards
@Composable
fun LuxCard(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Lux.Surface.copy(alpha = 0.92f))
            .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(22.dp))
            .padding(padding)
    ) { content() }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = Lux.Gold.copy(alpha = 0.8f),
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

// ---------------------------------------------------------------------- inputs
@Composable
fun LuxField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = Lux.TextMuted
            )
        },
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            textDirection = TextDirection.Rtl,
            textAlign = TextAlign.Right
        ),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        trailingIcon = trailing,
        leadingIcon = leading,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Lux.Gold.copy(alpha = 0.75f),
            unfocusedBorderColor = Lux.Stroke,
            focusedContainerColor = Lux.Surface.copy(alpha = 0.7f),
            unfocusedContainerColor = Lux.Surface.copy(alpha = 0.45f),
            cursorColor = Lux.Gold,
            focusedTextColor = Lux.TextPrimary,
            unfocusedTextColor = Lux.TextPrimary
        )
    )
}

// --------------------------------------------------------------------- avatars
private val avatarGradients = listOf(
    listOf(Color(0xFFD4AF6A), Color(0xFF8C6B2A)),
    listOf(Color(0xFF6A8FD4), Color(0xFF2A3F8C)),
    listOf(Color(0xFF8C6AD4), Color(0xFF5B2A8C)),
    listOf(Color(0xFF6AD4A8), Color(0xFF2A8C6B)),
    listOf(Color(0xFFD46A8F), Color(0xFF8C2A4B)),
    listOf(Color(0xFFD4A06A), Color(0xFF8C5B2A)),
    listOf(Color(0xFF6AC4D4), Color(0xFF2A6B8C)),
    listOf(Color(0xFFB0B7C3), Color(0xFF4A5563))
)

@Composable
fun Avatar(
    alias: String,
    colorIndex: Int,
    size: Dp = 48.dp,
    showOnline: Boolean = false
) {
    val palette = avatarGradients[(colorIndex % avatarGradients.size + avatarGradients.size) % avatarGradients.size]
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = alias.trim().take(1).ifBlank { "؟" },
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (size.value / 2.6f).sp
            ),
            color = Color(0xFF0B0D12)
        )
        if (showOnline) {
            Box(
                Modifier
                    .size(size / 3.6f)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-2).dp, y = (-2).dp)
                    .clip(CircleShape)
                    .background(Lux.Success)
                    .border(2.dp, Lux.Ink, CircleShape)
            )
        }
    }
}

@Composable
fun UserRow(
    profile: UserProfile,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = rememberInteraction(),
                indication = null
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(profile.alias, profile.colorIndex)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(profile.alias, style = MaterialTheme.typography.titleMedium)
            Text(
                when {
                    !profile.allowMessages -> "دریافت پیام بسته است"
                    profile.bio.isNotBlank() -> profile.bio
                    else -> "عضو پیامک ناشناس"
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
        if (trailing != null) trailing()
    }
}

@Composable
private fun rememberInteraction(): MutableInteractionSource =
    androidx.compose.runtime.remember { MutableInteractionSource() }

// ------------------------------------------------------------------ indicators
@Composable
fun MessageBadge(count: Int) {
    if (count <= 0) return
    Box(
        Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(Lux.Gold),
        contentAlignment = Alignment.Center
    ) {
        Text(
            com.payamaknashnas.app.data.PersianDate.toPersianDigits(count.toString()),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = Color(0xFF1A1305)
        )
    }
}

@Composable
fun NoticeBar(message: String?, isError: Boolean = true) {
    AnimatedVisibility(visible = !message.isNullOrBlank(), enter = fadeIn(tween(180)), exit = fadeOut(tween(120))) {
        val bg = if (message != null && (isError)) Lux.DangerGhost else Lux.GoldGhost
        val fg = if (message != null && isError) Lux.Danger else Lux.Gold
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .border(1.dp, fg.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                message ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = fg,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 64.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(Lux.GoldGhost),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Lux.Gold, modifier = Modifier.size(36.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 18.dp)
        )
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        if (action != null) Spacer(Modifier.height(18.dp))
        action?.invoke()
    }
}

@Composable
fun LoadingBlock(text: String = "در حال بارگذاری…") {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Lux.Gold, strokeWidth = 2.dp, modifier = Modifier.size(30.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

@Composable
fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    switchColors: androidx.compose.material3.SwitchColors = androidx.compose.material3.SwitchDefaults.colors(
        checkedThumbColor = Color(0xFF1A1305),
        checkedTrackColor = Lux.Gold,
        uncheckedThumbColor = Lux.TextMuted,
        uncheckedTrackColor = Lux.SurfaceHigh,
        uncheckedBorderColor = Lux.Stroke
    ),
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))
        }
        androidx.compose.material3.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = switchColors
        )
    }
}

@Composable
fun SettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    tint: Color = Lux.Gold,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 1.dp))
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = Lux.TextMuted,
            modifier = Modifier
                .size(16.dp)
                .androidxRotate()
        )
    }
}

/** چرخش ۱۸۰ درجه فلش بازگشت تا در چیدمان راست‌چین به سمت داخل اشاره کند. */
private fun Modifier.androidxRotate(): Modifier = this.rotate(180f)

/** نوار ناوبری پایین با چهار مقصد اصلی. */
@Composable
fun LuxBottomBar(
    items: List<Triple<String, ImageVector, String>>,
    selectedRoute: String,
    unread: Int,
    onSelect: (String) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Lux.Surface.copy(alpha = 0.96f))
            .border(1.dp, Lux.StrokeSoft, RoundedCornerShape(26.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (route, icon, label) ->
            val selected = route == selectedRoute
            val scale by animateFloatAsState(if (selected) 1f else 0.94f, tween(180), label = "scale")
            Column(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) Lux.GoldGhost else Color.Transparent)
                    .clickable { onSelect(route) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Icon(
                        icon,
                        contentDescription = label,
                        tint = if (selected) Lux.Gold else Lux.TextMuted,
                        modifier = Modifier.size((21 * scale).dp)
                    )
                    if (route == "inbox" && unread > 0) {
                        Box(
                            Modifier
                                .size(8.dp)
                                .offset(x = 9.dp, y = (-2).dp)
                                .clip(CircleShape)
                                .background(Lux.Danger)
                        )
                    }
                }
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = if (selected) Lux.Gold else Lux.TextMuted,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}

@Composable
fun ContentListPadding(): PaddingValues = PaddingValues(bottom = 24.dp)

@Composable
fun DividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Lux.StrokeSoft)
    )
}
