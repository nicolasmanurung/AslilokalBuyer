package com.aslilokal.buyer.model.remote.request

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthRequest(
    var emailBuyer: String,
    var passwordBuyer: String,
    var emailVerifyStatus: Boolean?
) : Parcelable
