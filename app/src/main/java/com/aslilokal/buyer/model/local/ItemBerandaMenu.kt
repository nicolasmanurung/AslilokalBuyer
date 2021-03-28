package com.aslilokal.buyer.model.local

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ItemBerandaMenu(
    val imgMenuName: Int,
    val menuName: String
) : Parcelable
