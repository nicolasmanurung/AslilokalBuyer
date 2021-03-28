package com.aslilokal.buyer.ui.pesanan

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.OrderResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class PesananViewModel(private var mainRepository: AslilokalRepository) : ViewModel() {
    val orders: MutableLiveData<Resource<OrderResponse>> = MutableLiveData()
    var orderResponse: OrderResponse? = null

    suspend fun getPesanan(
        token: String,
        idUser: String,
        status: String
    ) = viewModelScope.launch {
        orders.postValue(Resource.Loading())
        try {
            val response = mainRepository.getOrder(token, idUser, status)
            orders.postValue(handleOrderResponse(response))
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> orders.postValue(Resource.Error("Jaringan lemah"))
                else -> orders.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleOrderResponse(response: Response<OrderResponse>): Resource<OrderResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { resultOrder ->
                if (orderResponse == null) {
                    orderResponse = resultOrder
                } else {
                    val oldOrders = orderResponse?.result
                    val newOrders = resultOrder.result
                    if (!(newOrders.isNullOrEmpty()) && newOrders != oldOrders) {
                        oldOrders?.clear()
                        oldOrders?.addAll(newOrders)
                    }
                }
                return Resource.Success(orderResponse ?: resultOrder)
            }
        }
        return Resource.Error(response.message())
    }
}