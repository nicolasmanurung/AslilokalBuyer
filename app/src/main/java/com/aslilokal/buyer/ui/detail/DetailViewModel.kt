package com.aslilokal.buyer.ui.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.ItemCart
import com.aslilokal.buyer.model.remote.response.OneProductResponse
import com.aslilokal.buyer.model.remote.response.ShopDetailResponse
import com.aslilokal.buyer.model.remote.response.StatusResponse
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
        idUser:String,
        product: ItemCart
    ) = viewModelScope.launch {
        cart.postValue(Resource.Loading())
        try {
            val response = mainRepository.postProductToCart(token,idUser, product)
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
                is java.io.IOException -> cart.postValue(Resource.Error("Jaringan lemah"))
                else -> cart.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }
}