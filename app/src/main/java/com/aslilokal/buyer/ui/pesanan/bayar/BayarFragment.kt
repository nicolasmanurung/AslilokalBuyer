package com.aslilokal.buyer.ui.pesanan.bayar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.aslilokal.buyer.databinding.FragmentBayarBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.ui.adapter.PesananAdapter
import com.aslilokal.buyer.ui.pesanan.PesananViewModel
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BayarFragment : Fragment() {

    private var _binding: FragmentBayarBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PesananViewModel
    private lateinit var username: String
    private lateinit var token: String
    private lateinit var orderAdapter: PesananAdapter
    private lateinit var datastore: AslilokalDataStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBayarBinding.inflate(inflater, container, false)

        datastore = AslilokalDataStore(binding.root.context)
        hideEmpty()
        setupViewModel()
        setupRecycler()

        runBlocking {
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()
            getData()
            setupObserver()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getPesananBayar(token, username)
            }
            setupObserver()
        }

        return binding.root
    }

    private fun getData() {
        showProgressBar()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getPesananBayar(token, username)
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(PesananViewModel::class.java)
    }

    private fun setupRecycler() {
        orderAdapter = PesananAdapter()
        binding.rvBayar.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupObserver() {
        viewModel.orders.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.result.let { orderResponse ->
                        orderAdapter.differ.submitList(
                            orderResponse?.toList()?.sortedByDescending { it.orderAt })
                        Log.d("RESULT", "Counting")
                        if (orderResponse != null) {
                            Log.d("ISNOTNULL", "Counting")
                            if (orderResponse.toList().isEmpty()) {
                                Log.d("EMPTY", "Counting")
                                showEmpty()
                            } else {
                                hideEmpty()
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    hideEmpty()
                    response.message?.let { message ->
                        Toast.makeText(
                            activity,
                            "Jaringanmu lemah, coba refresh...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                    hideEmpty()
                }
            }
        })
    }

    private fun showProgressBar() {
        binding.swipeRefresh.isRefreshing = true
    }

    private fun hideProgressBar() {
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showEmpty() {
        binding.lnrEmpty.visibility = View.VISIBLE
    }

    private fun hideEmpty() {
        binding.lnrEmpty.visibility = View.GONE
    }
}