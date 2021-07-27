package com.aslilokal.buyer.ui.bottomfragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.databinding.FragmentShipmentListDialogBinding
import com.aslilokal.buyer.databinding.ItemShipmentListBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.response.*
import com.aslilokal.buyer.ui.pembayaran.detailpembayaran.DetailPembayaranActivity
import com.aslilokal.buyer.utils.Constants.Companion.RO_KEY_ID
import com.aslilokal.buyer.utils.CustomFunctions
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.aslilokal.buyer.viewmodel.ROViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.runBlocking

class ShipmentFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentShipmentListDialogBinding? = null
    private val binding get() = _binding!!
    private var buyerInformation: RajaOngkirAddress? = null
    private var sellerInformation: Shop? = null
    private lateinit var detailPembayaranActivity: DetailPembayaranActivity
    private lateinit var shipmentJneAdapter: ShipmentAdapter
    private lateinit var shipmentPosAdapter: ShipmentAdapter
    private lateinit var shipmentTikiAdapter: ShipmentAdapter
    private lateinit var shipmentCustomAdapter: ShipmentAdapter
    private lateinit var roViewmodel: ROViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShipmentListDialogBinding.inflate(inflater, container, false)
        detailPembayaranActivity = activity as DetailPembayaranActivity
        setupROViewModel()
        setupRvCost()

        buyerInformation = detailPembayaranActivity.getBuyerInformation()
        sellerInformation = detailPembayaranActivity.getSellerInformation()
        // tambahkan listener ketika click item maka akan close
