package com.paan.safa.domain.repository

import com.paan.safa.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits the signed-in user, or null when logged out. */
    val session: Flow<User?>
    suspend fun requestOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(phone: String, otp: String): Result<User>
    suspend fun updateName(name: String): Result<Unit>
    suspend fun refreshProfile(): Result<Unit>
    suspend fun logout()
}