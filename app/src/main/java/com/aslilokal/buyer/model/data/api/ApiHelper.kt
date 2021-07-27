package com.aslilokal.buyer.model.data.api

import com.aslilokal.buyer.model.remote.request.ArrayStringRequest
import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.model.remote.request.BiodataRequest
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.response.DetailBiodata
import com.aslilokal.buyer.model.remote.response.ItemCart
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ApiHelper(private val apiService: AslilokalAPI) {
    suspend fun getAllPopularProduct(page: Int, limit: Int) =
        apiService.getAllPopularProduct(page, limit)

    suspend fun getAllProductByCategorize(type: String, page: Int, limit: Int) =
        apiService.getProductByCategorize(type, page, limit)

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

    suspend fun postProductToCart(token: String, idUser: String, idProduct: String) =
        apiService.postProductToCart(token, idUser, idProduct)

    suspend fun postBiodataBuyer(
        token: String,
        imgKtpBuyer: MultipartBody.Part?,
        imgSelfBuyer: MultipartBody.Part,
        idBuyerAccount: RequestBody,
        nameBuyer: RequestBody,
        noTelpBuyer: RequestBody,
        addressBuyer: RequestBody,
        postalCodeInput: RequestBody,
        nameAcceptPackage: RequestBody,
        cityId: RequestBody,
        provinceId: RequestBody,
        provinceName: RequestBody,
        cityName: RequestBody,
        postalCode: RequestBody
    ) = apiService.postBiodataBuyer(
        token,
        imgKtpBuyer,
        imgSelfBuyer,
        idBuyerAccount,
        nameBuyer,
        noTelpBuyer,
        addressBuyer,
        postalCodeInput,
        nameAcceptPackage,
        cityId,
        provinceId,
        provinceName,
        cityName,
        postalCode
    )

    suspend fun getBiodataBuyer(
        token: String,
        username: String
    ) = apiService.getBiodataBuyer(token, username)

    suspend fun postOneOrder(
        token: String,
        orderRequest: OrderRequest
    ) = apiService.postOneOrder(token, orderRequest)

    suspend fun getCitiesRO(
        key: String
    ) = apiService.ROGetCities(key)

    suspend fun postCostRO(
        key: String,
        origin: String,
        destination: String,
        weight: String,
        courier: String
    ) = apiService.ROPostCost(key, origin, destination, weight, courier)

    suspend fun getAllVouchersByBuyer(
        username: String
    ) = apiService.getAllVouchersByBuyer(username)

    suspend fun deleteProductsFromCart(
        key: String,
        username: String,
        products: String
    ) = apiService.deleteProductsFromCart(key, username, products)

    suspend fun getDetailOrder(
        key: String,
        orderId: String
    ) = apiService.getDetailOrder(key, orderId)

    suspend fun getProductsByIdShop(
        shopId: String
    ) = apiService.getProductsByIdShop(shopId)

    suspend fun getProductShopByName(
        shopId: String,
        nameProduct: String
    ) = apiService.getProductShopByName(shopId, nameProduct)

    suspend fun getSearchProductsByName(
        name: String,
        type: String,
        page: Int,
        limit: Int
    ) = apiService.getSearchProductsByName(name, type, page, limit)

    suspend fun getSearchShopsByName(
        name: String,
        page: Int,
        limit: Int
    ) = apiService.getSearchShopByName(name, page, limit)

    suspend fun putBuyerBiodata(
        token: String,
        idUser: String,
        biodata: BiodataRequest
    ) = apiService.putBuyerBiodata(token, idUser, biodata)
}