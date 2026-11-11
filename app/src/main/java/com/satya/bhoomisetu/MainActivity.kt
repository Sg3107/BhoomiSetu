package com.satya.bhoomisetu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.satya.bhoomisetu.presentation.navigation.AppNavGraph
import com.satya.bhoomisetu.presentation.theme.BhoomiSetuTheme
import com.satya.bhoomisetu.util.localization.LanguageManager
import com.satya.bhoomisetu.util.localization.LocalAppLanguage
import com.satya.bhoomisetu.util.localization.LocalAppStrings
import com.satya.bhoomisetu.util.localization.getStringsForLanguage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class  MainActivity : ComponentActivity() {

    @Inject
    lateinit var languageManager: LanguageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentLang by languageManager.language.collectAsState()
            val strings = remember(currentLang) { getStringsForLanguage(currentLang) }

            CompositionLocalProvider(
                LocalAppStrings provides strings,
                LocalAppLanguage provides currentLang
            ) {
                BhoomiSetuTheme {
                    AppNavGraph(languageManager = languageManager)
                }
            }
        }
    }
}
