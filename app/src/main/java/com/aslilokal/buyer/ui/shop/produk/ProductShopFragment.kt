package com.aslilokal.buyer.ui.shop.produk

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.aslilokal.buyer.databinding.FragmentProductShopBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.ui.adapter.ProductGridAdapter
import com.aslilokal.buyer.ui.shop.DetailShopActivity
import com.aslilokal.buyer.ui.shop.ShopViewModel
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.runBlocking

class ProductShopFragment : Fragment() {
    private var _binding: FragmentProductShopBinding? = null
    private val binding get() = _binding!!

    private lateinit var productGridAdapter: ProductGridAdapter
    private lateinit var detailShopActivity: DetailShopActivity
    private lateinit var shopViewmodel: ShopViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductShopBinding.inflate(inflater, container, false)

        detailShopActivity = activity as DetailShopActivity
        setupRvAllProducts()
        setupShopViewModel()
        setupAllProductsObserver()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        runBlocking {
            shopViewmodel.getProductShopByIdShop(detailShopActivity.idShop)
        }
    }

    private fun setupRvAllProducts() {
        productGridAdapter = ProductGridAdapter()
        binding.rvAllProductShop.apply {
            adapter = productGridAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2)
        }
    }

    private fun setupShopViewModel() {
        shopViewmodel = activity?.let {
            ViewModelProvider(
                it,
                AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
            ).get(ShopViewModel::class.java)
        }!!
    }

    private fun setupAllProductsObserver() {
        shopViewmodel.products.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    Log.d("PANGGILSUCCES", "excecute")
                    detailShopActivity.hideLoadingProgress()
                    response.data.let { productsResponse ->
                        if (productsResponse?.success == true) {
                            if (productsResponse.result.toList().isNotEmpty()) {
                                productGridAdapter.differ.submitList(productsResponse.result.toList())
                                hideEmpty()
                            } else {
                                showEmpty()
                            }
                        } else if (productsResponse?.success == false) {
                            Toast.makeText(
                                binding.root.context,
                                "Kesalahan tak terduga",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                is Resource.Loading -> {
                    Log.d("PANGGILLOADING", "excecute")
                    detailShopActivity.showLoadingProgress()
                }

                is Resource.Error -> {
                    Log.d("PANGGILLOADING", "excecute")
                    detailShopActivity.hideLoadingProgress()
                }
            }
        })
    }

    private fun showEmpty() {
        binding.lnrEmpty.visibility = View.VISIBLE
        binding.rvAllProductShop.visibility = View.GONE
    }

    private fun hideEmpty() {
        binding.lnrEmpty.visibility = View.GONE
        binding.rvAllProductShop.visibility = View.VISIBLE
    }
}