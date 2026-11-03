package com.payamaknashnas.app.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payamaknashnas.app.ui.auth.AuthScreen
import com.payamaknashnas.app.ui.main.MainShell
import com.payamaknashnas.app.ui.theme.Lux

@Composable
fun RootScreen() {
    val app = rememberAppState()
    app // keep state alive

    AnimatedContent(
        targetState = when {
            app.booting -> "boot"
            app.me == null -> "auth"
            else -> "main"
        },
        transitionSpec = { fadeIn(tween(320)) togetherWith fadeOut(tween(180)) },
        label = "root"
    ) { stage ->
        when (stage) {
            "boot" -> SplashScreen()
            "auth" -> AuthScreen(app, onDone = { app.setProfile(it) })
            else -> MainShell(app, onSignOut = { app.signOutAndReset() })
        }
    }
}

@Composable
fun SplashScreen() {
    Box(Modifier.fillMaxSize().background(Lux.Ink), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(Lux.GoldGhost),
                contentAlignment = Alignment.Center
            ) {
                Text("؟", style = MaterialTheme.typography.displaySmall.copy(fontSize = 34.sp), color = Lux.Gold)
            }
            Text(
                "پیامک ناشناس",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 18.dp)
            )
            Text(
                "در حال آماده‌سازی…",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
