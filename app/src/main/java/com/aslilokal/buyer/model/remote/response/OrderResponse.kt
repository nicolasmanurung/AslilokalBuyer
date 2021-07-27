package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderResponse(
    val message: String,
    val result: ArrayList<ResultOrder>,
    val success: Boolean
) : Parcelable

@Parcelize
data class ResultOrder(
    val __v: Int,
    val _id: String,
    val addressBuyer: String,
    val courierCost: Int,
    val courierType: String,
    val idBuyerAccount: String,
    val numberTelp: String,
    val nameBuyer: String,
    val idSellerAccount: String,
    val isCancelBuyer: Boolean,
    val isCancelSeller: Boolean,
    val isFinish: Boolean,
    val products: ArrayList<ProductOrder>,
    val statusOrder: String,
    val totalPayment: Int,
    val totalProductPrice: Int,
    val voucherCode: String,
    val messageCancel: String,
    val voucherId: String,
    val acceptAt: String,
    val finishAt: String,
    val orderAt: String,
    val resiCode: String?
) : Parcelable

@Parcelize
data class ProductOrder(
    val _id: String,
    val idProduct: String,
    val imgProduct: String,
    val nameProduct: String,
    val noteProduct: String,
    val priceAt: Int,
    val qty: Int,
    val productWeight: Int
) : Parcelable