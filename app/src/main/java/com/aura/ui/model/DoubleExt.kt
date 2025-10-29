package com.aura.ui.model

import java.text.NumberFormat
import java.util.Locale

/**
 * Formate le Double en format monétaire français.
 */
fun Double.formatBalance(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    return format.format(this)
}