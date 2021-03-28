package com.aslilokal.buyer.ui.beranda

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.ProductResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class BerandaViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val products: MutableLiveData<Resource<ProductResponse>> = MutableLiveData()
    var productPage = 1
    var productResponse: ProductResponse? = null

    fun getProducts() = viewModelScope.launch {
        breakingProductCall()
    }

    private suspend fun breakingProductCall() {
        products.postValue(Resource.Loading())
        try {
            val response = mainRepository.getAllPopularProduct(
                productPage,
                5
            )
            products.postValue(handleProductResponse(response))
        } catch (t: Throwable) {
            when (t) {
                is IOException -> products.postValue(Resource.Error("Jaringan lemah"))
                else -> products.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private fun handleProductResponse(response: Response<ProductResponse>): Resource<ProductResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { resultResponse ->
                productPage++
                if (productResponse == null) {
                    productResponse = resultResponse
                } else {
                    val oldProducts = productResponse?.result?.docs
                    val newProducts = resultResponse.result.docs
                    if (newProducts != null) {
                        oldProducts?.addAll(newProducts)
                    }
                }
                return Resource.Success(productResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }
}