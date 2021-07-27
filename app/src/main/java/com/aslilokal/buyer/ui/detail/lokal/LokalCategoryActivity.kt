package com.aslilokal.buyer.ui.detail.lokal

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.databinding.ActivityLokalCategoryBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.ui.adapter.ProductCategorizeAdapter
import com.aslilokal.buyer.ui.detail.DetailViewModel
import com.aslilokal.buyer.utils.Constants.Companion.QUERY_PAGE_SIZE
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.launch

class LokalCategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLokalCategoryBinding
    private lateinit var category: String
    private lateinit var viewmodel: DetailViewModel
    private lateinit var productAdapter: ProductCategorizeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLokalCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupRecyclerView()
        setupObserver()
        binding.lnrEmpty.visibility = View.GONE

        category = intent.getStringExtra("category")!!
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        when (category) {
            "sembako" -> {
                supportActionBar?.title = "Sembako"
            }
            "jasa" -> {
                supportActionBar?.title = "Jasa"
            }
            "kuliner" -> {
                supportActionBar?.title = "Kuliner"
            }
            "fashion" -> {
                supportActionBar?.title = "Fashion"
            }
        }

        lifecycleScope.launch {
            viewmodel.getAllProductByCategorize(category)
        }
    }

    private fun setupViewModel() {
        viewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(DetailViewModel::class.java)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductCategorizeAdapter()
        binding.rvProductByCategorize.apply {
            adapter = productAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
            addOnScrollListener(this@LokalCategoryActivity.scrollListener)
        }
    }

    private fun setupObserver() {
        viewmodel.allProducts.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    hideErrorMessage()
                    response.data?.result.let { productResponse ->
                        productAdapter.differ.submitList(productResponse?.docs?.toList())
                        if (productResponse?.docs?.toList()?.size!! <= 0) {
                            binding.lnrEmpty.visibility = View.VISIBLE
                        } else {
                            binding.lnrEmpty.visibility = View.GONE
                        }
                        val totalPages = productResponse.totalPages
                        isLastPage = (viewmodel.productPage - 1) == totalPages
                        Log.d(
                            "ISLASTPAGEVALUE",
                            "isLastPage: $isLastPage, totalPages: $totalPages, currentPage: " + viewmodel.productPage.toString()
                        )
                        if (isLastPage) {
                            binding.rvProductByCategorize.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Toast.makeText(this, "An error occured: $message", Toast.LENGTH_SHORT)
                            .show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                    hideErrorMessage()
                }
            }
        })

        binding.itemErrorMessage.btnRetry.setOnClickListener {
            lifecycleScope.launch {
                viewmodel.getAllProductByCategorize(category)
            }
        }
    }

    var isError = false
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNoErrors = !isError
            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtBeginning = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= QUERY_PAGE_SIZE
            val shouldPaginate =
                isNoErrors && isNotLoadingAndNotLastPage && isAtLastItem && isNotAtBeginning &&
                        isTotalMoreThanVisible && isScrolling
            Log.d(
                "SHOULDPAGINATE",
                "isNoErrors: $isNoErrors ,isNotLoadingAndNotLastPage: $isNotLoadingAndNotLastPage ,isAtLastItem: $isAtLastItem ,isNotAtBeginning: $isNotAtBeginning ,isTotalMoreThanVisible: $isTotalMoreThanVisible ,isScrolling: $isScrolling"
            )

            Log.d(
                "NESTEDSCROLL", "firstVisibleItemPosition: $firstVisibleItemPosition ," +
                        "visibleItemCount: $visibleItemCount " +
                        "totalItemCount: $totalItemCount "
            )
            if (shouldPaginate) {
                lifecycleScope.launch {
                    viewmodel.getAllProductByCategorize(category)
                    isScrolling = false
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                Log.d("ISSCROLL", "Eksekusi")
                isScrolling = true
            }
        }
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun hideErrorMessage() {
        binding.itemErrorMessage.itemError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showErrorMessage(message: String) {
        binding.itemErrorMessage.itemError.visibility = View.VISIBLE
        binding.itemErrorMessage.tvErrorMessage.visibility = View.VISIBLE
        binding.itemErrorMessage.tvErrorMessage.text = message
        isError = true
    }
}