package com.aslilokal.buyer

import java.text.SimpleDateFormat
import java.util.*

fun main() {
    print(isDeathTimeOrder("2021-04-16T07:19:49.215Z").toString())
}

fun isDeathTimeOrder(overdate: String): Boolean {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:s.S'Z'", Locale.getDefault())
    val date: Date = format.parse(overdate)
    var finaloverdate = Date(date.time + (1000 * 60 * 60 * 24 * 2))
    val currentDate = Date()
    print("Final: $finaloverdate Current: $currentDate\n")
    return currentDate > finaloverdate
}