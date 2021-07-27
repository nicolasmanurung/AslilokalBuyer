package com.aslilokal.buyer.model.remote.request

import android.os.Parcelable
import com.aslilokal.buyer.model.remote.response.RajaOngkirAddress
import kotlinx.parcelize.Parcelize

@Parcelize
data class BiodataRequest(
    val addressBuyer: String,
    val nameBuyer: String,
    val noTelpBuyer: String,
    val postalCodeInput: String,
    val rajaOngkir: RajaOngkirAddress
) : Parcelable