//        binding.txtClose.setOnClickListener {
//           dismiss()
//        }

        Log.d("SELLERINFORMATION", sellerInformation.toString())
        if (buyerInformation != null && sellerInformation != null) {
            runBlocking {
                Log.d("SELLERID", sellerInformation!!.rajaOngkir.city_id)
                roViewmodel.postCostRO(
                    RO_KEY_ID,
                    sellerInformation!!.rajaOngkir.city_id,
                    buyerInformation!!.city_id,
                    detailPembayaranActivity.getTotalWeight()
                )
            }
        }
        setupCostObservable()

        shipmentJneAdapter.onItemClick = { data ->
            detailPembayaranActivity.setupEstimatedCost(data, "JNE")
        }

        shipmentPosAdapter.onItemClick = { data ->
            detailPembayaranActivity.setupEstimatedCost(data, "POS")
        }

        shipmentTikiAdapter.onItemClick = { data ->
            detailPembayaranActivity.setupEstimatedCost(data, "TIKI")
        }

        shipmentCustomAdapter.onItemClick = { data ->
            detailPembayaranActivity.setupEstimatedCost(data, "CUSTOM")
        }

        return binding.root
    }

    private fun setupROViewModel() {
        roViewmodel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.apiRO))
        ).get(ROViewModel::class.java)
    }

    private fun setupRvCost() {
        shipmentJneAdapter = ShipmentAdapter("jne")

        binding.rvJneShipmentCost.apply {
            adapter = shipmentJneAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }

        shipmentPosAdapter = ShipmentAdapter("pos")
        binding.rvPosShipmentCost.apply {
            adapter = shipmentPosAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }

        shipmentTikiAdapter = ShipmentAdapter("tiki")
        binding.rvTikiShipmentCost.apply {
            adapter = shipmentTikiAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }

        shipmentCustomAdapter = ShipmentAdapter("custom")
        binding.rvCustomShipmentCost.apply {
            adapter = shipmentCustomAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupCostObservable() {
        roViewmodel.costResults.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgress()
                    response.data.let { roCostResponse ->
                        roCostResponse?.let { breakingDataObserver(it) }
//                        shipmentAdapter.differ.submitList(roCostResponse?.rajaongkir?.results?.first()?.costs?.toList())
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

    private fun breakingDataObserver(listResponse: List<ROCostResponse>) {
        if (sellerInformation != null) {
            val tempCost = ArrayList<Cost>()
            if (sellerInformation!!.isDelivery) {
                // add condition ketika gratis pengantaran && dalam jangkauan
                Log.d(
                    "ISINDISTANCE",
                    detailPembayaranActivity.getIsInDistanceShopDelivery().toString()
                )
                if (detailPembayaranActivity.getIsInDistanceShopDelivery() == true) {
                    if (sellerInformation?.isShopFreeDelivery == true) {
                        tempCost.add(
                            Cost(
                                arrayListOf(CostX("", "", 0)),
                                "Diantarkan langsung oleh penjual setelah pesanan di konfirmasi",
                                "Diantar Penjual"
                            )
                        )
                    } else if (sellerInformation?.isShopFreeDelivery == false) {
                        tempCost.add(
                            Cost(
                                arrayListOf(
                                    CostX(
                                        "",
                                        "",
                                        5000
                                    )
                                ),
                                "Diantarkan langsung oleh penjual setelah pesanan di konfirmasi",
                                "Diantar Penjual"
                            )
                        )
                    }
                }
            }
            if (sellerInformation!!.isPickup) {
                tempCost.add(
                    Cost(
                        arrayListOf(CostX("", "", 0)),
                        "Barang harus anda jemput setelah dikemas",
                        "Dijemput Sendiri"
                    )
                )
            }

            shipmentCustomAdapter.differ.submitList(tempCost)
        }
        shipmentJneAdapter.differ.submitList(listResponse[0].rajaongkir.results.first().costs.toList())
        shipmentPosAdapter.differ.submitList(listResponse[1].rajaongkir.results.first().costs.toList())
        shipmentTikiAdapter.differ.submitList(listResponse[2].rajaongkir.results.first().costs.toList())
    }


    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
    }

    private inner class ShipmentAdapter(type: String) :
        RecyclerView.Adapter<ShipmentAdapter.ShipmentViewHolder>() {

        var typeExpedition = type
        var onItemClick: ((Cost) -> Unit)? = null

        inner class ShipmentViewHolder(private val binding: ItemShipmentListBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n")
            fun bind(itemCost: Cost) {
                when (typeExpedition) {
                    "jne" -> {
                        binding.txtTitleShipment.text = "JNE " + itemCost.service
                        binding.txtDescShipment.text =
                            "Estimasi pengiriman " + itemCost.cost.first().etd + " hari"
                    }
                    "pos" -> {
                        binding.txtTitleShipment.text = "POS " + itemCost.service
                        binding.txtDescShipment.text =
                            "Estimasi pengiriman " + itemCost.cost.first().etd
                    }
                    "tiki" -> {
                        binding.txtTitleShipment.text = "TIKI " + itemCost.service
                        binding.txtDescShipment.text =
                            "Estimasi pengiriman " + itemCost.cost.first().etd
                    }
                    "custom" -> {
                        if (itemCost.service == "Diantar Penjual") {
                            binding.txtTitleShipment.text = "Diantar Penjual"
                            binding.txtDescShipment.text =
                                "Diantarkan langsung oleh penjual setelah pesanan di konfirmasi"
                        } else if (itemCost.service == "Dijemput Sendiri") {
                            binding.txtTitleShipment.text = "Dijemput Sendiri"
                            binding.txtDescShipment.text =
                                "Barang harus anda jemput setelah dikemas"
                        }
                    }
                }
                binding.txtPriceShipment.text =
                    CustomFunctions().formatRupiah(itemCost.cost.first().value.toDouble())

                itemView.setOnClickListener {
                    onItemClick?.invoke(itemCost)
                    dismiss()
                }
            }
        }

        private val differCallback = object : DiffUtil.ItemCallback<Cost>() {
            override fun areItemsTheSame(oldItem: Cost, newItem: Cost): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Cost, newItem: Cost): Boolean {
                return oldItem == newItem
            }
        }

        val differ = AsyncListDiffer(this, differCallback)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShipmentViewHolder =
            ShipmentViewHolder(
                ItemShipmentListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun getItemCount(): Int = differ.currentList.size

        override fun onBindViewHolder(holder: ShipmentViewHolder, position: Int) {
            holder.bind(differ.currentList[position])
        }
    }
}