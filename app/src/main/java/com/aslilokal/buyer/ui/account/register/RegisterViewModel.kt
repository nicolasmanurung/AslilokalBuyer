package com.aslilokal.buyer.ui.account.register

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.model.remote.response.AuthResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import java.io.IOException

class RegisterViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val registers: MutableLiveData<Resource<AuthResponse>> = MutableLiveData()
    var registerResponse: AuthResponse? = null

    suspend fun postRegisterRequest(
        authRequest: AuthRequest
    ) = viewModelScope.launch {
        registers.postValue(Resource.Loading())
        try {
            val response = mainRepository.postRegisterBuyer(authRequest)
            if (response.isSuccessful) {
                response.body()?.let { loginResult ->
                    if (registerResponse == null) {
                        registerResponse = loginResult
                    }
                    registers.postValue(Resource.Success(registerResponse ?: loginResult))
                }
            } else {
                registers.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> registers.postValue(Resource.Error("Jaringan lemah"))
                else -> registers.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }
}