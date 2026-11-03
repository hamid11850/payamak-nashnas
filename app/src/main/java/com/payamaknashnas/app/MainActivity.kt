package com.payamaknashnas.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.payamaknashnas.app.ui.RootScreen
import com.payamaknashnas.app.ui.theme.PayamakTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PayamakTheme {
                // کل رابط کاربری راست‌چین است.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    RootScreen()
                }
            }
        }
    }
}
