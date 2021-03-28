package com.aslilokal.buyer.utils

import java.text.NumberFormat
import java.util.*

class CustomFunctions {
    fun formatRupiah(number: Double): String? {
        val localeID = Locale("in", "ID")
        val formatRupiah: NumberFormat = NumberFormat.getCurrencyInstance(localeID)
        var finalNumber = formatRupiah.format(number)
        return finalNumber.replace(",00", "")
    }
}