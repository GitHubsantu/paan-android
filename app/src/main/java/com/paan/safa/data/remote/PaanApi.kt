package com.paan.safa.data.remote

import kotlinx.serialization.json.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PaanApi {
    @POST("auth/otp/send")
    suspend fun sendOtp(@Body body: SendOtpRequest): ApiResponse<JsonElement>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): ApiResponse<AuthDataDto>

    @POST("auth/logout")
    suspend fun logout(): ApiResponse<JsonElement>

    @PUT("profile")
    suspend fun updateProfile(@Body body: ProfileRequest): ApiResponse<UserDto>

    @GET("profile")
    suspend fun profile(): ApiResponse<UserDto>

    @GET("categories")
    suspend fun categories(): ApiResponse<List<CategoryDto>>

    @GET("products")
    suspend fun products(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int,
    ): ApiResponse<List<ProductDto>>

    @GET("products/{id}")
    suspend fun product(@Path("id") id: String): ApiResponse<ProductDto>

    @POST("orders")
    suspend fun createOrder(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body body: CreateOrderRequest,
    ): ApiResponse<OrderDto>

    @GET("orders")
    suspend fun orders(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int,
    ): ApiResponse<List<OrderDto>>

    @GET("orders/{id}")
    suspend fun order(@Path("id") id: String): ApiResponse<OrderDto>

    @GET("orders/{id}/status")
    suspend fun orderStatus(@Path("id") id: String): ApiResponse<OrderStatusDto>

    @POST("orders/{id}/pay")
    suspend fun payOrder(@Path("id") id: String): ApiResponse<PayOrderDto>

    @GET("shop")
    suspend fun shop(): ApiResponse<ShopDto>

    @POST("orders/{id}/pickup")
    suspend fun pickupOrder(
        @Path("id") id: String,
        @Body body: PickupRequest,
    ): ApiResponse<OrderDto>

    @POST("device-tokens")
    suspend fun registerDevice(@Body body: DeviceTokenRequest): ApiResponse<JsonElement>

    @DELETE("device-tokens/{token}")
    suspend fun removeDevice(@Path("token") token: String): ApiResponse<JsonElement>

    // ---- Notifications ----
    @GET("notifications")
    suspend fun notifications(
        @Query("per_page") perPage: Int,
        @Query("page") page: Int,
    ): ApiResponse<List<NotificationDto>>

    @GET("notifications/unread-count")
    suspend fun unreadCount(): ApiResponse<UnreadCountDto>

    @POST("notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: String): ApiResponse<JsonElement>

    @POST("notifications/read-all")
    suspend fun markAllNotificationsRead(): ApiResponse<JsonElement>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): ApiResponse<JsonElement>
}