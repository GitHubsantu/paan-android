package com.paan.safa.data.repository

import com.paan.safa.data.local.SessionStore
import com.paan.safa.data.remote.PaanApi
import com.paan.safa.data.remote.ProfileRequest
import com.paan.safa.data.remote.SendOtpRequest
import com.paan.safa.data.remote.VerifyOtpRequest
import com.paan.safa.data.remote.apiCall
import com.paan.safa.data.remote.apiCallUnit
import com.paan.safa.data.remote.toDomain
import com.paan.safa.domain.model.User
import com.paan.safa.domain.repository.AuthRepository
import com.paan.safa.push.DeviceTokenRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAuthRepository @Inject constructor(
    private val api: PaanApi,
    private val store: SessionStore,
    private val deviceTokens: DeviceTokenRepository,
) : AuthRepository {

    override val session: Flow<User?> = store.session

    override suspend fun requestOtp(phone: String): Result<Unit> =
        apiCallUnit { api.sendOtp(SendOtpRequest(phone)) }

    override suspend fun verifyOtp(phone: String, otp: String): Result<User> =
        apiCall { api.verifyOtp(VerifyOtpRequest(phone, otp)) }.map { data ->
            val user = data.user.toDomain()
            store.save(data.token, user)
            user
        }

    override suspend fun updateName(name: String): Result<Unit> =
        apiCall { api.updateProfile(ProfileRequest(name)) }.map { dto ->
            store.updateUser(dto.toDomain())
        }

    override suspend fun refreshProfile(): Result<Unit> =
        apiCall { api.profile() }.map { dto -> store.updateUser(dto.toDomain()) }

    override suspend fun logout() {
        deviceTokens.unregisterFromServer()   // stop pushes while the login is still valid
        apiCallUnit { api.logout() }
        store.clear()
    }
}