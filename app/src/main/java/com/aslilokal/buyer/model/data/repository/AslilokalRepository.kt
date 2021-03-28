package com.aslilokal.buyer.model.data.repository

import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.response.ItemCart
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AslilokalRepository(val apiHelper: ApiHelper) {
    suspend fun getAllPopularProduct(page: Int, limit: Int) =
        apiHelper.getAllPopularProduct(page, limit)

    suspend fun getOneDetailProduct(idProduct: String) = apiHelper.getOneDetailProduct(idProduct)

    suspend fun getDetailShop(idShop: String) = apiHelper.getDetailShop(idShop)

    suspend fun getProductCategorizeByUmkm(umkmName: String) =
        apiHelper.getProductCategorizeByUmkm(umkmName)

    suspend fun getCartBuyer(token: String, idUser: String) = apiHelper.getCartBuyer(token, idUser)

    suspend fun getOrder(token: String, idUser: String, status: String) =
        apiHelper.getOrder(token, idUser, status)

    suspend fun postLoginBuyer(authRequest: AuthRequest) = apiHelper.postLoginBuyer(authRequest)

    suspend fun postRegisterBuyer(authRequest: AuthRequest) =
        apiHelper.postRegisterBuyer(authRequest)

    suspend fun getTokenVerify(token: String, tokenVerify: String) =
        apiHelper.getVerifyToken(token, tokenVerify)

    suspend fun postTokenVerifyResubmit(token: String) = apiHelper.postResubmitToken(token)

    suspend fun postProductToCart(token: String, idUser: String, product: ItemCart) =
        apiHelper.postProductToCart(token, idUser, product)

    suspend fun postSellerBiodata(
        token: String,
        imgKtpBuyer: MultipartBody.Part?,
        imgSelfBuyer: MultipartBody.Part,
        idBuyerAccount: RequestBody,
        nameBuyer: RequestBody,
        noTelpBuyer: RequestBody,
        postalCode: RequestBody?,
        addressBuyer: RequestBody
    ) = apiHelper.postBiodataBuyer(
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
    ) = apiHelper.getBiodataBuyer(token, username)

    suspend fun postOneOrder(
        token: String,
        orderRequest: OrderRequest
    ) = apiHelper.postOneOrder(token, orderRequest)
}