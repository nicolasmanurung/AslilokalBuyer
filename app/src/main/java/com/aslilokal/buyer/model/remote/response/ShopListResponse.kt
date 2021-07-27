package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShopListResponse(
    val message: String,
    val result: ResultShop,
    val success: Boolean
) : Parcelable

@Parcelize
data class ResultShop(
    val docs: MutableList<Shop>?,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val limit: Int,
    val nextPage: Int,
    val page: Int,
    val pagingCounter: Int,
    val prevPage: Int,
    val totalDocs: Int,
    val totalPages: Int
) : Parcelable