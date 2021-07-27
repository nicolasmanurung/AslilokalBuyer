package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ListProductResponse(
    val message: String,
    val result: ArrayList<Product>,
    val success: Boolean
) : Parcelable