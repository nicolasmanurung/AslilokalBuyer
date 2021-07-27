package com.aslilokal.buyer.ui.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ItemShopSearchBinding
import com.aslilokal.buyer.model.remote.response.Shop
import com.aslilokal.buyer.ui.shop.DetailShopActivity
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_USR_URL
import com.bumptech.glide.Glide

class ShopAdapter : RecyclerView.Adapter<ShopAdapter.ShopViewHolder>() {
    inner class ShopViewHolder(private val binding: ItemShopSearchBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(shop: Shop) {
            Glide.with(binding.root.context)
                .load(BUCKET_USR_URL + shop.imgShop)
                .placeholder(R.drawable.loading_animation)
                .into(binding.circleImageView)

            binding.txtNameLapo.text = shop.nameShop
            binding.txtAddressLapo.text = shop.addressShop
            when (shop.shopTypeStatus) {
                "taput" -> {
                    binding.txtNameAsliUmkm.text = "Asli Tapanuli Utara"
                }
                "tapteng" -> {
                    binding.txtNameAsliUmkm.text = "Asli Tapanuli Tengah"
                }
                else -> {
                    binding.verifiedImage.visibility = View.GONE
                    binding.txtNameAsliUmkm.text = shop.rajaOngkir.city_name
                }
            }
            itemView.setOnClickListener {
                val intent = Intent(binding.root.context, DetailShopActivity::class.java)
                intent.putExtra("idShop", shop.idSellerAccount)
                binding.root.context.startActivity(intent)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<Shop>() {
        override fun areItemsTheSame(oldItem: Shop, newItem: Shop): Boolean {
            return oldItem.idSellerAccount == newItem.idSellerAccount
        }

        override fun areContentsTheSame(oldItem: Shop, newItem: Shop): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder =
        ShopViewHolder(
            ItemShopSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size
}