package com.aslilokal.buyer.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.ProductResponse
import com.aslilokal.buyer.model.remote.response.ShopListResponse
import com.aslilokal.buyer.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import okio.IOException

class SearchViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val products: MutableLiveData<Resource<ProductResponse>> = MutableLiveData()
    var productResponse: ProductResponse? = null
    var productPage = 1

    val shops: MutableLiveData<Resource<ShopListResponse>> = MutableLiveData()
    var shopResponse: ShopListResponse? = null
    var shopPage = 1

    suspend fun getSearchProductsByName(
        name: String
    ) = viewModelScope.launch {
        products.postValue(Resource.Loading())
        try {
            val response = mainRepository.getSearchProductsByName(
                name,
                type = "all",
                productPage,
                QUERY_PAGE_SIZE
            )
            if (response.isSuccessful) {
                response.body()?.let { productResult ->
                    productPage++
                    if (productResponse == null) {
                        productResponse = productResult
                    } else {
                        val oldProducts = productResponse?.result?.docs
                        val newProducts = productResult.result.docs
                        if (newProducts != null) {
                            oldProducts?.addAll(newProducts)
                        }
                    }
                    products.postValue(Resource.Success(productResponse ?: productResult))
                }
            } else {
                products.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> products.postValue(Resource.Error("Jaringan lemah"))
                else -> products.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    fun resetProductsShopsValue() {
        productResponse = null
        shopResponse = null
    }

    suspend fun getSearchShopByName(
        name: String
    ) = viewModelScope.launch {
        shops.postValue(Resource.Loading())
        try {
            val response = mainRepository.getSearchShopsByName(name, shopPage, QUERY_PAGE_SIZE)
            if (response.isSuccessful) {
                response.body()?.let { shopResult ->
                    shopPage++
                    if (shopResponse == null) {
                        shopResponse = shopResult
                    } else {
                        val oldShops = shopResponse?.result?.docs
                        val newShops = shopResult.result.docs
                        if (newShops != null) {
                            oldShops?.addAll(newShops)
                        }
                    }
                    shops.postValue(Resource.Success(shopResponse ?: shopResult))
                }
            } else {
                shops.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> shops.postValue(Resource.Error("Jaringan lemah"))
                else -> shops.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }
}