package com.aslilokal.buyer.ui.shop

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ActivityDetailShopBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.ui.pesanan.bayar.BayarFragment
import com.aslilokal.buyer.ui.shop.produk.ProductShopFragment
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_USR_URL
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

class DetailShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailShopBinding
    lateinit var idShop: String
    private lateinit var shopViewModel: ShopViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupShopViewModel()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        idShop = intent.getStringExtra("idShop").toString()
        getShopBiodata()

        binding.vp.adapter = ViewPagerAdapter(this)

        TabLayoutMediator(binding.tabs, binding.vp) { tab, position ->
            when (position) {
                0 -> tab.text = "Produk"
            }
        }.attach()
    }

    private fun getShopBiodata() = CoroutineScope(Dispatchers.Main).launch {
        showLoadingProgress()
        try {
            val response = RetrofitInstance.api.getShopDetail(idShop)
            if (response.isSuccessful) {
                hideLoadingProgress()
                response.body()?.result.let { shopResponse ->
                    if (shopResponse != null) {
                        Glide.with(binding.root.context)
                            .load(BUCKET_USR_URL + shopResponse.imgShop)
                            .into(binding.imgBannerLapo)
                        binding.txtNameLapo.text = shopResponse.nameShop
                        binding.txtAddressLapo.text = shopResponse.addressShop
                        when (shopResponse.shopTypeStatus) {
                            "taput" -> {
                                binding.txtNameAsliUmkm.text = "Asli Taput"
                            }
                            "tapteng" -> {
                                binding.txtNameAsliUmkm.text = "Asli Tapteng"
                            }
                            else -> {
                                binding.rlAsliUmkm.visibility = View.GONE
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah",
                    Toast.LENGTH_SHORT
                ).show()
                hideLoadingProgress()
            }
        } catch (exception: Exception) {
            hideLoadingProgress()
            when (exception) {
                is IOException -> Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    Toast.makeText(
                        binding.root.context,
                        "Kesalahan tak terduga",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("KESALAHAN", exception.toString())
                }
            }
        }
    }

    private fun setupShopViewModel() {
        shopViewModel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(ShopViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.option_detail_shop, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu?.findItem(R.id.search)?.actionView as SearchView

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView.queryHint = resources.getString(R.string.search_shop_hint)

        val searchMenuItem = menu.findItem(R.id.search)

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                return true
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                CoroutineScope(Dispatchers.Main).launch {
                    shopViewModel.getProductsShopByName(idShop, query.toString())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                when (newText) {
                    "" -> {
                        CoroutineScope(Dispatchers.Main).launch {
                            shopViewModel.getProductsShopByName(idShop, "")
                        }
                    }
                }
                return true
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    class ViewPagerAdapter internal constructor(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 1

        override fun createFragment(position: Int): Fragment {
            when (position) {
                0 -> return ProductShopFragment()
            }
            return BayarFragment()
        }
    }

    fun showLoadingProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun hideLoadingProgress() {
        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}