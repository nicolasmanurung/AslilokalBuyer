package com.aslilokal.buyer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.repository.AslilokalRepository
import com.aslilokal.buyer.ui.account.login.LoginViewModel
import com.aslilokal.buyer.ui.account.register.RegisterViewModel
import com.aslilokal.buyer.ui.account.verify.VerificationViewModel
import com.aslilokal.buyer.ui.aslilokal.AslilokalViewModel
import com.aslilokal.buyer.ui.beranda.BerandaViewModel
import com.aslilokal.buyer.ui.detail.DetailViewModel
import com.aslilokal.buyer.ui.keranjang.KeranjangViewModel
import com.aslilokal.buyer.ui.pembayaran.PembayaranViewModel
import com.aslilokal.buyer.ui.pesanan.PesananViewModel
import com.aslilokal.buyer.ui.profil.ProfilViewModel

class AslilokalVMProviderFactory(private val apiHelper: ApiHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BerandaViewModel::class.java)) {
            return BerandaViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(AslilokalViewModel::class.java)) {
            return AslilokalViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(KeranjangViewModel::class.java)) {
            return KeranjangViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(PesananViewModel::class.java)) {
            return PesananViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            return RegisterViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(VerificationViewModel::class.java)) {
            return VerificationViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(PembayaranViewModel::class.java)) {
            return PembayaranViewModel(AslilokalRepository(apiHelper)) as T
        }
        if (modelClass.isAssignableFrom(ProfilViewModel::class.java)) {
            return ProfilViewModel(AslilokalRepository(apiHelper)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}