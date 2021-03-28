package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AuthResponse(
    val success: Boolean,
    val token: String? = null,
    val username: String?,
    val message: String? = null,
    val emailVerifyStatus: Boolean,
    val biodataVerifyStatus: Boolean
) : Parcelable