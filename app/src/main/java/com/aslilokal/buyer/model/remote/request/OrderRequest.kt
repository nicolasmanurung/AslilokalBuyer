package com.aslilokal.buyer.model.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderRequest(
    val addressBuyer: String,
    val courierCost: String,
    val courierType: String,
    val idBuyerAccount: String,
    val idSellerAccount: String,
    val nameBuyer: String,
    val numberTelp: String,
    val products: ArrayList<Product>,
    val statusOrder: String,
    val totalPayment: String,
    val totalProductPrice: String,
    val voucherCode: String,
    val voucherId: String,
    val resiCode: String
) : Parcelable

@Parcelize
data class Product(
    val idSellerAccount: String,
    val idProduct: String,
    val imgProduct: String,
    val nameProduct: String,
    val noteProduct: String,
    val priceAt: String,
    var qty: String,
    val productWeight: String
) : Parcelable