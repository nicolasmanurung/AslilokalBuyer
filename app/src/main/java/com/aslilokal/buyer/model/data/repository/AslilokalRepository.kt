package com.aslilokal.buyer.model.data.repository

import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.remote.request.ArrayStringRequest
import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.model.remote.request.BiodataRequest
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.response.DetailBiodata
import com.aslilokal.buyer.model.remote.response.ItemCart
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AslilokalRepository(val apiHelper: ApiHelper) {
    suspend fun getAllPopularProduct(page: Int, limit: Int) =
        apiHelper.getAllPopularProduct(page, limit)

    suspend fun getProductByCategorize(type: String, page: Int, limit: Int) =
        apiHelper.getAllProductByCategorize(type, page, limit)

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

    suspend fun postProductToCart(token: String, idUser: String, idProduct: String) =
        apiHelper.postProductToCart(token, idUser, idProduct)

    suspend fun postSellerBiodata(
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
    ) = apiHelper.postBiodataBuyer(
        token,
        imgKtpBuyer,
        imgSelfBuyer,
        idBuyerAccount,
        nameBuyer,
        noTelpBuyer,
        addressBuyer,
        postalCodeInput,
        nameAcceptPackage,
        cityId, provinceId, provinceName, cityName, postalCode
    )

    suspend fun getBiodataBuyer(
        token: String,
        username: String
    ) = apiHelper.getBiodataBuyer(token, username)

    suspend fun postOneOrder(
        token: String,
        orderRequest: OrderRequest
    ) = apiHelper.postOneOrder(token, orderRequest)

    suspend fun getCitiesRO(
        key: String
    ) = apiHelper.getCitiesRO(key)

    suspend fun postCostRO(
        key: String,
        origin: String,
        destination: String,
        weight: String,
        courier: String
    ) = apiHelper.postCostRO(key, origin, destination, weight, courier)

    suspend fun getAllVouchersByBuyer(
        username: String
    ) = apiHelper.getAllVouchersByBuyer(username)

    suspend fun deleteProductsFromCart(
        key: String,
        username: String,
        products: String
    ) = apiHelper.deleteProductsFromCart(key, username, products)

    suspend fun getDetailOrder(
        key: String,
        orderId: String
    ) = apiHelper.getDetailOrder(key, orderId)

    suspend fun getProductsByIdShop(
        idShop: String
    ) = apiHelper.getProductsByIdShop(idShop)

    suspend fun getProductShopByName(
        shopId: String,
        nameProduct: String
    ) = apiHelper.getProductShopByName(shopId, nameProduct)

    suspend fun getSearchProductsByName(
        name: String,
        type: String,
        page: Int,
        limit: Int
    ) = apiHelper.getSearchProductsByName(name, type, page, limit)

    suspend fun getSearchShopsByName(
        name: String,
        page: Int,
        limit: Int
    ) = apiHelper.getSearchShopsByName(name, page, limit)

    suspend fun putBuyerBiodata(
        token: String,
        idUser: String,
        biodata: BiodataRequest
    ) = apiHelper.putBuyerBiodata(token, idUser, biodata)
}