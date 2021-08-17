package com.aslilokal.buyer.model.data.api

import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.model.remote.request.BiodataRequest
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Query

interface AslilokalAPI {
    @Headers("Content-Type:application/json")
    @GET("buyer/products/popular")
    suspend fun getAllPopularProduct(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ProductResponse>

    @Headers("Content-Type:application/json")
    @GET("buyer/products")
    suspend fun getProductByCategorize(
        @Query("type") type: String,
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
    @GET("buyer/cart/live/{idUser}")
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

    @POST("buyer/verify")
    suspend fun postResubmitVerifyToken(
        @Header("Authorization") token: String,
        @Body emptyRequest: Any = Object()
    ): Response<StatusResponse>

    @FormUrlEncoded
    @POST("buyer/cart/live/{idUser}")
    suspend fun postProductToCart(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String,
        @Field("idProduct") idProduct: String
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
        @Part("addressBuyer") addressBuyer: RequestBody,
        @Part("postalCodeInput") postalCodeInput: RequestBody,
        @Part("nameAcceptPackage") nameAcceptPackage: RequestBody,
        // test
        @Part("rajaOngkir[city_id]") cityId: RequestBody,
        @Part("rajaOngkir[province_id]") provinceId: RequestBody,
        @Part("rajaOngkir[province]") provinceName: RequestBody,
        @Part("rajaOngkir[city_name]") cityName: RequestBody,
        @Part("rajaOngkir[postal_code]") postalCode: RequestBody
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

    @GET("starter/city")
    suspend fun ROGetCities(
        @Header("key") key: String
    ): Response<ROCityResponse>

    @FormUrlEncoded
    @POST("starter/cost")
    suspend fun ROPostCost(
        @Header("key") key: String,
        @Field("origin") origin: String,
        @Field("destination") destination: String,
        @Field("weight") weight: String,
        @Field("courier") courier: String
    ): Response<ROCostResponse>

    @GET("buyer/vouchers/shop/{idUser}")
    suspend fun getAllVouchersByBuyer(
        @Path("idUser") idUser: String
    ): Response<VoucherResponse>

    @FormUrlEncoded
    @HTTP(method = "DELETE", path = "buyer/cart/live/{idUser}", hasBody = true)
    suspend fun deleteProductsFromCart(
        @Header("Authorization") key: String,
        @Path("idUser") idUser: String,
        @Field("idProduct") idProduct: String
    ): Response<StatusResponse>

    @GET("buyer/order/detail/{idOrder}")
    suspend fun getDetailOrder(
        @Header("Authorization") key: String,
        @Path("idOrder") idOrder: String
    ): Response<DetailOrderResponse>

    @Multipart
    @POST("buyer/order/attachment/{idOrder}")
    suspend fun postAttachmentOrderImg(
        @Header("Authorization") key: String,
        @Path("idOrder") idOrder: String,
        @Part orderAttachmentImg: MultipartBody.Part
    ): Response<StatusResponse>

    @Multipart
    @PUT("buyer/order/attachment/{idOrder}")
    suspend fun putAttachmentOrderImg(
        @Header("Authorization") key: String,
        @Path("idOrder") idOrder: String,
        @Part("imgKey") imgKey: RequestBody,
        @Part orderAttachmentImg: MultipartBody.Part
    ): Response<StatusResponse>

    @GET("buyer/products/shop")
    suspend fun getProductsByIdShop(
        @Query("shop") shopId: String
    ): Response<ListProductResponse>

    @GET("buyer/products/shop/search")
    suspend fun getProductShopByName(
        @Query("shop") shopId: String,
        @Query("name") nameProduct: String
    ): Response<ListProductResponse>

    @GET("buyer/products/search")
    suspend fun getSearchProductsByName(
        @Query("name") name: String,
        @Query("type") type: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ProductResponse>

    @GET("buyer/shops/search")
    suspend fun getSearchShopByName(
        @Query("name") name: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<ShopListResponse>

    @Headers("Content-Type:application/json")
    @PUT("buyer/account/detail/{idUser}")
    suspend fun putBuyerBiodata(
        @Header("Authorization") token: String,
        @Path("idUser") idUser: String,
        @Body buyerBiodata: BiodataRequest
    ): Response<StatusResponse>

    @Multipart
    @PUT("buyer/update/imgself")
    suspend fun putUpdateSelfImg(
        @Header("Authorization") token: String,
        @Part("imgKey") imgKey: RequestBody,
        @Part imgSelfBuyerUpdate: MultipartBody.Part
    ): Response<StatusResponse>
}

