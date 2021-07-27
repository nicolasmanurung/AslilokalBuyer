package com.aslilokal.buyer.ui.shop

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.ListProductResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import okio.IOException

class ShopViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val products: MutableLiveData<Resource<ListProductResponse>> = MutableLiveData()
    var productResponse: ListProductResponse? = null

    suspend fun getProductShopByIdShop(
        idSeller: String
    ) = viewModelScope.launch {
        products.postValue(Resource.Loading())
        try {
            val response = mainRepository.getProductsByIdShop(idSeller)
            if (response.body()?.success == true) {
                response.body()?.let { resultResponse ->
                    if (productResponse == null) {
                        productResponse = resultResponse
                    }
                    products.postValue(Resource.Success(productResponse ?: resultResponse))
                }
            } else {
                products.postValue(Resource.Error(response.message()))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> products.postValue(Resource.Error("Jaringan lemah"))
                else -> products.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    suspend fun getProductsShopByName(
        shopId: String,
        nameProduct: String
    ) = viewModelScope.launch {
        products.postValue(Resource.Loading())
        try {
            val response = mainRepository.getProductShopByName(shopId, nameProduct)
            if (response.body()?.success == true) {
                products.value = null
                productResponse = null
                response.body()?.let { resultResponse ->
                    if (productResponse == null) {
                        productResponse = resultResponse
                    }
                    products.value = Resource.Success(productResponse ?: resultResponse)
                }
            } else {
                products.postValue(Resource.Error(response.message()))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> products.postValue(Resource.Error("Jaringan lemah"))
                else -> products.postValue(Resource.Error("Conversion Error"))
            }
        }
    }
}