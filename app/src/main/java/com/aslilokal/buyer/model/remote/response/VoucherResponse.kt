package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VoucherResponse(
    val message: String,
    val result: ArrayList<VoucherItem>,
    val success: Boolean
) : Parcelable

@Parcelize
data class VoucherItem(
    val __v: Int,
    val _id: String,
    val codeVoucher: String,
    val createdAt: String,
    val idSellerAccount: String,
    val minimumPurchase: Int,
    val validity: String,
    val valueVoucher: Int
) : Parcelable
