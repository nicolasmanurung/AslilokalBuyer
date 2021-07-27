package com.aslilokal.buyer.ui.bottomfragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.databinding.FragmentVoucherBinding
import com.aslilokal.buyer.databinding.ItemVoucherBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.response.VoucherItem
import com.aslilokal.buyer.ui.pembayaran.detailpembayaran.DetailPembayaranActivity
import com.aslilokal.buyer.utils.CustomFunctions
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.runBlocking

class VoucherFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentVoucherBinding? = null
    private val binding get() = _binding!!
    private lateinit var detailPembayaranActivity: DetailPembayaranActivity
    private lateinit var voucherAdapter: VoucherAdapter
    private lateinit var voucherViewModel: VoucherViewModel
    private var shopUsername: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVoucherBinding.inflate(inflater, container, false)

        detailPembayaranActivity = activity as DetailPembayaranActivity
        setupViewModel()
        setupRecyclerVoucher()
        setupVoucherObservable()

        shopUsername = detailPembayaranActivity.getSellerInformation()?.idSellerAccount

        runBlocking {
            voucherViewModel.getAllVoucherByShop(shopUsername.toString())
        }

        voucherAdapter.onItemClick = { data ->
            detailPembayaranActivity.setupVoucher(data)
        }

        return binding.root
    }

    private fun setupViewModel() {
        voucherViewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(VoucherViewModel::class.java)
    }

    private fun setupRecyclerVoucher() {
        voucherAdapter = VoucherAdapter()
        binding.rvVoucher.apply {
            adapter = voucherAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupVoucherObservable() {
        voucherViewModel.vouchers.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgress()
                    response.data.let { voucher ->
                        if (voucher?.result.isNullOrEmpty()) {
                            binding.txtEmpty.visibility = View.VISIBLE
                        } else {
                            binding.txtEmpty.visibility = View.GONE
                            voucherAdapter.differ.submitList(voucher?.result)
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

    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
    }

    private inner class VoucherAdapter : RecyclerView.Adapter<VoucherAdapter.VoucherViewHolder>() {

        var onItemClick: ((VoucherItem) -> Unit)? = null

        inner class VoucherViewHolder(private val binding: ItemVoucherBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n")
            fun bind(voucher: VoucherItem) {
                binding.txtValueVoucher.text = "Potongan harga ${voucher.valueVoucher} %"
                binding.txtCodeVoucher.text = voucher.codeVoucher
                binding.txtDateLimit.text =
                    CustomFunctions().isoTimeToSlashNormalDate(voucher.validity)

                itemView.setOnClickListener {
                    onItemClick?.invoke(voucher)
                    dismiss()
                }
            }
        }

        private val differCallback = object : DiffUtil.ItemCallback<VoucherItem>() {
            override fun areItemsTheSame(oldItem: VoucherItem, newItem: VoucherItem): Boolean {
                return oldItem._id == newItem._id
            }

            override fun areContentsTheSame(oldItem: VoucherItem, newItem: VoucherItem): Boolean {
                return oldItem == newItem
            }
        }

        val differ = AsyncListDiffer(this, differCallback)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VoucherViewHolder =
            VoucherViewHolder(
                ItemVoucherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun onBindViewHolder(holder: VoucherViewHolder, position: Int) {
            holder.bind(differ.currentList[position])
        }

        override fun getItemCount(): Int = differ.currentList.size
    }
}