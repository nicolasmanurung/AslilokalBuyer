package com.aslilokal.buyer.ui.account.verify

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.model.remote.response.StatusResponse
import com.aslilokal.buyer.utils.Resource
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.IOException

class VerificationViewModel(private val mainRepository: AslilokalRepository) : ViewModel() {
    val emailVerivications: MutableLiveData<Resource<StatusResponse>> = MutableLiveData()
    var emailVerificationResponse: StatusResponse? = null

    val tokenResubmits: MutableLiveData<Resource<StatusResponse>> = MutableLiveData()
    var tokenResubmitResponse: StatusResponse? = null

    val biodataResults: MutableLiveData<Resource<StatusResponse>> = MutableLiveData()
    var biodataResponse: StatusResponse? = null


    suspend fun getVerifyTokenCode(
        token: String,
        tokenVerify: String
    ) = viewModelScope.launch {
        emailVerivications.postValue(Resource.Loading())
        try {
            val response = mainRepository.getTokenVerify(token, tokenVerify)
            if (response.isSuccessful) {
                response.body()?.let { tokenVerifyResult ->
                    if (emailVerificationResponse == null) {
                        emailVerificationResponse = tokenVerifyResult
                    }
                    emailVerivications.postValue(
                        Resource.Success(
                            emailVerificationResponse ?: tokenVerifyResult
                        )
                    )
                }
            } else {
                emailVerivications.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> emailVerivications.postValue(Resource.Error("Jaringan lemah"))
                else -> emailVerivications.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    suspend fun postResubmitSendTokenVerify(
        token: String
    ) = viewModelScope.launch {
        tokenResubmits.postValue(Resource.Loading())
        try {
            val response = mainRepository.postTokenVerifyResubmit(token)
            if (response.isSuccessful) {
                response.body()?.let { tokenVerifyResult ->
                    if (tokenResubmitResponse == null) {
                        tokenResubmitResponse = tokenVerifyResult
                    }
                    tokenResubmits.postValue(
                        Resource.Success(
                            tokenResubmitResponse ?: tokenVerifyResult
                        )
                    )
                }
            } else {
                tokenResubmits.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> tokenResubmits.postValue(Resource.Error("Jaringan lemah"))
                else -> tokenResubmits.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

    suspend fun postBiodataBuyer(
        token: String,
        imgKtpBuyer: MultipartBody.Part?,
        imgSelfBuyer: MultipartBody.Part,
        idBuyerAccount: RequestBody,
        nameBuyer: RequestBody,
        noTelpBuyer: RequestBody,
        addressBuyer: RequestBody,
        postalCodeInput: RequestBody,
        nameAcceptPackage: RequestBody,
        cityId: RequestBody,
        provinceId: RequestBody,
        provinceName: RequestBody,
        cityName: RequestBody,
        postalCode: RequestBody
    ) = viewModelScope.launch {
        biodataResults.postValue(Resource.Loading())
        try {
            val response = mainRepository.postSellerBiodata(
                token,
                imgKtpBuyer,
                imgSelfBuyer,
                idBuyerAccount,
                nameBuyer,
                noTelpBuyer,
                addressBuyer,
                postalCodeInput,
                nameAcceptPackage,
                cityId, provinceId, provinceName, cityName, postalCode
            )
            if (response.isSuccessful) {
                Log.d("VMSUCCESS", "true")
                response.body()?.let { tokenVerifyResult ->
                    if (biodataResponse == null) {
                        biodataResponse = tokenVerifyResult
                    }
                    biodataResults.postValue(
                        Resource.Success(
                            biodataResponse ?: tokenVerifyResult
                        )
                    )
                }
            } else {
                Log.d("VMFAILED", "true")
                biodataResults.postValue(Resource.Error(response.message()))
            }
        } catch (exception: Exception) {
            when (exception) {
                is IOException -> biodataResults.postValue(Resource.Error("Jaringan lemah"))
                else -> biodataResults.postValue(Resource.Error("Kesalahan tak terduga"))
            }
        }
    }

}