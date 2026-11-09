package com.paan.safa.core.localization

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.LocaleList
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private const val PREFS_NAME = "language_settings"
private const val KEY_LANGUAGE = "language"
private const val KEY_CHOSEN = "has_chosen_language"

enum class AppLanguage(val code: String) {
    English("en"), Bengali("bn"), Odia("or");

    companion object {
        fun fromCode(code: String?) = entries.firstOrNull { it.code == code } ?: English
    }
}

@Singleton
class LanguageStore @Inject constructor(@ApplicationContext context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _current = MutableStateFlow(
        AppLanguage.fromCode(preferences.getString(KEY_LANGUAGE, AppLanguage.English.code))
    )

    /** Observed by MainActivity so the UI switches language instantly. */
    val current: StateFlow<AppLanguage> = _current

    val hasChosen: Boolean get() = preferences.getBoolean(KEY_CHOSEN, false)
    val language: AppLanguage get() = _current.value

    fun save(language: AppLanguage) {
        preferences.edit()
            .putString(KEY_LANGUAGE, language.code)
            .putBoolean(KEY_CHOSEN, true)
            .apply()
        _current.value = language
    }
}

/** Reads the saved language without Hilt (needed in attachBaseContext, before injection). */
fun storedLanguage(context: Context): AppLanguage =
    AppLanguage.fromCode(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, AppLanguage.English.code)
    )

/** A copy of this context whose resources use [language]. */
fun Context.withLanguage(language: AppLanguage): Context {
    val locale = Locale.forLanguageTag(language.code)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration).apply {
        setLocale(locale)
        setLocales(LocaleList(locale))
    }
    return createConfigurationContext(config)
}

/** Applies [language] to the Application resources (notifications, services, ViewModel strings). */
fun applyAppLanguage(context: Context, language: AppLanguage) {
    val locale = Locale.forLanguageTag(language.code)
    Locale.setDefault(locale)
    LocaleList.setDefault(LocaleList(locale))
    val config = Configuration(context.resources.configuration).apply {
        setLocale(locale)
        setLocales(LocaleList(locale))
    }
    @Suppress("DEPRECATION")
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}