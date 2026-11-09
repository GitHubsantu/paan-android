package com.paan.safa

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.paan.safa.core.localization.AppLanguage
import com.paan.safa.core.localization.LanguageStore
import com.paan.safa.core.localization.applyAppLanguage
import com.paan.safa.core.localization.storedLanguage
import com.paan.safa.core.localization.withLanguage
import com.paan.safa.core.navigation.AppNavHost
import com.paan.safa.core.navigation.DeepLinkHolder
import com.paan.safa.ui.theme.SafaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var deepLinks: DeepLinkHolder
    @Inject lateinit var languageStore: LanguageStore

    /** Applies the saved language on every cold start / recreate, before anything is drawn. */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase.withLanguage(storedLanguage(newBase)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyAppLanguage(applicationContext, languageStore.language)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            val language by languageStore.current.collectAsState()
            val baseContext = LocalContext.current
            val baseConfig = LocalConfiguration.current
            val localized = remember(language, baseConfig) { LocalizedContext.create(baseContext, language) }
            CompositionLocalProvider(
                LocalContext provides localized,
                LocalConfiguration provides localized.config,
            ) {
                SafaTheme { AppNavHost() }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(i: Intent?) {
        val id = i?.getStringExtra(EXTRA_ORDER_ID)?.takeIf { it.isNotEmpty() } ?: return
        i.removeExtra(EXTRA_ORDER_ID)
        deepLinks.open(id)
    }

    companion object {
        const val EXTRA_ORDER_ID = "open_order_id"
    }
}

/** Gives Compose the selected language's resources instantly, with no activity restart. */
private class LocalizedContext(
    base: Context,
    private val res: Resources,
    val config: Configuration,
) : ContextWrapper(base) {

    override fun getResources(): Resources = res

    companion object {
        fun create(base: Context, language: AppLanguage): LocalizedContext {
            val localizedBase = base.withLanguage(language)
            return LocalizedContext(base, localizedBase.resources, localizedBase.resources.configuration)
        }
    }
}