package com.paan.safa.core.navigation

sealed class Route(val path: String) {
    data object Language : Route("language")
    data object Splash : Route("splash")
    data object Login : Route("login")
    data object Otp : Route("otp/{phone}") {
        const val ARG_PHONE = "phone"
        fun create(phone: String) = "otp/$phone"
    }
    data object CompleteProfile : Route("complete-profile")
    data object Home : Route("home")
    data object ProductDetails : Route("product/{id}") {
        const val ARG_ID = "id"
        fun create(id: String) = "product/$id"
    }
    data object Orders : Route("orders")
    data object Cart : Route("cart")
    data object Checkout : Route("checkout")

    data object Payment : Route("payment/{id}") {
        const val ARG_ID = "id"
        fun create(id: String) = "payment/$id"
    }
    data object OrderConfirmation : Route("order-confirmation/{id}") {
        const val ARG_ID = "id"
        fun create(id: String) = "order-confirmation/$id"
    }
    data object OrderStatus : Route("order/{id}") {
        const val ARG_ID = "id"
        fun create(id: String) = "order/$id"
    }
    data object Profile : Route("profile")
    data object Notifications : Route("notifications")
    data object Help : Route("help")
    data object Terms : Route("terms")
    data object Privacy : Route("privacy")
}
