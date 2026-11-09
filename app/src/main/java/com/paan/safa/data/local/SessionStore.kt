package com.paan.safa.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.paan.safa.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authStore: DataStore<Preferences> by preferencesDataStore(name = "auth_session")

/** Keeps the login token and basic user info in app-private storage. */
@Singleton
class SessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val ID = stringPreferencesKey("user_id")
        val NAME = stringPreferencesKey("user_name")
        val PHONE = stringPreferencesKey("user_phone")
    }

    val token: Flow<String?> = context.authStore.data.map { it[Keys.TOKEN] }

    val session: Flow<User?> = context.authStore.data.map { p ->
        if (p[Keys.TOKEN] == null) return@map null
        val id = p[Keys.ID] ?: return@map null
        User(id = id, name = p[Keys.NAME].orEmpty(), phone = p[Keys.PHONE].orEmpty())
    }

    suspend fun save(token: String, user: User) {
        context.authStore.edit {
            it[Keys.TOKEN] = token
            it[Keys.ID] = user.id
            it[Keys.NAME] = user.name
            it[Keys.PHONE] = user.phone
        }
    }

    suspend fun updateUser(user: User) {
        context.authStore.edit { if (it[Keys.TOKEN] != null) it[Keys.NAME] = user.name }
    }

    suspend fun clear() {
        context.authStore.edit { it.clear() }
    }

    // OkHttp interceptors run on a background thread, so a short blocking read is fine there.
    fun tokenBlocking(): String? = runBlocking { token.first() }
    fun clearBlocking() = runBlocking { clear() }
}
