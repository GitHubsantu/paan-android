package com.paan.safa.push

import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.paan.safa.data.remote.ApiException
import com.paan.safa.data.remote.DeviceTokenRequest
import com.paan.safa.data.remote.PaanApi
import com.paan.safa.data.remote.apiCallUnit
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

private suspend fun <T> Task<T>.awaitOrNull(): T? = suspendCancellableCoroutine { cont ->
    addOnSuccessListener { if (cont.isActive) cont.resume(it) }
    addOnFailureListener { if (cont.isActive) cont.resume(null) }
    addOnCanceledListener { if (cont.isActive) cont.resume(null) }
}

@Singleton
class DeviceTokenRepository @Inject constructor(
    private val api: PaanApi,
) {
    @Volatile private var lastRegistered: String? = null

    /** Sends this phone's push token to the server. Safe to call many times. */
    suspend fun register(): Result<Unit> {
        val token = currentToken() ?: return Result.failure(ApiException("Push token not available."))
        if (token == lastRegistered) return Result.success(Unit)
        return apiCallUnit { api.registerDevice(DeviceTokenRequest(token, "android", "customer")) }
            .onSuccess { lastRegistered = token }
    }

    /** Logout step 1: ask the server to stop sending pushes to this phone. */
    suspend fun unregisterFromServer() {
        val token = currentToken() ?: return
        apiCallUnit { api.removeDevice(token) }
        lastRegistered = null
    }

    /** Logout step 2: make the old token useless, so nothing meant for the previous user can arrive. */
    suspend fun deleteLocalToken() {
        runCatching { FirebaseMessaging.getInstance().deleteToken().awaitOrNull() }
        lastRegistered = null
    }

    // Returns null (instead of crashing) when Firebase is not configured, for example a missing google-services.json.
    private suspend fun currentToken(): String? =
        runCatching { FirebaseMessaging.getInstance().token.awaitOrNull() }.getOrNull()
}