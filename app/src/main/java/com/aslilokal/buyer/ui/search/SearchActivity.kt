package com.aslilokal.buyer.ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aslilokal.buyer.databinding.ActivitySearchBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.ui.search.product.ProductSearchFragment
import com.aslilokal.buyer.ui.search.shop.ShopSearchFragment
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchViewModel: SearchViewModel
    var searchText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.searchToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupSearchViewmodel()

        binding.vp.adapter = ViewPagerAdapter(this)
        TabLayoutMediator(binding.tabs, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = "Produk"
                1 -> tab.text = "Lapo"
            }
        }.attach()

        binding.vp.isUserInputEnabled = false
        binding.searchProductsShops.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                CoroutineScope(Dispatchers.Main).launch {
                    searchText = query.toString()
                    searchViewModel.productPage = 1
                    searchViewModel.shopPage = 1
                    searchViewModel.resetProductsShopsValue()
                    searchViewModel.getSearchProductsByName(query.toString())
                    searchViewModel.getSearchShopByName(query.toString())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun setupSearchViewmodel() {
        searchViewModel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(SearchViewModel::class.java)
    }

    class ViewPagerAdapter internal constructor(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return ProductSearchFragment()
                1 -> return ShopSearchFragment()
            }
            return ProductSearchFragment()
        }

    }
}