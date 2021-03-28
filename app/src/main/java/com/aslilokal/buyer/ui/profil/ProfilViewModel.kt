package com.aslilokal.buyer.ui.profil

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.BiodataResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import java.io.IOException

class ProfilViewModel(private var mainRepository: AslilokalRepository) : ViewModel() {
    val biodatas: MutableLiveData<Resource<BiodataResponse>> = MutableLiveData()
    var biodataResponse: BiodataResponse? = null

    suspend fun getBiodata(
        token: String,
        username: String
    ) = viewModelScope.launch {
        biodatas.postValue(Resource.Loading())
        try {
            val response = mainRepository.getBiodataBuyer(token, username)
            if (response.isSuccessful) {
                response.body()?.let { biodataResult ->
                    if (biodataResponse == null) {
                        biodataResponse = biodataResult
                    }
                    biodatas.postValue(Resource.Success(biodataResponse ?: biodataResult))
                }
            } else {
                biodatas.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> biodatas.postValue(Resource.Error("Jaringan lemah"))
                else -> biodatas.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }
}