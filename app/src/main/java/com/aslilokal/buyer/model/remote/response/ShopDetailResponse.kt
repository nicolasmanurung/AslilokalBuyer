package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopDetailResponse(
    val message: String,
    val result: Shop,
    val success: Boolean
) : Parcelable

@Parcelize
data class Shop(
    val __v: Int,
    val _id: String,
    val addressShop: String,
    val closeTime: String,
    val createdAt: String,
    val idSellerAccount: String,
    val imgShop: String,
    val isDelivery: Boolean,
    val isPickup: Boolean,
    val isTwentyFourHours: Boolean,
    val nameShop: String,
    val noTelpSeller: String,
    val noWhatsappShop: String,
    val openTime: String,
    val shopTypeStatus: String?,
    val sumFollowers: Int,
    val validityTypeStatus: String
) : Parcelable