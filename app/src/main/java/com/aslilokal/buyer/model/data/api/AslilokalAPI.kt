package com.aslilokal.buyer.model.data.api

import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface AslilokalAPI {
    @Headers("Content-Type:application/json")
    @GET("buyer/products/popular")
    suspend fun getAllPopularProduct(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ProductResponse>

    @Headers("Content-Type:application/json")
    @GET("buyer/product/detail/{idProduct}")
    suspend fun getDetailProduct(
        @Path("idProduct") idProduct: String
    ): Response<OneProductResponse>

    @Headers("Content-Type:application/json")
    @GET("buyer/shop/detail/{idShop}")
    suspend fun getShopDetail(
        @Path("idShop") idShop: String
    ): Response<ShopDetailResponse>

    @Headers("Content-Type:application/json")
    @GET("buyer/products/umkm")
    suspend fun getProductCategorizeByUmkm(
        @Query("umkm") umkm: String
    ): Response<ListProductResponse>

    @Headers("Content-Type:application/json")
    @GET("buyer/cart/{idUser}")
    suspend fun getCartBuyer(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String
    ): Response<CartBuyerResponse>

    @Headers("Content-Type:application/json")
    @GET("buyer/orders/{idUser}")
    suspend fun getOrderOrderByStatus(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String,
        @Query("statusOrder") status: String
    ): Response<OrderResponse>

    @Headers("Content-Type:application/json")
    @POST("buyer/login")
    suspend fun postLoginBuyer(
        @Body buyerData: AuthRequest
    ): Response<AuthResponse>

    @Headers("Content-Type:application/json")
    @POST("buyer/register")
    suspend fun postRegisterBuyer(
        @Body registerRequest: AuthRequest
    ): Response<AuthResponse>

    @Headers("Content-Type:application/json")
    @GET("buyer/verify")
    suspend fun getVerifyToken(
        @Header("Authorization") token: String,
        @Query("tokenVerify") tokenVerify: String
    ): Response<StatusResponse>

    @FormUrlEncoded
    @POST("buyer/verify")
    suspend fun postResubmitVerifyToken(
        @Header("Authorization") token: String
    ): Response<StatusResponse>

    @Headers("Content-Type:application/json")
    @POST("buyer/cart/{idUser}")
    suspend fun postProductToCart(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String,
        @Body product: ItemCart
    ): Response<StatusResponse>

    @Multipart
    @POST("buyer/account")
    suspend fun postBiodataBuyer(
        @Header("Authorization") token: String,
        @Part imgKtpBuyer: MultipartBody.Part? = null,
        @Part imgSelfBuyer: MultipartBody.Part,
        @Part("idBuyerAccount") idBuyerAccount: RequestBody,
        @Part("nameBuyer") nameBuyer: RequestBody,
        @Part("noTelpBuyer") noTelpBuyer: RequestBody,
        @Part("postalCode") postalCode: RequestBody?,
        @Part("addressBuyer") addressBuyer: RequestBody,
    ): Response<StatusResponse>

    @Headers("Content-Type:application/json")
    @GET("buyer/account/detail/{idUser}")
    suspend fun getBiodataBuyer(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String
    ): Response<BiodataResponse>

    @Headers("Content-Type:application/json")
    @POST("buyer/order")
    suspend fun postOneOrder(
        @Header("Authorization") token: String,
        @Body oneOrderRequest: OrderRequest
    ): Response<StatusResponse>
}

