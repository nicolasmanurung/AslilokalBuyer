package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OneProductResponse(
    val message: String,
    val result: OneProduct,
    val success: Boolean
) : Parcelable

@Parcelize
data class OneProduct(
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
    val sumCountView: Int
) : Parcelable