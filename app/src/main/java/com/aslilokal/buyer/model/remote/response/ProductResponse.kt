package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ProductResponse(
    val message: String,
    val result: Result,
    val success: Boolean
) : Parcelable

@Parcelize
data class Result(
    val docs: MutableList<Product>?,
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


@Parcelize
data class Product(
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
    val umkmTags: String?,
    val promotionTags: ArrayList<String>?,
    val promoPrice: String? = null,
    val sumCountView: Int
) : Parcelable