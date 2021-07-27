package com.aslilokal.buyer.ui.pembayaran.detailpembayaran

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ActivityDetailPembayaranBinding
import com.aslilokal.buyer.databinding.ItemMicroPesananBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.request.OrderRequest
import com.aslilokal.buyer.model.remote.request.Product
import com.aslilokal.buyer.model.remote.response.*
import com.aslilokal.buyer.ui.bottomfragment.ShipmentFragment
import com.aslilokal.buyer.ui.bottomfragment.VoucherFragment
import com.aslilokal.buyer.ui.pembayaran.PembayaranViewModel
import com.aslilokal.buyer.ui.pembayaran.verifikasipembayaran.VerifikasiPembayaranActivity
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Constants.Companion.RO_KEY_ID
import com.aslilokal.buyer.utils.CustomFunctions
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.aslilokal.buyer.viewmodel.ROViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class DetailPembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPembayaranBinding
    private lateinit var datastore : AslilokalDataStore
    private var isLogin: Boolean? = false
    private var isSellectedExpedition: Boolean? = false
    private var isSellectAutocomplete: Boolean? = false

    private lateinit var viewmodel: PembayaranViewModel
    private lateinit var ROviewmodel: ROViewModel

    private lateinit var username: String
    private lateinit var token: String
    private lateinit var listItemCart: ArrayList<Product>
    private lateinit var listCity: ArrayList<City>

    private lateinit var newNameBuyer: String
    private lateinit var newNoTelpBuyer: String
    private lateinit var newAddressBuyer: String
    private lateinit var totalSumPrice: String
    private lateinit var postalCodeInput: String
    private lateinit var courierCost: String
    private lateinit var courierType: String
    private var buyerInformation: RajaOngkirAddress? = null
    private var sellerInformation: Shop? = null
    private var applyVoucher: VoucherItem? = null

    private var deliveryPrice = 0
    private var voucherSumPrice = 0
    private var sumTotal = 0
    private var sumTotalProductPrice = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        showCurrentDataShimmer()

        setupViewModel()
        setupROViewModel()
        setupBuyerBiodataObserver()
        setupSellerBiodataObserver()
        setupOrderObserver()
        setupROObserver()

        listItemCart = intent.getParcelableArrayListExtra("LISTPRODUCT")!!

        runBlocking {
            isLogin = datastore.read("ISLOGIN").toString().toBoolean()
            username = datastore.read("USERNAME").toString()
            token = datastore.read("TOKEN").toString()

            viewmodel.getBuyerBiodata(token, username)
            viewmodel.getSellerBiodata(listItemCart.first().idSellerAccount)
        }

        setupRvMicroPembayaran()
        setupSumPricing()

        binding.txtUbahData.setOnClickListener {
            binding.lnrUbahData.visibility = View.VISIBLE
            binding.txtCancelUbah.visibility = View.VISIBLE
            binding.txtUbahData.visibility = View.GONE
            binding.rlCurrentData.visibility = View.GONE
            runBlocking {
                ROviewmodel.getCitiesByRO(RO_KEY_ID)
            }
        }

        binding.txtCancelUbah.setOnClickListener {
            binding.lnrUbahData.visibility = View.GONE
            binding.txtCancelUbah.visibility = View.GONE
            binding.txtUbahData.visibility = View.VISIBLE
            binding.rlCurrentData.visibility = View.VISIBLE
        }

        binding.btnSaveData.setOnClickListener {
            if (getCityFromAutocomplete(binding.originLocation.text.toString()) == null) {
                binding.originLocation.error = "Isi sesuai pilihan"
            }
            if (binding.etAddressSeller.text.toString().isEmpty()) {
                binding.etAddressSeller.error = "Isi alamat pengantaran"
            }
            if (binding.etNameSeller.text.toString().isEmpty()) {
                binding.etNameSeller.error = "Isi nama penerima"
            }
            if (binding.etPostalCode.text.toString().isEmpty()) {
                binding.etPostalCode.error = "Harap isi"
            }
            if (binding.etNoTelp.text.toString().isEmpty()) {
                binding.etNoTelp.error = "Isi nomor telp penerima"
            } else if (binding.etAddressSeller.text.toString()
                    .isNotEmpty() && binding.etNameSeller.text.toString()
                    .isNotEmpty() && binding.etNoTelp.text.toString().isNotEmpty()
            ) {
                postalCodeInput = binding.etPostalCode.text.toString()
                newAddressBuyer = binding.etAddressSeller.text.toString()
                newNameBuyer = binding.etNameSeller.text.toString()
                newNoTelpBuyer = binding.etNoTelp.text.toString()
                // set global address
                isSellectedExpedition = false
                binding.btnAddPayment.isEnabled = false
                binding.rlShipment.backgroundTintList =
                    ContextCompat.getColorStateList(binding.root.context, R.color.grayFading)

                binding.btnAddPayment.setBackgroundColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.gray
                    )
                )
                val tempAddress = getCityFromAutocomplete(binding.originLocation.text.toString())
                if (tempAddress != null) {
                    buyerInformation = RajaOngkirAddress(
                        tempAddress.city_id,
                        tempAddress.city_name,
                        tempAddress.postal_code,
                        tempAddress.province,
                        tempAddress.province_id,
                        tempAddress.type
                    )
                }
                binding.lnrCostDelivery.visibility = View.GONE
                binding.txtCancelUbah.visibility = View.GONE
                binding.titleTxtPilihPengantaran.text = "Pilih Pengantaran"
                deliveryPrice = 0
                setupSumPricing()
                setupNewDataBuyer()
            }
        }

        binding.rlShipment.setOnClickListener {
            ShipmentFragment().apply {
                show(supportFragmentManager, tag)
            }
        }

        binding.rlVoucher.setOnClickListener {
            VoucherFragment().apply {
                show(supportFragmentManager, tag)
            }
        }

        binding.removeVoucher.setOnClickListener {
            voucherSumPrice = 0
            binding.lnrVoucherApply.visibility = View.GONE
            binding.removeVoucher.visibility = View.GONE
            binding.voucherBtn.visibility = View.VISIBLE
            binding.titleVoucher.text = "Lihat Voucher"
            applyVoucher = null
            setupSumPricing()
        }

        binding.btnAddPayment.setOnClickListener {
            if (isSellectedExpedition == true) {
                setupDataOrder()
            } else {
                // todo
            }
        }

        //change address delivery
        binding.originLocation.addTextChangedListener {
            if (it?.isNotEmpty() == true) {
                binding.txtChangeProvince.visibility = View.VISIBLE
            }
            if (getCityFromAutocomplete(it.toString()) == null) {
                isSellectAutocomplete = false
            } else {
                isSellectAutocomplete = false
                binding.txtChangeProvince.visibility = View.GONE
            }
        }

        binding.txtChangeProvince.setOnClickListener {
            isSellectAutocomplete = false
            binding.originLocation.setText("")
        }
    }


    private fun setupROObserver() {
        ROviewmodel.citiesResults.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { cityResponse ->
                        hideProgress()
                        initSpinner(cityResponse?.rajaongkir?.results ?: return@observe)
                    }
                }

                is Resource.Loading -> {
                    showProgress()
                }

                is Resource.Error -> {
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
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
                            buyerInformation = buyerBiodataResponse.result.rajaOngkir
                            postalCodeInput = buyerBiodataResponse.result.postalCodeInput.toString()
                            setupDataBuyer(buyerBiodataResponse.result)
                        }
                    }
                }

                is Resource.Loading -> {
                    showCurrentDataShimmer()
                }

                is Resource.Error -> {
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setupSellerBiodataObserver() {
        viewmodel.sellerBiodatas.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { sellerResponse ->
                        if (sellerResponse?.success == false) {
                            Toast.makeText(
                                binding.root.context,
                                sellerResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (sellerResponse?.success == true) {
                            hideProgress()
                            sellerInformation = sellerResponse.result
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgress()
                }

                is Resource.Error -> {
                    showProgress()
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
                            finish()
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgress()
                }

                is Resource.Error -> {
                    showProgress()
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initSpinner(cityResults: ArrayList<City>) {
        listCity = cityResults
        val cities = mutableListOf<String>()
        for (i in cityResults.indices) cities.add(
            cityResults[i].province + ", " + cityResults[i].city_name
        )
        val cityAdapter = ArrayAdapter(
            binding.root.context,
            android.R.layout.simple_spinner_dropdown_item,
            cities
        )
        binding.originLocation.setAdapter(cityAdapter)
        binding.originLocation.setOnItemClickListener { parent, view, position, id ->
            isSellectAutocomplete = true
        }

        if (buyerInformation != null) {
            binding.originLocation.setText(buyerInformation!!.province + ", " + buyerInformation!!.city_name)
//            binding.originLocation.setSelection(0)
        }
    }

    private fun getCityFromAutocomplete(city: String): City? {
        if (isSellectAutocomplete == true) {
            val textCity = city.split(", ")

            val tempCity = listCity.filter {
                it.city_name.contains(textCity[1])
            }

            for (i in tempCity.indices) {
                val matchedCity = tempCity[i].city_name
                if (tempCity[i].city_name == matchedCity) {
                    return tempCity[i]
                }
            }
        }
        return null
    }

    private fun setupDataOrder() {
        var oneOrderRequest = OrderRequest(
            binding.txtAlamatBuyer.text.toString(),
            courierCost,
            courierType,
            username,
            listItemCart.first().idSellerAccount,
            binding.txtNameBuyer.text.toString(),
            binding.txtNoTelp.text.toString(),
            listItemCart,
            "paymentrequired",
            totalSumPrice,
            totalSumPrice,
            applyVoucher?.codeVoucher.toString(),
            applyVoucher?._id.toString(),
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

    @SuppressLint("SetTextI18n")
    private fun setupNewDataBuyer() {
        binding.txtNameBuyer.text = newNameBuyer
        binding.txtAlamatBuyer.text = "$newAddressBuyer, $postalCodeInput"
        binding.txtNoTelp.text = newNoTelpBuyer

        binding.txtUbahData.visibility = View.VISIBLE
        binding.lnrUbahData.visibility = View.GONE
        binding.rlCurrentData.visibility = View.VISIBLE
    }

    private fun setupViewModel() {
        viewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(PembayaranViewModel::class.java)
    }

    private fun setupROViewModel() {
        ROviewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.apiRO))
        ).get(ROViewModel::class.java)
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
        sumTotalProductPrice = 0
        var sumQtyTotal = 0
        for (item in listItemCart) {
            sumTotalProductPrice += item.priceAt.toInt() * item.qty.toInt()
            sumQtyTotal += item.qty.toInt()
        }
        sumTotal = (sumTotalProductPrice - voucherSumPrice) + deliveryPrice
        totalSumPrice = sumTotal.toString()
        binding.txtSumPrice.text = CustomFunctions().formatRupiah(sumTotal.toDouble())
        binding.txtSumQtyProduct.text = "$sumQtyTotal item"
    }

    fun getTotalWeight(): String {
        Log.d("WEIGHT", listItemCart.toString())
        var sumTotal = 0
        for (item in listItemCart) {
            sumTotal += item.productWeight.toInt() * item.qty.toInt()
        }
        return sumTotal.toString()
    }

    @SuppressLint("SetTextI18n")
    fun setupEstimatedCost(cost: Cost, expedition: String) {
        isSellectedExpedition = true
        binding.btnAddPayment.isEnabled = true
        binding.rlShipment.backgroundTintList =
            ContextCompat.getColorStateList(binding.root.context, R.color.white)

        binding.btnAddPayment.setBackgroundColor(
            ContextCompat.getColor(
                binding.root.context,
                R.color.primaryColor
            )
        )
        if (expedition == "CUSTOM") {
            binding.titleTxtPilihPengantaran.text = cost.service
            binding.txtNameExpedition.text = cost.service
        } else {
            binding.titleTxtPilihPengantaran.text = expedition + " ${cost.service}"
            binding.txtNameExpedition.text = expedition + " ${cost.service}"
        }

        binding.lnrCostDelivery.visibility = View.VISIBLE
        when (expedition) {
            "JNE" -> {
                binding.txtEstimatedDay.text = cost.cost.first().etd + " hari"
            }

            "POS" -> {
                binding.txtEstimatedDay.text = cost.cost.first().etd
            }

            "TIKI" -> {
                binding.txtEstimatedDay.text = cost.cost.first().etd + " hari"
            }
        }
        binding.txtDeliveryPrice.text =
            CustomFunctions().formatRupiah(cost.cost.first().value.toDouble())
        deliveryPrice = cost.cost.first().value
        courierCost = deliveryPrice.toString()
        courierType = expedition + " " + cost.service
        setupSumPricing()
    }

    @SuppressLint("SetTextI18n")
    fun setupVoucher(voucher: VoucherItem) {
        applyVoucher = voucher
        binding.voucherBtn.visibility = View.GONE
        binding.removeVoucher.visibility = View.VISIBLE
        binding.lnrVoucherApply.visibility = View.VISIBLE

        binding.titleVoucher.text = voucher.codeVoucher
        binding.txtValueVoucher.text = voucher.valueVoucher.toString() + "%"


        var voucherPersentage = voucher.valueVoucher
        voucherSumPrice = (voucherPersentage * sumTotalProductPrice) / 100
        binding.txtVoucherActualRupiah.text =
            "-${CustomFunctions().formatRupiah(voucherSumPrice.toDouble())}"
        setupSumPricing()
    }


    class MicroPembayaranAdapter(private val listItem: ArrayList<Product>) :
        RecyclerView.Adapter<MicroPembayaranAdapter.MicroPembayaranViewHolder>() {
        inner class MicroPembayaranViewHolder(private val binding: ItemMicroPesananBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n")
            fun bind(itemProduct: Product) {
                binding.txtNameProduct.text = itemProduct.nameProduct
                binding.txtSumProduct.text = itemProduct.qty + "X"
                binding.txtPriceProduct.text =
                    CustomFunctions().formatRupiah(itemProduct.priceAt.toDouble())
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
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    fun getBuyerInformation(): RajaOngkirAddress? {
        return buyerInformation
    }

    fun getSellerInformation(): Shop? {
        return sellerInformation
    }

    fun getIsInDistanceShopDelivery(): Boolean {
        Log.d(
            "POSTALCODE",
            "Seller Postal: " + sellerInformation?.postalCodeInput + " " + "Buyer Postal: " + postalCodeInput
        )
        return sellerInformation?.postalCodeInput == postalCodeInput
    }

}