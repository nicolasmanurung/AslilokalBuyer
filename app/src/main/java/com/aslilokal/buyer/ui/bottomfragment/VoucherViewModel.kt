package com.aslilokal.buyer.ui.bottomfragment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.VoucherResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch

class VoucherViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val vouchers: MutableLiveData<Resource<VoucherResponse>> = MutableLiveData()
    private var voucherResponse: VoucherResponse? = null

    suspend fun getAllVoucherByShop(
        usernameShop: String
    ) = viewModelScope.launch {
        vouchers.postValue(Resource.Loading())
        try {
            val response = mainRepository.getAllVouchersByBuyer(usernameShop)
            if (response.isSuccessful) {
                response.body()?.let { voucher ->
                    if (voucherResponse == null) {
                        voucherResponse = voucher
                    }
                    vouchers.postValue(Resource.Success(voucherResponse ?: voucher))
                }
            } else {
                vouchers.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is java.io.IOException -> vouchers.postValue(Resource.Error("Jaringan lemah"))
                else -> vouchers.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

}