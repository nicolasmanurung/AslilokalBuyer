package com.aslilokal.buyer.model.data.api

import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.response.ItemCart
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApiHelper(private val apiService: AslilokalAPI) {
    suspend fun getAllPopularProduct(page: Int, limit: Int) =
        apiService.getAllPopularProduct(page, limit)

    suspend fun getOneDetailProduct(idProduct: String) = apiService.getDetailProduct(idProduct)

    suspend fun getDetailShop(idShop: String) = apiService.getShopDetail(idShop)

    suspend fun getProductCategorizeByUmkm(umkmName: String) =
        apiService.getProductCategorizeByUmkm(umkmName)

    suspend fun getCartBuyer(token: String, idUser: String) = apiService.getCartBuyer(token, idUser)

    suspend fun getOrder(token: String, idUser: String, status: String) =
        apiService.getOrderOrderByStatus(token, idUser, status)

    suspend fun postLoginBuyer(authRequest: AuthRequest) = apiService.postLoginBuyer(authRequest)

    suspend fun postRegisterBuyer(authRequest: AuthRequest) =
        apiService.postRegisterBuyer(authRequest)

    suspend fun getVerifyToken(token: String, tokenVerify: String) =
        apiService.getVerifyToken(token, tokenVerify)

    suspend fun postResubmitToken(token: String) = apiService.postResubmitVerifyToken(token)

    suspend fun postProductToCart(token: String, idUser: String, product: ItemCart) =
        apiService.postProductToCart(token, idUser, product)

    suspend fun postBiodataBuyer(
        token: String,
        imgKtpBuyer: MultipartBody.Part?,
        imgSelfBuyer: MultipartBody.Part,
        idBuyerAccount: RequestBody,
        nameBuyer: RequestBody,
        noTelpBuyer: RequestBody,
        postalCode: RequestBody?,
        addressBuyer: RequestBody
    ) = apiService.postBiodataBuyer(
        token,
        imgKtpBuyer,
        imgSelfBuyer,
        idBuyerAccount,
        nameBuyer,
        noTelpBuyer,
        postalCode,
        addressBuyer
    )

    suspend fun getBiodataBuyer(
        token: String,
        username: String
    ) = apiService.getBiodataBuyer(token, username)

    suspend fun postOneOrder(
        token: String,
        orderRequest: OrderRequest
    ) = apiService.postOneOrder(token, orderRequest)
}