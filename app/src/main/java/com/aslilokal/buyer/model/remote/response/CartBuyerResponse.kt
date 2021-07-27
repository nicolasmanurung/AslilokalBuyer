package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartBuyerResponse(
    val message: String,
    val result: ArrayList<ItemCart>,
    val success: Boolean
) : Parcelable

@Parcelize
data class ItemCart(
    val __v: Int,
    val _id: String,
    val createAt: String,
    val descProduct: String,
    val idSellerAccount: String,
    val imgProduct: String,
    val isAvailable: Boolean,
    val lastUpdateAt: String,
    val nameProduct: String,
    val priceProduct: String,
    val priceServiceRange: String,
    val productCategory: String,
    val productWeight: Int,
    val promoPrice: Int?,
    val promotionTags: ArrayList<String>,
    val sumCountView: Int,
    val umkmTags: String
) : Parcelable