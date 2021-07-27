package com.aslilokal.buyer.ui.profil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.buyer.NOTIFICATION_TOPIC
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.FragmentProfilBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.response.DetailBiodata
import com.aslilokal.buyer.ui.account.login.LoginActivity
import com.aslilokal.buyer.ui.profil.edit.EditProfileBuyerActivity
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_USR_URL
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProfilFragment : Fragment() {
    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var datastore: AslilokalDataStore
    private lateinit var viewmodel: ProfilViewModel
    private lateinit var username: String
    private lateinit var token: String
    private var currentDataBuyer: DetailBiodata? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)

        showShimmer()

        datastore = AslilokalDataStore(binding.root.context)

        setupViewModel()
        setupProfileObservable()
        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()
            viewmodel.getBiodata(token, username)
        }

        binding.ubahTxt.setOnClickListener {
            if (currentDataBuyer != null) {
                val intent = Intent(activity, EditProfileBuyerActivity::class.java)
                intent.putExtra("currentDataBuyer", currentDataBuyer)
                startActivity(intent)
            }
        }

        binding.buttonLogout.setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                datastore.clearAll()
                Glide.get(binding.root.context).clearDiskCache()
                val finalTopic = "$NOTIFICATION_TOPIC$username"
                Log.d("FINALTOPIC", finalTopic)
                FirebaseMessaging.getInstance().unsubscribeFromTopic(finalTopic)
            }

            binding.root.context.startActivity(
                Intent(
                    binding.root.context,
                    LoginActivity::class.java
                )
            )
            activity?.finish()
        }

        return binding.root
    }

    private fun setupViewModel() {
        viewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(ProfilViewModel::class.java)
    }

    private fun setupProfileObservable() {
        viewmodel.biodatas.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { biodataResponse ->
                        if (biodataResponse?.success == false) {
                            hideShimmer()
                            Toast.makeText(
                                binding.root.context,
                                biodataResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (biodataResponse?.success == true) {
                            hideShimmer()
                            setupDataBuyer(biodataResponse.result)
                        }
                    }
                }

                is Resource.Loading -> {
                    showShimmer()
                }

                is Resource.Error -> {
                    hideShimmer()
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }


    private fun setupDataBuyer(biodataResponse: DetailBiodata) {
        currentDataBuyer = biodataResponse
        binding.txtNameBuyer.text = biodataResponse.nameBuyer
        binding.txtAddress.text = biodataResponse.addressBuyer
        binding.txtNoTelpBuyer.text = biodataResponse.noTelpBuyer

        Glide.with(binding.root)
            .load(BUCKET_USR_URL + biodataResponse.imgSelfBuyer)
            .placeholder(R.drawable.loading_animation)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .priority(Priority.HIGH)
            .into(binding.imgProfileBuyer)
    }

    private fun showShimmer() {
        binding.shimmerViewContainer.showShimmer(true)
        binding.shimmerViewContainer.startShimmer()
    }

    private fun hideShimmer() {
        binding.shimmerViewContainer.stopShimmer()
        binding.shimmerViewContainer.hideShimmer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        showShimmer()
        viewLifecycleOwner.lifecycleScope.launch {
            viewmodel.getBiodata(token, username)
        }
    }
}