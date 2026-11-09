package com.paan.safa.di

import com.paan.safa.data.repository.InMemoryCartRepository
import com.paan.safa.data.repository.RemoteAuthRepository
import com.paan.safa.data.repository.RemoteOrderRepository
import com.paan.safa.data.repository.RemoteProductRepository
import com.paan.safa.domain.repository.AuthRepository
import com.paan.safa.domain.repository.CartRepository
import com.paan.safa.domain.repository.OrderRepository
import com.paan.safa.domain.repository.ProductRepository
import com.paan.safa.data.repository.RemoteNotificationRepository
import com.paan.safa.domain.repository.NotificationRepository
import com.paan.safa.data.repository.RemoteShopRepository
import com.paan.safa.domain.repository.ShopRepository
import com.paan.safa.data.repository.RemoteGatewayPaymentRepository
import com.paan.safa.domain.repository.GatewayPaymentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuth(impl: RemoteAuthRepository): AuthRepository
    @Binds @Singleton abstract fun bindProducts(impl: RemoteProductRepository): ProductRepository
    @Binds @Singleton abstract fun bindCart(impl: InMemoryCartRepository): CartRepository
    @Binds @Singleton abstract fun bindOrders(impl: RemoteOrderRepository): OrderRepository
    @Binds @Singleton abstract fun bindGatewayPayments(impl: RemoteGatewayPaymentRepository): GatewayPaymentRepository
    @Binds @Singleton abstract fun bindNotifications(impl: RemoteNotificationRepository): NotificationRepository
    @Binds @Singleton abstract fun bindShop(impl: RemoteShopRepository): ShopRepository
}