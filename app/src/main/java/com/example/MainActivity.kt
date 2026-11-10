package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.LocalThemeModeController
import com.example.ui.theme.MyApplicationTheme
import com.example.util.configureAppCheck

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        // تهيئة قنوات الإشعارات الذكية في شريط إشعارات الجوال
        com.example.util.SmartNotificationManager.initChannels(this)

        enableEdgeToEdge()
        setContent {
            var themeMode by remember { mutableStateOf(AppThemeMode.SYSTEM) }

            MyApplicationTheme(themeMode = themeMode) {
                CompositionLocalProvider(
                    LocalThemeMode provides themeMode,
                    LocalThemeModeController provides { themeMode = it }
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
