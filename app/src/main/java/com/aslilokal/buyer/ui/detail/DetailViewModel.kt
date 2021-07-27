package com.aslilokal.buyer.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.*
import com.aslilokal.buyer.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import okio.IOException

class DetailViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val product: MutableLiveData<Resource<OneProductResponse>> = MutableLiveData()
    private var productResponse: OneProductResponse? = null

    val shop: MutableLiveData<Resource<ShopDetailResponse>> = MutableLiveData()
    private var shopResponse: ShopDetailResponse? = null

    val cart: MutableLiveData<Resource<StatusResponse>> = MutableLiveData()
    private var cartResponse: StatusResponse? = null

    val allProducts: MutableLiveData<Resource<ProductResponse>> = MutableLiveData()
    private var allProductResponse: ProductResponse? = null
    var productPage = 1

    val detailOrders: MutableLiveData<Resource<DetailOrderResponse>> = MutableLiveData()
    private var detailOrderResponse: DetailOrderResponse? = null

    fun getProduct(idProduct: String) = viewModelScope.launch {
        callProductResponse(idProduct)
    }

    fun getShopDetail(idShop: String) = viewModelScope.launch {
        callShopResponse(idShop)
    }

    private suspend fun callProductResponse(idProduct: String) {
        product.postValue(Resource.Loading())
        try {
            val response = mainRepository.getOneDetailProduct(idProduct)
            if (response.body()?.success == true) {
                response.body()?.let { oneProductResponse ->
                    if (productResponse == null) {
                        productResponse = oneProductResponse
                        product.postValue(Resource.Success(oneProductResponse))
                    }
                }

            } else {
                product.postValue(Resource.Error(response.message()))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> product.postValue(Resource.Error("Jaringan lemah"))
                else -> product.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun callShopResponse(idShop: String) {
        shop.postValue(Resource.Loading())
        try {
            val response = mainRepository.getDetailShop(idShop)
            if (response.body()?.success == true) {
                response.body()?.let { oneShopResponse ->
                    if (shopResponse == null) {
                        shopResponse = oneShopResponse
                        shop.postValue(Resource.Success(oneShopResponse))
                    }
                }
            } else {
                shop.postValue(Resource.Error(response.message()))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> shop.postValue(Resource.Error("Jaringan lemah"))
                else -> shop.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    suspend fun postProductToCart(
        token: String,
        idUser: String,
        idProduct: String
    ) = viewModelScope.launch {
        cart.postValue(Resource.Loading())
        try {
            val response = mainRepository.postProductToCart(token, idUser, idProduct)
            if (response.isSuccessful) {
                response.body()?.let { cartResult ->
                    if (cartResponse == null) {
                        cartResponse = cartResult
                    }
                    cart.postValue(Resource.Success(cartResponse ?: cartResult))
                }
            } else {
                cart.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> cart.postValue(Resource.Error("Jaringan lemah"))
                else -> cart.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    suspend fun getAllProductByCategorize(
        type: String
    ) = viewModelScope.launch {
        allProducts.postValue(Resource.Loading())
        try {
            val response = mainRepository.getProductByCategorize(
                type, productPage,
                QUERY_PAGE_SIZE
            )
            if (response.isSuccessful) {
                response.body()?.let { productResult ->
                    productPage++
                    if (allProductResponse == null) {
                        allProductResponse = productResult
                    } else {
                        val oldProducts = allProductResponse?.result?.docs
                        val newProducts = productResult.result.docs
                        if (newProducts != null) {
                            oldProducts?.addAll(newProducts)
                        }
                    }
                    allProducts.postValue(Resource.Success(allProductResponse ?: productResult))
                }
            } else {
                allProducts.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> allProducts.postValue(Resource.Error("Jaringan lemah"))
                else -> allProducts.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    suspend fun getDetailProduct(
        token: String,
        idOrder: String
    ) = viewModelScope.launch {
        detailOrders.postValue(Resource.Loading())
        try {
            val response = mainRepository.getDetailOrder(token, idOrder)
            if (response.isSuccessful) {
                response.body()?.let { detailResult ->
                    if (detailOrderResponse == null) {
                        detailOrderResponse = detailResult
                    }
                    detailOrders.postValue(Resource.Success(detailOrderResponse ?: detailResult))
                }
            } else {
                detailOrders.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> detailOrders.postValue(Resource.Error("Jaringan lemah"))
                else -> detailOrders.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }
}