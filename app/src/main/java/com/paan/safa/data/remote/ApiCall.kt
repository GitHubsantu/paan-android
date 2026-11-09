package com.paan.safa.data.remote

import com.paan.safa.core.ui.UiText
import com.paan.safa.R
import androidx.annotation.StringRes
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import retrofit2.HttpException
import java.io.IOException

class ApiException(
    message: String,
    val httpCode: Int = 0,
    val errorCode: String? = null,
    /** Set for messages the APP wrote (not the server), so they can be shown in the chosen language. */
    @StringRes val messageRes: Int? = null,
) : Exception(message)

private fun appError(@StringRes res: Int, english: String, httpCode: Int = 0, errorCode: String? = null) =
    ApiException(english, httpCode, errorCode, res)

/** UiText for an error: app-written messages follow the app language, server-written ones are shown as received. */
fun Throwable.asUiText(): UiText? = when {
    this is ApiException && messageRes != null -> UiText.Res(messageRes)
    else -> message?.let { UiText.Plain(it) }
}

private val errorJson = Json { ignoreUnknownKeys = true; isLenient = true }

private const val UNEXPECTED = "The server sent an unexpected response. Please try again."
private const val OFFLINE = "No internet connection. Please check your network and try again."
private const val SESSION_EXPIRED = "Your session has expired. Please log in again."
private const val TOO_MANY = "Too many attempts. Please wait a moment and try again."
private const val SERVER = "Something went wrong on our side. Please try again."

private suspend fun <T> execute(block: suspend () -> ApiResponse<T>): Result<ApiResponse<T>> =
    try {
        val response = block()
        if (response.success) Result.success(response)
        else Result.failure(
            response.message?.let { ApiException(it, errorCode = response.code) }
                ?: appError(R.string.error_unexpected, UNEXPECTED, errorCode = response.code),
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.failure(e.toApiException())
    } catch (e: IOException) {
        Result.failure(appError(R.string.error_offline, OFFLINE))
    } catch (e: SerializationException) {
        Result.failure(appError(R.string.error_unexpected, UNEXPECTED))
    }

private fun HttpException.toApiException(): ApiException {
    val code = code()
    val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
    val parsed = body?.let { runCatching { errorJson.decodeFromString<ApiResponse<JsonElement>>(it) }.getOrNull() }
    val bodyMessage = parsed?.message?.takeIf { it.isNotBlank() }
    val firstFieldError = parsed?.errors?.values?.firstOrNull()?.firstOrNull()
    val errorCode = parsed?.code
    return when {
        code == 422 && firstFieldError != null -> ApiException(firstFieldError, httpCode = code, errorCode = errorCode)
        code == 401 -> appError(R.string.error_session_expired, SESSION_EXPIRED, code, errorCode)
        code == 429 -> appError(R.string.error_too_many_attempts, TOO_MANY, code, errorCode)
        code >= 500 -> appError(R.string.error_server, SERVER, code, errorCode)
        bodyMessage != null -> ApiException(bodyMessage, httpCode = code, errorCode = errorCode)
        code == 403 -> appError(R.string.error_forbidden, "You don't have permission to do that.", code, errorCode)
        code == 404 -> appError(R.string.error_not_found, "We couldn't find that.", code, errorCode)
        else -> appError(R.string.error_unexpected, UNEXPECTED, code, errorCode)
    }
}

/** Calls the API and returns just `data`. */
suspend fun <T : Any> apiCall(block: suspend () -> ApiResponse<T>): Result<T> =
    execute(block).mapCatching { it.data ?: throw appError(R.string.error_unexpected, UNEXPECTED) }

/** For endpoints whose body we don't need. */
suspend fun apiCallUnit(block: suspend () -> ApiResponse<JsonElement>): Result<Unit> =
    execute(block).map { }

/** Returns `data` together with pagination `meta`. */
suspend fun <T : Any> apiCallWithMeta(block: suspend () -> ApiResponse<T>): Result<Pair<T, PageMeta?>> =
    execute(block).mapCatching { (it.data ?: throw appError(R.string.error_unexpected, UNEXPECTED)) to it.meta }