package com.aslilokal.buyer.ui.keranjang

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.FragmentKeranjangBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.response.ItemCart
import com.aslilokal.buyer.ui.adapter.CartAdapter
import com.aslilokal.buyer.ui.pembayaran.detailpembayaran.DetailPembayaranActivity
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.CustomFunctions
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.runBlocking

class KeranjangFragment : Fragment() {
    private var _binding: FragmentKeranjangBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: KeranjangViewModel
    private lateinit var keranjangAdapter: CartAdapter
    private lateinit var username: String
    private lateinit var token: String
    private lateinit var datastore: AslilokalDataStore
    private var listCartCheck = ArrayList<ItemCart>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKeranjangBinding.inflate(inflater, container, false)

        datastore = AslilokalDataStore(binding.root.context)
        showLoading()
        setupViewModel()
        setupRecycler()

        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()

            viewModel.getCartBuyer(token, username)
            setupObserver()
        }

        keranjangAdapter.onAddItem = { data, value ->
            listCartCheck.mapInPlace { if (it._id == data) getItemCart(it, value) else it }
            Log.d("SUMVALUECURRENT", listCartCheck.toString())
            refreshLnrData()
        }

        keranjangAdapter.onSubItem = { data, value ->
            listCartCheck.mapInPlace { if (it._id == data) getItemCart(it, value) else it }
            Log.d("SUMVALUECURRENT", data)
            refreshLnrData()
        }

        keranjangAdapter.onCheckItem = { data, status ->
            if (status) {
                Log.d("STATUSCHECK", data.toString())
                listCartCheck.add(data)
                refreshLnrData()
            } else {
                listCartCheck.remove(data)
                refreshLnrData()
            }
        }

        binding.btnBayar.setOnClickListener {
            var intent = Intent(activity, DetailPembayaranActivity::class.java)
            intent.putParcelableArrayListExtra("LISTPRODUCT", listCartCheck)
            startActivity(intent)
        }

        return binding.root
    }

    private fun getItemCart(data: ItemCart, qtyValue: Int): ItemCart {
        val newItemCart = data
        newItemCart.qtyProduct = qtyValue
        return newItemCart
    }

    private fun refreshLnrData() {
        binding.lnrSumValue.visibility = View.VISIBLE
        if (listCartCheck.size > 0) {
            var tempSumData = 0
            var differentSellerCount = 0
            for (item in listCartCheck) {
                tempSumData += item.productPrice * item.qtyProduct
                if (item.idSellerAccount != listCartCheck.first().idSellerAccount) {
                    differentSellerCount++
                }
                if (differentSellerCount != 0) {
                    binding.btnBayar.isEnabled = false
                    binding.txtDifferentShopAlert.visibility = View.VISIBLE
                    binding.btnBayar.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.text_shadow
                        )
                    )
                } else {
                    binding.btnBayar.isEnabled = true
                    binding.txtDifferentShopAlert.visibility = View.GONE
                    binding.btnBayar.setBackgroundColor(
                        ContextCompat.getColor(
                            binding.root.context,
                            R.color.primaryColor
                        )
                    )
                }
            }
            Log.d("differentSellerCount", differentSellerCount.toString())
            binding.txtValueSumPayment.text = CustomFunctions().formatRupiah(tempSumData.toDouble())
        } else {
            binding.lnrSumValue.visibility = View.GONE
        }

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(KeranjangViewModel::class.java)
    }

    private fun setupRecycler() {
        keranjangAdapter = CartAdapter()
        binding.rvKeranjang.apply {
            adapter = keranjangAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupObserver() {
        viewModel.cartBuyers.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideLoading()
                    response.data?.result.let { cartResponse ->
                        keranjangAdapter.differ.submitList(cartResponse?.toList())
                        if (cartResponse != null) {
                            if (cartResponse.toList().isEmpty()) {
                                showEmpty()
                            } else {
                                hideEmpty()
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    hideLoading()
                }

                is Resource.Loading -> {
                    showLoading()
                }

            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun hideLoading() {
        binding.llProgressBar.progressbar.visibility = View.GONE
    }

    private fun showLoading() {
        binding.lnrSumValue.visibility = View.GONE
        binding.llProgressBar.progressbar.visibility = View.VISIBLE

    }

    private fun showEmpty() {
        binding.lnrEmpty.visibility = View.VISIBLE
    }

    private fun hideEmpty() {
        binding.lnrEmpty.visibility = View.GONE
    }

    inline fun <T> MutableList<T>.mapInPlace(mutator: (T) -> T) {
        val iterate = this.listIterator()
        while (iterate.hasNext()) {
            val oldValue = iterate.next()
            val newValue = mutator(oldValue)
            if (newValue !== oldValue) {
                iterate.set(newValue)
            }
        }
    }
}

