package com.aslilokal.buyer.utils

class Constants {
    companion object {
        const val BASE_URL = "https://aslilokalbackend.herokuapp.com"
        const val BASE_URL_RO = "https://api.rajaongkir.com"
        const val RO_KEY_ID = "dcda05cc2e57551d93e0d599e3874050"
        const val COURIER_TYPE = "jne"

        const val SEARCH_PRODUCT_TIME_DELAY = 500L

        // QUERY_PAGE_SIZE adalah LIMIT dalam 1 kali hit api
        const val QUERY_PAGE_SIZE = 9
        const val BUCKET_PRODUCT_URL =
            "https://kodelapo-product-img.s3.ap-southeast-1.amazonaws.com/"
        const val BUCKET_USR_URL = "https://kodelapo-usr-img.s3-ap-southeast-1.amazonaws.com/"
        const val BUCKET_USR_EVIDANCE_URL =
            "https://aslilokal-payment-receipts.s3-ap-southeast-1.amazonaws.com/"
    }
}