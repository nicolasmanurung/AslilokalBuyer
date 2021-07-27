package com.aslilokal.buyer.ui.profil.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.request.BiodataRequest
import com.aslilokal.buyer.model.remote.response.DetailBiodata
import com.aslilokal.buyer.model.remote.response.StatusResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import java.io.IOException

class EditProfileViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val putBiodatas: MutableLiveData<Resource<StatusResponse>> = MutableLiveData()
    var putBiodataResponse: StatusResponse? = null

    suspend fun putBuyerBiodata(
        token: String,
        idUser: String,
        biodata: BiodataRequest
    ) = viewModelScope.launch {
        putBiodatas.postValue(Resource.Loading())
        try {
            val response = mainRepository.putBuyerBiodata(token, idUser, biodata)
            if (response.body()?.success == true) {
                response.body()?.let { result ->
                    if (putBiodataResponse == null) {
                        putBiodataResponse = result
                    }
                    putBiodatas.postValue(Resource.Success(putBiodataResponse ?: result))
                }
            } else {
                putBiodatas.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> putBiodatas.postValue(Resource.Error("Jaringan lemah"))
                else -> putBiodatas.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

}