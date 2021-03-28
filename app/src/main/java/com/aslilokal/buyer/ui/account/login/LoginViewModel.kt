package com.aslilokal.buyer.ui.account.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.model.remote.response.AuthResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import java.io.IOException

class LoginViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val logins: MutableLiveData<Resource<AuthResponse>> = MutableLiveData()
    var loginResponse: AuthResponse? = null

    suspend fun postLoginRequest(
        authRequest: AuthRequest
    ) = viewModelScope.launch {
        logins.postValue(Resource.Loading())
        try {
            val response = mainRepository.postLoginBuyer(authRequest)
            if (response.isSuccessful) {
                response.body()?.let { loginResult ->
                    if (loginResponse == null) {
                        loginResponse = loginResult
                    }
                    logins.postValue(Resource.Success(loginResponse ?: loginResult))
                }
            } else {
                logins.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> logins.postValue(Resource.Error("Jaringan lemah"))
                else -> logins.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }
}