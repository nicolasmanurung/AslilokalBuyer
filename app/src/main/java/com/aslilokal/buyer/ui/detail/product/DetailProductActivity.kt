package com.aslilokal.buyer.ui.detail.product

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ActivityDetailProductBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.response.OneProduct
import com.aslilokal.buyer.model.remote.response.Shop
import com.aslilokal.buyer.ui.detail.DetailViewModel
import com.aslilokal.buyer.ui.keranjang.KeranjangActivity
import com.aslilokal.buyer.ui.shop.DetailShopActivity
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_PRODUCT_URL
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_USR_URL
import com.aslilokal.buyer.utils.CustomFunctions
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tuonbondol.textviewutil.strike
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailProductBinding

    private lateinit var viewModel: DetailViewModel
    private lateinit var idProduct: String
    private lateinit var idShop: String
    private lateinit var idBuyer: String
    private lateinit var token: String
    private var isLogin: Boolean? = false
    private var shopInfo: Shop? = null
    private var isAvailable: Boolean? = false
    private var productInfo: OneProduct? = null
    private lateinit var datastore: AslilokalDataStore
    private var productType: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)

        requestedOrientation = if (resources.getBoolean(R.bool.portrait_only)) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

        CoroutineScope(Dispatchers.Main).launch {
            isLogin = datastore.read("ISLOGIN").toString().toBoolean()

            if (isLogin == true) {
                idBuyer = datastore.read("USERNAME").toString()
                token = datastore.read("TOKEN").toString()

                binding.optionKeranjang.setOnClickListener {
                    startActivity(Intent(binding.root.context, KeranjangActivity::class.java))
                }
                binding.btnAddToCart.setOnClickListener {
                    lifecycleScope.launch {
                        viewModel.postProductToCart(token, idBuyer, idProduct)
                    }
                }
            } else {
                binding.optionKeranjang.setOnClickListener {
                    Toast.makeText(
                        binding.root.context,
                        "Silahkan login terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                binding.btnAddToCart.setOnClickListener {
                    Toast.makeText(
                        binding.root.context,
                        "Silahkan login terlebih dahulu",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        hideLoadingProgress()

        showShimmerProduct()
        showShimmerShop()

        idProduct = intent.getStringExtra("idProduct")!!
        idShop = intent.getStringExtra("idShop")!!

        setupViewModel()

        lifecycleScope.launch {
            viewModel.getProduct(idProduct)
            viewModel.getShopDetail(idShop)
        }

        setupObserverOneProduct()
        setupObserverDetailShop()
        setupObserverPostToCart()

        binding.btnWhatsappSeller.setOnClickListener {
            val url = "https://wa.me/${shopInfo?.noWhatsappShop}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.addressToShop.setOnClickListener {
            val url = "https://www.google.com/maps/search/${shopInfo?.addressShop}"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent.setClassName(
                "com.google.android.apps.maps",
                "com.google.android.maps.MapsActivity"
            )
            startActivity(intent)
        }

        binding.shimmerLoadingSub2Data.setOnClickListener {
            val intent = Intent(binding.root.context, DetailShopActivity::class.java)
            intent.putExtra("idShop", idShop)
            startActivity(intent)
        }

    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(DetailViewModel::class.java)
    }

    private fun setupObserverOneProduct() {
        viewModel.product.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.result.let { productResponse ->
                        if (productResponse == null) {
                            Toast.makeText(this, "Sepertinya ada kesalahan", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            hideShimmerProduct()
                            showProductData(productResponse)
                            if ((productInfo != null) && (shopInfo != null) && (isAvailable != false) && (productType != "jasa")) {
                                binding.btnAddToCart.isActivated = true
                                binding.btnAddToCart.isEnabled = true
                                binding.btnAddToCart.setBackgroundColor(
                                    ContextCompat.getColor(
                                        binding.root.context,
                                        R.color.primaryColor
                                    )
                                )
                            } else {
                                binding.btnAddToCart.isActivated = false
                                binding.btnAddToCart.isEnabled = false
                                binding.btnAddToCart.setBackgroundColor(Color.GRAY)
                            }
                        }
                    }
                }

                is Resource.Error -> {
                    showShimmerProduct()
                    Toast.makeText(this, response.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }

                is Resource.Loading -> {
                    showShimmerProduct()
                }
            }
        })
    }

    private fun setupObserverDetailShop() {
        viewModel.shop.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data?.result.let { shopResponse ->
                        if (shopResponse == null) {
                            Toast.makeText(this, "Sepertinya ada kesalahan", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            hideShimmerShop()
                            showShopData(shopResponse)
                            if ((productInfo != null) && (shopInfo != null) && (isAvailable != false) && (productType != "jasa")) {
                                binding.btnAddToCart.isActivated = true
                                binding.btnAddToCart.isEnabled = true
                                binding.btnAddToCart.setBackgroundColor(
                                    ContextCompat.getColor(
                                        binding.root.context,
                                        R.color.primaryColor
                                    )
                                )
                            } else {
                                binding.btnAddToCart.isActivated = false
                                binding.btnAddToCart.isEnabled = false
                                binding.btnAddToCart.setBackgroundColor(Color.GRAY)
                            }
                        }
                    }
                }

                is Resource.Loading -> {
                    showShimmerShop()
                }

                is Resource.Error -> {
                    showShimmerShop()
                    Toast.makeText(this, response.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }

    private fun setupObserverPostToCart() {
        viewModel.cart.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideLoadingProgress()
                    response.data.let { productToCartResponse ->
                        if (productToCartResponse?.success == false) {
                            Toast.makeText(
                                binding.root.context,
                                productToCartResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (productToCartResponse?.success == true) {
                            Toast.makeText(this, productToCartResponse.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }

                is Resource.Loading -> {
                    showLoadingProgress()
                }

                is Resource.Error -> {
                    hideLoadingProgress()
                    Toast.makeText(this, response.message, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }


    @SuppressLint("SetTextI18n")
    private fun showProductData(product: OneProduct) {
        Log.d("ONEPRODUCTDATA", product.toString())
        binding.shimmerLoadingSub1.visibility = View.GONE
        binding.shimmerLoadingSub1Data.visibility = View.VISIBLE

        productInfo = product
        isAvailable = product.isAvailable
        productType = product.productCategory

        binding.txtNameProduct.text = product.nameProduct
        binding.txtWeightProduct.text = "${product.productWeight} gram"
        binding.txtTypeProduct.text = product.productCategory.capitalize(Locale.getDefault())
        binding.txtDescriptionProduct.text = product.descProduct

        Glide.with(binding.root.context)
            .load(BUCKET_PRODUCT_URL + product.imgProduct)
            .placeholder(R.drawable.loading_animation)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .priority(Priority.HIGH)
            .into(binding.imgDetailProduct)

        if (product.promoPrice != null) {
            if (product.promoPrice == 0) {
                binding.lnrPromo.visibility = View.GONE
                binding.txtCurrentPrice.text =
                    CustomFunctions().formatRupiah(product.priceProduct.toDouble())
            }
            if (product.promoPrice > 0) {
                binding.lnrPromo.visibility = View.VISIBLE
                val sumCount =
                    (product.promoPrice.toFloat().div(product.priceProduct.toFloat()))
                val countPercentage = (100 - (sumCount.times(100)).toInt())
                binding.txtPromoPrice.strike()
                binding.percentagePromo.text = "$countPercentage %"
                binding.txtPromoPrice.text =
                    CustomFunctions().formatRupiah(product.promoPrice.toDouble())
                binding.txtCurrentPrice.text =
                    CustomFunctions().formatRupiah(product.priceProduct.toDouble())
            }
        } else {
            binding.lnrPromo.visibility = View.GONE
            binding.txtCurrentPrice.text =
                CustomFunctions().formatRupiah(product.priceProduct.toDouble())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showShopData(shop: Shop) {
        binding.shimmerLoadingSub2.visibility = View.GONE
        binding.shimmerLoadingSub2Data.visibility = View.VISIBLE
        binding.rlOpenTime.visibility = View.VISIBLE

        shopInfo = shop

        Glide.with(binding.root.context)
            .load(BUCKET_USR_URL + shop.imgShop)
            .priority(Priority.HIGH)
            .placeholder(R.drawable.loading_animation)
            .into(binding.imgBannerLapo)

        binding.txtNameLapo.text = shop.nameShop
        binding.txtAddressLapo.text = shop.addressShop
        when (shop.shopTypeStatus) {
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

        if (shop.isTwentyFourHours) {
            binding.rlOpenTime.visibility = View.VISIBLE
            binding.txtOpenTimeLapo.text = "Buka 24 Jam"

        } else {
            binding.rlOpenTime.visibility = View.VISIBLE
            binding.txtOpenTimeLapo.text = "Buka dari ${shop.openTime} - ${shop.closeTime}"
        }
    }

    private fun showShimmerProduct() {
        binding.shimmerLoadingSub1.showShimmer(true)
        binding.shimmerLoadingSub1.startShimmer()
    }

    private fun hideShimmerProduct() {
        binding.shimmerLoadingSub1.stopShimmer()
        binding.shimmerLoadingSub1.hideShimmer()
    }

    private fun showShimmerShop() {
        binding.shimmerLoadingSub2.showShimmer(true)
        binding.shimmerLoadingSub2.startShimmer()
    }

    private fun hideShimmerShop() {
        binding.shimmerLoadingSub2.stopShimmer()
        binding.shimmerLoadingSub2.hideShimmer()
    }

    private fun showLoadingProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideLoadingProgress() {
        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

}