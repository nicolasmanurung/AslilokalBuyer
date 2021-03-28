package com.aslilokal.buyer.ui.aslilokal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.buyer.databinding.FragmentAslilokalBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.response.ListProductResponse
import com.aslilokal.buyer.model.remote.response.Product
import com.aslilokal.buyer.ui.adapter.ProductLinearAdapter
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.launch

class AslilokalFragment : Fragment() {
    private var _binding: FragmentAslilokalBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AslilokalViewModel
    private lateinit var umkm1GridAdapter: ProductLinearAdapter
    private lateinit var umkm2GridAdapter: ProductLinearAdapter
    private lateinit var umkm3GridAdapter: ProductLinearAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAslilokalBinding.inflate(inflater, container, false)

        loadingResponse()
        setupViewModel()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAllProducts("taput", "tapteng", "toba")
        }

        setupRv()
        setupObserverUmkmLokalProduct()
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(AslilokalViewModel::class.java)
    }

    private fun setupRv() {
        umkm1GridAdapter = ProductLinearAdapter()
        binding.rv1Umkm.apply {
            adapter = umkm1GridAdapter
            layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        }

        umkm2GridAdapter = ProductLinearAdapter()
        binding.rv2Umkm.apply {
            adapter = umkm2GridAdapter
            layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        }

        umkm3GridAdapter = ProductLinearAdapter()
        binding.rv3Umkm.apply {
            adapter = umkm3GridAdapter
            layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupObserverUmkmLokalProduct() {
        viewModel.productsByUmkm.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { listProductResponse ->
                        if (!listProductResponse.isNullOrEmpty()) {
                            breakingDataObserver(listProductResponse)
                        }
                    }
                }

                is Resource.Error -> {
                    Toast.makeText(binding.root.context, "Kesalahan jaringan", Toast.LENGTH_SHORT)
                        .show()
                }

                is Resource.Loading -> {
                    loadingResponse()
                }

            }
        })
    }

    private fun breakingDataObserver(listProduct: List<ListProductResponse>) {
        val allProductData = ArrayList<Product>()
        for (item in listProduct) {
            allProductData.addAll(item.result)
        }

        // change taput into umkm lokal lainnya
        umkm1GridAdapter.differ.submitList(allProductData.filter { it.umkmTags == "taput" }
            .toList())
        umkm2GridAdapter.differ.submitList(allProductData.filter { it.umkmTags == "tapteng" }
            .toList())
        umkm3GridAdapter.differ.submitList(allProductData.filter { it.umkmTags == "toba" }
            .toList())

        successResponse()
    }

    private fun loadingResponse() {
        binding.rvSkeletonLayout.showShimmerAdapter()
    }

    private fun successResponse() {
        binding.rvSkeletonLayout.hideShimmerAdapter()
        binding.cardViewRv1.visibility = View.VISIBLE
        binding.cardViewRv2.visibility = View.VISIBLE
        binding.cardViewRv3.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}