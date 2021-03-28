package com.aslilokal.buyer.ui.pembayaran

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.response.BiodataResponse
import com.aslilokal.buyer.model.remote.response.ShopDetailResponse
import com.aslilokal.buyer.model.remote.response.StatusResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import java.io.IOException

class PembayaranViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val buyerBiodatas: MutableLiveData<Resource<BiodataResponse>> = MutableLiveData()
    var buyerBiodataResponse: BiodataResponse? = null

    val sellerBiodatas: MutableLiveData<Resource<ShopDetailResponse>> = MutableLiveData()
    var sellerBiodataResponse: ShopDetailResponse? = null

    val orders: MutableLiveData<Resource<StatusResponse>> = MutableLiveData()
    var orderResponse: StatusResponse? = null

    suspend fun getBuyerBiodata(
        token: String,
        username: String
    ) = viewModelScope.launch {
        buyerBiodatas.postValue(Resource.Loading())
        try {
            val response = mainRepository.getBiodataBuyer(token, username)
            if (response.isSuccessful) {
                response.body()?.let { biodataResult ->
                    if (buyerBiodataResponse == null) {
                        buyerBiodataResponse = biodataResult
                    }
                    buyerBiodatas.postValue(Resource.Success(buyerBiodataResponse ?: biodataResult))
                }
            } else {
                buyerBiodatas.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> buyerBiodatas.postValue(Resource.Error("Jaringan lemah"))
                else -> buyerBiodatas.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    suspend fun getSellerBiodata(
        idShop: String
    ) = viewModelScope.launch {
        sellerBiodatas.postValue(Resource.Loading())
        try {
            val response = mainRepository.getDetailShop(idShop)
            if (response.isSuccessful) {
                response.body()?.let { shopResult ->
                    if (sellerBiodataResponse == null) {
                        sellerBiodataResponse = shopResult
                    }
                    sellerBiodatas.postValue(Resource.Success(sellerBiodataResponse ?: shopResult))
                }
            } else {
                sellerBiodatas.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> sellerBiodatas.postValue(Resource.Error("Jaringan lemah"))
                else -> sellerBiodatas.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    suspend fun postOneOrder(
        token: String,
        oneOrderRequest: OrderRequest
    ) = viewModelScope.launch {
        orders.postValue(Resource.Loading())
        try {
            val response = mainRepository.postOneOrder(token, oneOrderRequest)
            if (response.isSuccessful) {
                response.body()?.let { statusResponse ->
                    if (orderResponse == null) {
                        orderResponse = statusResponse
                    }
                    orders.postValue(Resource.Success(orderResponse ?: statusResponse))
                }
            } else {
                orders.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> orders.postValue(Resource.Error("Jaringan lemah"))
                else -> orders.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

}