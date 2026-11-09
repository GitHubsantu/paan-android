package com.paan.safa.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paan.safa.ui.components.BottomTab
import com.paan.safa.ui.screens.cart.CartScreen
import com.paan.safa.ui.screens.checkout.CheckoutScreen
import com.paan.safa.ui.screens.home.HomeScreen
import com.paan.safa.ui.screens.info.InfoContent
import com.paan.safa.ui.screens.info.InfoScreen
import com.paan.safa.R
import com.paan.safa.ui.screens.login.LoginScreen
import com.paan.safa.ui.screens.language.LanguageScreen
import com.paan.safa.ui.screens.registration.CompleteProfileScreen
import com.paan.safa.ui.screens.notifications.NotificationsScreen
import com.paan.safa.ui.screens.order.OrderConfirmationScreen
import com.paan.safa.ui.screens.order.OrderStatusScreen
import com.paan.safa.ui.screens.orders.OrdersScreen
import com.paan.safa.ui.screens.otp.OtpScreen
import com.paan.safa.ui.screens.product.ProductDetailScreen
import com.paan.safa.ui.screens.profile.ProfileScreen
import com.paan.safa.ui.screens.splash.SplashRoute
import com.paan.safa.ui.screens.payment.PaymentScreen
import kotlinx.coroutines.flow.first

