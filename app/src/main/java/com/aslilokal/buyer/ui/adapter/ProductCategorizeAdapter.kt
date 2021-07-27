package com.aslilokal.buyer.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ItemProductCategorizeBinding
import com.aslilokal.buyer.model.remote.response.Product
import com.aslilokal.buyer.ui.detail.product.DetailProductActivity
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_PRODUCT_URL
import com.aslilokal.buyer.utils.CustomFunctions
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tuonbondol.textviewutil.strike

class ProductCategorizeAdapter :
    RecyclerView.Adapter<ProductCategorizeAdapter.ProductCategorizeViewHolder>() {

    inner class ProductCategorizeViewHolder(private val binding: ItemProductCategorizeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            Glide.with(itemView.context)
                .load(BUCKET_PRODUCT_URL + product.imgProduct)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .priority(Priority.HIGH)
                .placeholder(R.drawable.loading_animation)
                .into(binding.imgProduct)

            binding.txtCurrentPrice.text =
                CustomFunctions().formatRupiah(product.priceProduct.toDouble())
            binding.txtNameProduct.text = product.nameProduct
            binding.txtDescProduct.text = product.descProduct

            when (product.promoPrice?.toIntOrNull()) {
                0 -> {
                    binding.txtPromoPrice.visibility = View.GONE
                }
                null -> {
                    binding.txtPromoPrice.visibility = View.GONE
                }
                else -> {
                    binding.txtPromoPrice.text =
                        CustomFunctions().formatRupiah(product.priceProduct.toDouble())
                    binding.txtCurrentPrice.text =
                        CustomFunctions().formatRupiah(product.promoPrice.toDouble())
                    binding.txtPromoPrice.strike()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCategorizeViewHolder =
        ProductCategorizeViewHolder(
            ItemProductCategorizeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ProductCategorizeViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}