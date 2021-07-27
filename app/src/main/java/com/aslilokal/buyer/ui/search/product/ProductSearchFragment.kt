package com.aslilokal.buyer.ui.search.product

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.databinding.FragmentProductSearchBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.ui.adapter.ProductGridAdapter
import com.aslilokal.buyer.ui.search.SearchActivity
import com.aslilokal.buyer.ui.search.SearchViewModel
import com.aslilokal.buyer.utils.Constants
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.launch

class ProductSearchFragment : Fragment() {
    private var _binding: FragmentProductSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var productGridAdapter: ProductGridAdapter
    private lateinit var searchViewmodel: SearchViewModel
    private lateinit var searchActivity: SearchActivity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductSearchBinding.inflate(inflater, container, false)
        setupSearchViewModel()
        setupProductsObserver()
        setupProductsRv()
        searchActivity = activity as SearchActivity
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun setupSearchViewModel() {
        searchViewmodel = activity?.let {
            ViewModelProvider(
                it,
                AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
            ).get(SearchViewModel::class.java)
        }!!
    }

    private fun setupProductsRv() {
        productGridAdapter = ProductGridAdapter()
        binding.rvSearchProducts.apply {
            adapter = productGridAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2)
            addOnScrollListener(this@ProductSearchFragment.scrollListener)
        }
    }

    private fun setupProductsObserver() {
        searchViewmodel.products.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideLoadingProgress()
                    response.data.let { productsResponse ->
                        if (productsResponse?.success == true) {
                            if (productsResponse.result.docs?.isNotEmpty() == true) {
                                Log.d(
                                    "PRODUCTRESPONSE",
                                    productsResponse.result.docs.size.toString()
                                )
                                productGridAdapter.differ.submitList(productsResponse.result.docs.toList())
                                hideEmpty()
                            } else {
                                showEmpty()
                            }
                            val totalPages = productsResponse.result.totalPages
                            isLastPage = (searchViewmodel.productPage - 1) == totalPages
                            if (isLastPage) {
                                binding.rvSearchProducts.setPadding(0, 0, 0, 0)
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
                    showLoadingProgress()
                }

                is Resource.Error -> {
                    hideLoadingProgress()
                    Toast.makeText(
                        binding.root.context,
                        "Kesalahan tak terduga",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
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
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
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
                    var category = searchActivity.searchText
                    if (category.isNotEmpty()) {
                        Log.d("CATEGORYSCROLL", category)
                        searchViewmodel.getSearchProductsByName(category)
                    }
                    isScrolling = false
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                isScrolling = true
            }
        }
    }

    private fun showEmpty() {
        binding.lnrEmpty.visibility = View.VISIBLE
        binding.rvSearchProducts.visibility = View.GONE
    }

    private fun hideEmpty() {
        binding.lnrEmpty.visibility = View.GONE
        binding.rvSearchProducts.visibility = View.VISIBLE
    }

    fun showLoadingProgress() {
        binding.progressProductPagination.visibility = View.VISIBLE
    }

    fun hideLoadingProgress() {
        binding.progressProductPagination.visibility = View.INVISIBLE
    }
}