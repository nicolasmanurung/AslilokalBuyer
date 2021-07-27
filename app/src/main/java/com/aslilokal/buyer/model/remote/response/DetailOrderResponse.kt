package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DetailOrderResponse(
    val message: String,
    val result: DetailOrder,
    val success: Boolean
) : Parcelable

@Parcelize
data class DetailOrder(
    val __v: Int,
    val _id: String,
    val addressBuyer: String,
    val courierCost: Int,
    val courierType: String,
    val idBuyerAccount: String,
    val idSellerAccount: String,
    val isCancelBuyer: Boolean,
    val isCancelSeller: Boolean,
    val isFinish: Boolean,
    val nameBuyer: String,
    val numberTelp: String,
    val orderAt: String,
    val products: ArrayList<ProductOrder>,
    val statusOrder: String,
    val totalPayment: Int,
    val totalProductPrice: Int,
    val voucherCode: String,
    val voucherId: String,
    val imgPayment: String?,
    val resiCode: String?
) : Parcelable