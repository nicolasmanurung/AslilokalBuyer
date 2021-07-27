package com.aslilokal.buyer.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.ROCityResponse
import com.aslilokal.buyer.model.remote.response.ROCostResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.io.IOException

class ROViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val citiesResults: MutableLiveData<Resource<ROCityResponse>> = MutableLiveData()
    var citiesResponse: ROCityResponse? = null

    val costResults: MutableLiveData<Resource<List<ROCostResponse>>> = MutableLiveData()
    var costResponse: ROCostResponse? = null

    suspend fun getCitiesByRO(key: String) = viewModelScope.launch {
        citiesResults.postValue(Resource.Loading())
        try {
            val response = mainRepository.getCitiesRO(key)
            if (response.body()?.rajaongkir?.status?.code == 200) {
                response.body()?.let { citiesResult ->
                    if (citiesResponse == null) {
                        citiesResponse = citiesResult
                    }
                    citiesResults.postValue(Resource.Success(citiesResponse ?: citiesResult))
                }
            } else {
                citiesResults.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> citiesResults.postValue(Resource.Error("Jaringan lemah"))
                else -> citiesResults.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    suspend fun postCostRO(
        key: String,
        origin: String,
        destination: String,
        weight: String
    ) = viewModelScope.launch {
        costResults.postValue(Resource.Loading())
        try {
            supervisorScope {
                val jneResponse = async {
                    mainRepository.postCostRO(
                        key,
                        origin,
                        destination,
                        weight,
                        "jne"
                    )
                }.await()

                val posResponse = async {
                    mainRepository.postCostRO(
                        key,
                        origin,
                        destination,
                        weight,
                        "pos"
                    )
                }.await()

                val tikiResponse = async {
                    mainRepository.postCostRO(
                        key,
                        origin,
                        destination,
                        weight,
                        "tiki"
                    )
                }.await()

                val allShipmentResponseData = mutableListOf<ROCostResponse>()
                jneResponse.body().let { it?.let { jne -> allShipmentResponseData.add(jne) } }
                posResponse.body().let { it?.let { pos -> allShipmentResponseData.add(pos) } }
                tikiResponse.body().let { it?.let { tiki -> allShipmentResponseData.add(tiki) } }

                costResults.postValue(Resource.Success(allShipmentResponseData))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> costResults.postValue(Resource.Error("Jaringan lemah"))
                else -> costResults.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }
}