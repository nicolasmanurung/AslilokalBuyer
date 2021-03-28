package com.aslilokal.buyer.ui.pembayaran.detailpembayaran

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.databinding.ActivityDetailPembayaranBinding
import com.aslilokal.buyer.databinding.ItemMicroPesananBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.request.Product
import com.aslilokal.buyer.model.remote.response.DetailBiodata
import com.aslilokal.buyer.model.remote.response.ItemCart
import com.aslilokal.buyer.ui.pembayaran.PembayaranViewModel
import com.aslilokal.buyer.ui.pembayaran.verifikasipembayaran.VerifikasiPembayaranActivity
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.CustomFunctions
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DetailPembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPembayaranBinding
    private var datastore = AslilokalDataStore(this)
    private var isLogin: Boolean? = false

    private lateinit var viewmodel: PembayaranViewModel
    private lateinit var username: String
    private lateinit var token: String
    private lateinit var listItemCart: ArrayList<ItemCart>

    private lateinit var newNameBuyer: String
    private lateinit var newNoTelpBuyer: String
    private lateinit var newAddressBuyer: String
    private lateinit var totalSumPrice: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)
        showCurrentDataShimmer()

        setupViewModel()
        setupBuyerBiodataObserver()
        setupOrderObserver()

        runBlocking {
            isLogin = datastore.read("ISLOGIN").toString().toBoolean()
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()

            viewmodel.getBuyerBiodata(token, username)
        }

        listItemCart = intent.getParcelableArrayListExtra("LISTPRODUCT")!!

        setupRvMicroPembayaran()
        setupSumPricing()

        binding.txtUbahData.setOnClickListener {
            binding.txtUbahData.visibility = View.GONE
            binding.lnrUbahData.visibility = View.VISIBLE
            binding.rlCurrentData.visibility = View.GONE
        }

        binding.btnSaveData.setOnClickListener {
            if (binding.etAddressSeller.text.toString().isEmpty()) {
                binding.etAddressSeller.error = "Isi alamat pengantaran"
            }
            if (binding.etNameSeller.text.toString().isEmpty()) {
                binding.etNameSeller.error = "Isi nama penerima"
            }
            if (binding.etNoTelp.text.toString().isEmpty()) {
                binding.etNoTelp.error = "Isi nomor telp penerima"
            } else if (binding.etAddressSeller.text.toString()
                    .isNotEmpty() && binding.etNameSeller.text.toString()
                    .isNotEmpty() && binding.etNoTelp.text.toString().isNotEmpty()
            ) {
                newAddressBuyer = binding.etAddressSeller.text.toString()
                newNameBuyer = binding.etNameSeller.text.toString()
                newNoTelpBuyer = binding.etNoTelp.text.toString()
                setupNewDataBuyer()
            }
        }

        binding.btnAddPayment.setOnClickListener {
            setupDataOrder()
        }
    }

    private fun setupBuyerBiodataObserver() {
        viewmodel.buyerBiodatas.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { buyerBiodataResponse ->
                        if (buyerBiodataResponse?.success == false) {
                            hideCurrentDataShimmer()
                            Toast.makeText(
                                binding.root.context,
                                buyerBiodataResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (buyerBiodataResponse?.success == true) {
                            hideCurrentDataShimmer()
                            setupDataBuyer(buyerBiodataResponse.result)
                        }
                    }
                }

                is Resource.Loading -> {
                    showCurrentDataShimmer()
                }

                is Resource.Error -> {
                    hideCurrentDataShimmer()
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setupOrderObserver() {
        viewmodel.orders.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { orderResponse ->
                        if (orderResponse?.success == false) {
                            hideProgress()
                            Toast.makeText(
                                binding.root.context,
                                orderResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (orderResponse?.success == true) {
                            hideProgress()
                            //Intent
                            var intent = Intent(this, VerifikasiPembayaranActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgress()
                }

                is Resource.Error -> {
                    hideProgress()
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setupDataOrder() {
        var listProducts = ArrayList<Product>()
        for (item in listItemCart) {
            listProducts.add(
                Product(
                    item.idProduct,
                    item.imgProduct,
                    item.nameProduct.toString(),
                    item.noteProduct.toString(),
                    item.productPrice.toString(),
                    item.qtyProduct.toString()
                )
            )
        }

        var oneOrderRequest = OrderRequest(
            binding.txtAlamatBuyer.text.toString(),
            "0",
            "",
            username,
            listItemCart.first().idSellerAccount,
            binding.txtNameBuyer.text.toString(),
            binding.txtNoTelp.text.toString(),
            listProducts,
            "paymentrequired",
            totalSumPrice,
            totalSumPrice,
            "",
            ""
        )

        lifecycleScope.launch {
            viewmodel.postOneOrder(token, oneOrderRequest)
        }
    }

    private fun setupDataBuyer(buyerResponse: DetailBiodata) {
        binding.txtNameBuyer.text = buyerResponse.nameBuyer
        binding.txtAlamatBuyer.text = buyerResponse.addressBuyer
        binding.txtNoTelp.text = buyerResponse.noTelpBuyer
    }

    private fun setupNewDataBuyer() {
        binding.txtNameBuyer.text = newNameBuyer
        binding.txtAlamatBuyer.text = newAddressBuyer
        binding.txtNoTelp.text = newNoTelpBuyer

        binding.txtUbahData.visibility = View.VISIBLE
        binding.lnrUbahData.visibility = View.GONE
        binding.rlCurrentData.visibility = View.VISIBLE
    }

    private fun setupViewModel() {
        viewmodel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(PembayaranViewModel::class.java)
    }

    private fun setupRvMicroPembayaran() {
        val itemCartAdapter = MicroPembayaranAdapter(listItemCart)
        binding.rvMicroPesanan.apply {
            adapter = itemCartAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupSumPricing() {
        var sumTotal = 0
        var sumQtyTotal = 0
        for (item in listItemCart) {
            sumTotal += item.productPrice * item.qtyProduct
            sumQtyTotal += item.qtyProduct
        }
        totalSumPrice = sumTotal.toString()
        binding.txtSumPrice.text = CustomFunctions().formatRupiah(sumTotal.toDouble())
        binding.txtSumQtyProduct.text = "$sumQtyTotal item"
    }

    class MicroPembayaranAdapter(private val listItem: ArrayList<ItemCart>) :
        RecyclerView.Adapter<MicroPembayaranAdapter.MicroPembayaranViewHolder>() {
        inner class MicroPembayaranViewHolder(private val binding: ItemMicroPesananBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n")
            fun bind(itemProduct: ItemCart) {
                binding.txtNameProduct.text = itemProduct.nameProduct
                binding.txtSumProduct.text = itemProduct.qtyProduct.toString() + "X"
                binding.txtPriceProduct.text =
                    CustomFunctions().formatRupiah(itemProduct.productPrice.toDouble())
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MicroPembayaranViewHolder = MicroPembayaranViewHolder(
            ItemMicroPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: MicroPembayaranViewHolder, position: Int) {
            holder.bind(listItem[position])
        }

        override fun getItemCount(): Int = listItem.size
    }

    private fun showCurrentDataShimmer() {
        binding.shimmerLoadingCurrentData.showShimmer(true)
        binding.shimmerLoadingCurrentData.startShimmer()
    }

    private fun hideCurrentDataShimmer() {
        binding.shimmerLoadingCurrentData.stopShimmer()
        binding.shimmerLoadingCurrentData.hideShimmer()
    }

    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

}