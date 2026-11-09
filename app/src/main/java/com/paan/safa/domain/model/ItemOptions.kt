package com.paan.safa.domain.model

import androidx.annotation.StringRes
import android.content.Context
import com.paan.safa.R

enum class Sweetness(@StringRes val labelRes: Int, val api: String, @StringRes val noteRes: Int? = null) {
    Regular(R.string.option_regular_sweet, "regular", R.string.option_classic_recipe),
    Mild(R.string.option_mild_sweet, "mild"),
    LowGulkand(R.string.option_low_gulkand, "low_gulkand"),
    ExtraGulkand(R.string.option_extra_gulkand, "extra_gulkand"),
}

enum class SupariType(@StringRes val labelRes: Int, val api: String, @StringRes val noteRes: Int) {
    Fried(R.string.option_fried_supari, "fried", R.string.option_crunchy_roasted),
    Kacha(R.string.option_kacha_supari, "kacha", R.string.option_raw_traditional),
}

/** Customisation chosen for one paan. No option changes the price. */
data class ItemOptions(
    val sweetness: Sweetness = Sweetness.Regular,
    val supari: SupariType = SupariType.Fried,
) {
    fun summary(context: Context): String = context.getString(
        R.string.option_summary,
        context.getString(sweetness.labelRes),
        context.getString(supari.labelRes),
    )
    companion object {
        val Default = ItemOptions()
    }
}
