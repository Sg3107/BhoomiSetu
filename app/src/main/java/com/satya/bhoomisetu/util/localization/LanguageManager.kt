package com.satya.bhoomisetu.util.localization

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.satya.bhoomisetu.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "bhoomisetu_language_prefs")

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    init {
        scope.launch {
            context.languageDataStore.data.map { prefs ->
                val code = prefs[KEY_APP_LANGUAGE] ?: AppLanguage.ENGLISH.code
                AppLanguage.entries.find { it.code == code } ?: AppLanguage.ENGLISH
            }.collectLatest { lang ->
                _language.value = lang
            }
        }
    }

    suspend fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        context.languageDataStore.edit { prefs ->
            prefs[KEY_APP_LANGUAGE] = lang.code
        }
    }

    suspend fun toggleLanguage() {
        val next = if (_language.value == AppLanguage.ENGLISH) AppLanguage.TELUGU else AppLanguage.ENGLISH
        setLanguage(next)
    }
}
