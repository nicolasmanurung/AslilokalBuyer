package com.aslilokal.buyer.model.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ArrayStringRequest(
    val products: ArrayList<String>
) : Parcelable