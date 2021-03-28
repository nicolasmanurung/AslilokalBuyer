package com.aslilokal.buyer.ui.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.databinding.ItemProductBinding
import com.aslilokal.buyer.model.remote.response.Product
import com.aslilokal.buyer.ui.detail.product.DetailProductActivity
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_PRODUCT_URL
import com.aslilokal.buyer.utils.CustomFunctions
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.tuonbondol.textviewutil.strike

class ProductGridAdapter :
    RecyclerView.Adapter<ProductGridAdapter.GridProductViewHolder>() {

    inner class GridProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(product: Product) {
            binding.imgProduct.setBackgroundColor(Color.rgb(217, 217, 217))

            Glide.with(itemView.context)
                .load(BUCKET_PRODUCT_URL + product.imgProduct)
                .priority(Priority.HIGH)
                .into(binding.imgProduct)

            binding.txtCurrentPrice.text =
                CustomFunctions().formatRupiah(product.priceProduct.toDouble())
            binding.txtNameProduct.text = product.nameProduct
            when (product.promoPrice?.toIntOrNull()) {
                0 -> {
                    binding.lnrPromo.visibility = View.INVISIBLE
                    binding.txtPromoPrice.visibility = View.GONE
                }
                null -> {
                    binding.lnrPromo.visibility = View.INVISIBLE
                    binding.txtPromoPrice.visibility = View.GONE
                }
                else -> {
                    binding.txtPromoPrice.text =
                        CustomFunctions().formatRupiah(product.priceProduct.toDouble())
                    binding.txtCurrentPrice.text =
                        CustomFunctions().formatRupiah(product.promoPrice.toDouble())
                    binding.lnrPromo.visibility = View.VISIBLE
                    val sumCount =
                        (product.promoPrice.toFloat().div(product.priceProduct.toFloat()))
                    val countPercentage = (100 - (sumCount.times(100)).toInt())
                    binding.txtPromoPrice.strike()
                    binding.percentagePromo.text = "$countPercentage %"
                }
            }
            itemView.setOnClickListener {
                val intent = Intent(binding.root.context, DetailProductActivity::class.java)
                intent.putExtra("idProduct", product._id)
                intent.putExtra("idShop", product.idSellerAccount)
                binding.root.context.startActivity(intent)
            }
        }
    }


    private val differCallback = object : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridProductViewHolder =
        GridProductViewHolder(
            ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: GridProductViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.bind(product)
    }

    override fun getItemCount(): Int = differ.currentList.size
}