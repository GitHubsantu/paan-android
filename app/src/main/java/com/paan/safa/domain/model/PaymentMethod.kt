package com.paan.safa.domain.model

import androidx.annotation.StringRes
import com.paan.safa.R

enum class PaymentMethod(@StringRes val labelRes: Int, @StringRes val subtitleRes: Int) {
    Upi(R.string.payment_upi, R.string.payment_upi_subtitle),
    Card(R.string.payment_card, R.string.payment_card_subtitle),
}
