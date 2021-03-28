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
    val __v: Int?,
    val _id: String?,
    val addressShop: String,
    val idBuyerAccount: String,
    val idProduct: String,
    val idSellerAccount: String,
    val categoryProduct: String?,
    val imgProduct: String,
    val isLocalShop: Boolean?,
    val nameProduct: String?,
    val nameShop: String,
    val noteProduct: String?,
    val productPrice: Int,
    var qtyProduct: Int
) : Parcelable