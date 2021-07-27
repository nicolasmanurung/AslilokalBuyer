package com.aslilokal.buyer.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ItemProductPesananBinding
import com.aslilokal.buyer.model.remote.response.ProductOrder
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_PRODUCT_URL
import com.aslilokal.buyer.utils.CustomFunctions
import com.bumptech.glide.Glide

class PesananProductAdapter :
    RecyclerView.Adapter<PesananProductAdapter.PesananProductViewHolder>() {

    inner class PesananProductViewHolder(private val binding: ItemProductPesananBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(itemPesanan: ProductOrder) {

            Glide.with(itemView.context)
                .load(BUCKET_PRODUCT_URL + itemPesanan.imgProduct)
                .placeholder(R.drawable.loading_animation)
                .into(binding.imgProduct)

            binding.txtNameProduct.text = itemPesanan.nameProduct
            val weightTemp = itemPesanan.qty * itemPesanan.productWeight
            binding.txtQtyProduct.text = "${itemPesanan.qty}" + " item" + " ($weightTemp) gr"
            if (itemPesanan.noteProduct == "") {
                binding.txtNoteProduct.text = "Catatan: -"
            } else {
                binding.txtNoteProduct.text = "Catatan: ${itemPesanan.noteProduct}"
            }
            binding.txtCurrentPrice.text =
                CustomFunctions().formatRupiah(itemPesanan.priceAt.toDouble())
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<ProductOrder>() {
        override fun areItemsTheSame(oldItem: ProductOrder, newItem: ProductOrder): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: ProductOrder, newItem: ProductOrder): Boolean {
            return oldItem == newItem
        }

    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PesananProductViewHolder =
        PesananProductViewHolder(
            ItemProductPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: PesananProductViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}