package com.aslilokal.buyer.ui.keranjang

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.CartBuyerResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class KeranjangViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val cartBuyers: MutableLiveData<Resource<CartBuyerResponse>> = MutableLiveData()
    var cartBuyerResponse: CartBuyerResponse? = null

    suspend fun getCartBuyer(
        token: String,
        idUser: String
    ) = viewModelScope.launch {
        cartBuyers.postValue(Resource.Loading())
        try {
            val response = mainRepository.getCartBuyer(token, idUser)
            cartBuyers.postValue(handleCartResponse(response))

        } catch (exception: Exception) {
            when (exception) {
                is IOException -> cartBuyers.postValue(Resource.Error("Jaringan lemah"))
                else -> cartBuyers.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    private fun handleCartResponse(response: Response<CartBuyerResponse>): Resource<CartBuyerResponse> {
        if (response.body()?.success == true) {
            response.body()?.let { resultCart ->
                if (cartBuyerResponse == null) {
                    cartBuyerResponse = resultCart
                } else {
                    val oldCart = cartBuyerResponse?.result
                    val newCart = resultCart.result
                    if (!(newCart.isNullOrEmpty()) && newCart != oldCart) {
                        oldCart?.clear()
                        oldCart?.addAll(newCart)
                    }
                }
                return Resource.Success(cartBuyerResponse ?: resultCart)
            }
        }
        return Resource.Error(response.message())
    }

}