@Composable
fun AppNavHost() {
    val nav = rememberNavController()
    val sessionViewModel: SessionViewModel = hiltViewModel()
    val loggedIn by sessionViewModel.loggedIn.collectAsStateWithLifecycle()

    // Back from the background: refresh the bell badge.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { sessionViewModel.onAppResumed() }

    // Logged out (logout button or an expired token) while inside the app: go to Login, clear the history.
    LaunchedEffect(loggedIn) {
        if (loggedIn == false) {
            val route = nav.currentDestination?.route
            val isPublic = route == Route.Splash.path || route == Route.Login.path || route == Route.Otp.path
            if (route != null && !isPublic) {
                nav.navigate(Route.Login.path) { popUpTo(nav.graph.id) { inclusive = true } }
            }
        }
    }
    // A tapped notification opens that order once the app is past splash/login.
    val pendingOrder by sessionViewModel.pendingOrderId.collectAsStateWithLifecycle()
    LaunchedEffect(pendingOrder, loggedIn) {
        val id = pendingOrder ?: return@LaunchedEffect
        when (loggedIn) {
            null -> return@LaunchedEffect
            false -> { sessionViewModel.consumeDeepLink(); return@LaunchedEffect }
            true -> Unit
        }
        nav.currentBackStackEntryFlow.first { entry ->
            val route = entry.destination.route
            route != null && route != Route.Splash.path && route != Route.Login.path && route != Route.Otp.path
        }
        nav.navigate(Route.OrderStatus.create(id))
        sessionViewModel.consumeDeepLink()
        sessionViewModel.markOrderNotificationsRead(id)   // the user tapped the push: it is read now
    }

    /** Bottom-bar navigation: tabs replace each other, Home stays at the bottom of the stack. */
    val onTabSelected: (BottomTab) -> Unit = { tab ->
        if (tab == BottomTab.Home) {
            nav.popBackStack(Route.Home.path, inclusive = false)
        } else {
            val route = when (tab) {
                BottomTab.Orders -> Route.Orders
                BottomTab.Cart -> Route.Cart
                else -> Route.Profile
            }
            nav.navigate(route.path) {
                popUpTo(Route.Home.path) { inclusive = false }
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = nav, startDestination = Route.Splash.path) {

        composable(Route.Splash.path) {
            SplashRoute(
                onLoggedIn = {
                    nav.navigate(if (sessionViewModel.hasChosenLanguage) Route.Home.path else Route.Language.path) { popUpTo(Route.Splash.path) { inclusive = true } }
                },
                onLoggedOut = {
                    nav.navigate(if (sessionViewModel.hasChosenLanguage) Route.Login.path else Route.Language.path) { popUpTo(Route.Splash.path) { inclusive = true } }
                },
            )
        }

        composable(Route.Login.path) {
            LoginScreen(onOtpSent = { phone -> nav.navigate(Route.Otp.create(phone)) })
        }

        composable(Route.Language.path) {
            LanguageScreen(
                onDone = {
                    if (nav.previousBackStackEntry != null) {
                        // Opened from Profile: just go back.
                        nav.popBackStack()
                    } else {
                        // First launch: continue to Home or Login and drop the language screen.
                        val next = if (loggedIn == true) Route.Home.path else Route.Login.path
                        nav.navigate(next) { popUpTo(Route.Language.path) { inclusive = true } }
                    }
                },
            )
        }

        composable(
            Route.Otp.path,
            arguments = listOf(navArgument(Route.Otp.ARG_PHONE) { type = NavType.StringType }),
        ) {
            OtpScreen(
                onBack = { nav.popBackStack() },
                onVerified = { needsName ->
                    nav.navigate(if (needsName) Route.CompleteProfile.path else Route.Home.path) {
                        popUpTo(Route.Login.path) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.CompleteProfile.path) {
            CompleteProfileScreen(
                onDone = { nav.navigate(Route.Home.path) { popUpTo(Route.CompleteProfile.path) { inclusive = true } } },
            )
        }

        composable(Route.Home.path) {
            HomeScreen(
                onTabSelected = onTabSelected,
                onProductClick = { id -> nav.navigate(Route.ProductDetails.create(id)) },
                onOrderClick = { id -> nav.navigate(Route.OrderStatus.create(id)) },
                onNotifications = {
                    nav.navigate(Route.Notifications.path) { launchSingleTop = true }
                },
            )
        }

        composable(
            Route.ProductDetails.path,
            arguments = listOf(navArgument(Route.ProductDetails.ARG_ID) { type = NavType.StringType }),
        ) {
            ProductDetailScreen(
                onBack = { nav.popBackStack() },
                onAddMore = { nav.popBackStack() },   // back to Home to pick another paan
                onViewCart = {
                    nav.navigate(Route.Cart.path) { popUpTo(Route.Home.path) }
                },
            )
        }

        composable(Route.Cart.path) {
            CartScreen(
                onBack = { nav.popBackStack() },
                onAddMore = { nav.popBackStack(Route.Home.path, inclusive = false) },
                onCheckout = { nav.navigate(Route.Checkout.path) },
                onTabSelected = onTabSelected,
            )
        }

        composable(Route.Checkout.path) {
            CheckoutScreen(
                onBack = { nav.popBackStack() },
                onPaid = { orderId ->
                    nav.navigate(Route.Payment.create(orderId)) { popUpTo(Route.Home.path) }
                },
            )
        }

        composable(
            Route.OrderConfirmation.path,
            arguments = listOf(navArgument(Route.OrderConfirmation.ARG_ID) { type = NavType.StringType }),
        ) {
            OrderConfirmationScreen(
                onTrackOrder = { orderId -> nav.navigate(Route.OrderStatus.create(orderId)) },
                onHome = {
                    nav.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } }
                },
            )
        }

        composable(
            Route.OrderStatus.path,
            arguments = listOf(navArgument(Route.OrderStatus.ARG_ID) { type = NavType.StringType }),
        ) {
            OrderStatusScreen(
                onBack = { nav.popBackStack() },
                onHome = {
                    nav.navigate(Route.Home.path) { popUpTo(Route.Home.path) { inclusive = true } }
                },
            )
        }

        composable(Route.Orders.path) {
            OrdersScreen(
                onTabSelected = onTabSelected,
                onOrderClick = { id -> nav.navigate(Route.OrderStatus.create(id)) },
            )
        }

        composable(Route.Profile.path) {
            ProfileScreen(
                onTabSelected = onTabSelected,
                onMyOrders = { onTabSelected(BottomTab.Orders) },
                onNotifications = { nav.navigate(Route.Notifications.path) { launchSingleTop = true } },
                onHelp = { nav.navigate(Route.Help.path) },
                onTerms = { nav.navigate(Route.Terms.path) },
                onPrivacy = { nav.navigate(Route.Privacy.path) },
                onLanguage = { nav.navigate(Route.Language.path) },
                // Logging out clears the session; the effect at the top then sends the user to Login.
                onLoggedOut = { },
            )
        }

        composable(Route.Notifications.path) {
            NotificationsScreen(
                onBack = { nav.popBackStack() },
                onOpenOrder = { id -> nav.navigate(Route.OrderStatus.create(id)) },
            )
        }
        composable(Route.Help.path) {
            InfoScreen(R.string.info_help_title, InfoContent.help, onBack = { nav.popBackStack() })
        }
        composable(Route.Terms.path) {
            InfoScreen(R.string.info_terms_title, InfoContent.terms, onBack = { nav.popBackStack() })
        }
        composable(Route.Privacy.path) {
            InfoScreen(R.string.info_privacy_title, InfoContent.privacy, onBack = { nav.popBackStack() })
        }
        composable(
            Route.Payment.path,
            arguments = listOf(navArgument(Route.Payment.ARG_ID) { type = NavType.StringType }),
        ) {
            PaymentScreen(
                onPaid = { orderId ->
                    nav.navigate(Route.OrderConfirmation.create(orderId)) { popUpTo(Route.Home.path) }
                },
                // The order exists but is unpaid: send the user to their orders list.
                onBack = { onTabSelected(BottomTab.Orders) },
            )
        }
    }
}
