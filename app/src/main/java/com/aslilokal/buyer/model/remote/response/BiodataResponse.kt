package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class BiodataResponse(
    val message: String,
    val result: DetailBiodata,
    val success: Boolean
) : Parcelable

@Parcelize
data class DetailBiodata(
    val __v: Int,
    val _id: String,
    val addressBuyer: String,
    val idBuyerAccount: String,
    val imgKtpBuyer: String?,
    val imgSelfBuyer: String,
    val nameBuyer: String,
    val noTelpBuyer: String,
    val postalCodeInput: String?,
    val rajaOngkir: RajaOngkirAddress?
) : Parcelable

@Parcelize
data class RajaOngkirAddress(
    val city_id: String,
    val city_name: String,
    val postal_code: String,
    val province: String,
    val province_id: String,
    val type: String?
) : Parcelable