package com.aslilokal.buyer.model.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ROCostResponse(
    val rajaongkir: RajaongkirCost
) : Parcelable

@Parcelize
data class RajaongkirCost(
    val destination_details: DestinationDetails,
    val origin_details: OriginDetails,
    val query: Query,
    val results: ArrayList<ResultCost>,
    val status: Status
) : Parcelable

@Parcelize
data class DestinationDetails(
    val city_id: String,
    val city_name: String,
    val postal_code: String,
    val province: String,
    val province_id: String,
    val type: String
) : Parcelable

@Parcelize
data class OriginDetails(
    val city_id: String,
    val city_name: String,
    val postal_code: String,
    val province: String,
    val province_id: String,
    val type: String
) : Parcelable

@Parcelize
data class Query(
    val courier: String,
    val destination: String,
    val origin: String,
    val weight: Int
) : Parcelable

@Parcelize
data class ResultCost(
    val code: String,
    val costs: ArrayList<Cost>,
    val name: String
) : Parcelable

@Parcelize
data class Cost(
    val cost: ArrayList<CostX>,
    val description: String,
    val service: String
) : Parcelable

@Parcelize
data class CostX(
    val etd: String,
    val note: String,
    val value: Int
) : Parcelable