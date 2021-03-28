package com.aslilokal.buyer.ui.aslilokal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.ListProductResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import okio.IOException

class AslilokalViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val productsByUmkm: MutableLiveData<Resource<List<ListProductResponse>>> =
        MutableLiveData()

    fun getAllProducts(firstUmkm: String, secondUmkm: String, thirdUmkm: String) =
        viewModelScope.launch {
            breakingProductCall(firstUmkm, secondUmkm, thirdUmkm)
        }

    private suspend fun breakingProductCall(
        firstUmkm: String,
        secondUmkm: String,
        thirdUmkm: String
    ) {
        productsByUmkm.postValue(Resource.Loading())
        try {
            supervisorScope {
                val umkmProduct1 =
                    async { mainRepository.getProductCategorizeByUmkm(firstUmkm) }.await()
                val umkmProduct2 =
                    async { mainRepository.getProductCategorizeByUmkm(secondUmkm) }.await()
                val umkmProduct3 =
                    async { mainRepository.getProductCategorizeByUmkm(thirdUmkm) }.await()

                //define all variable contain all this...
                val allUmkmData = mutableListOf<ListProductResponse>()
                umkmProduct1.body()?.let { allUmkmData.add(it) }
                umkmProduct2.body()?.let { allUmkmData.add(it) }
                umkmProduct3.body()?.let { allUmkmData.add(it) }

                productsByUmkm.postValue(
                    Resource.Success(
                        allUmkmData
                    )
                )
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> productsByUmkm.postValue(Resource.Error("Jaringan lemah"))
                else -> productsByUmkm.postValue(Resource.Error("Ada kesalahan"))
            }
        }
    }
}