package com.aslilokal.buyer.ui.adapter

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ItemKeranjangBinding
import com.aslilokal.buyer.model.remote.response.ItemCart
import com.aslilokal.buyer.ui.detail.product.DetailProductActivity
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_PRODUCT_URL
import com.aslilokal.buyer.utils.CustomFunctions
import com.bumptech.glide.Glide

class CartAdapter : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {
    //var onCheckItem = ArrayList<ItemCart>()
    var onCheckItem: ((ItemCart, Boolean) -> Unit)? = null
    var onAddItem: ((String, Int) -> Unit)? = null
    var onSubItem: ((String, Int) -> Unit)? = null

    inner class CartViewHolder(private val binding: ItemKeranjangBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemCart: ItemCart) {
            binding.txtValueNameProduct.text = itemCart.nameProduct
            binding.txtValuePriceProduct.text =
                CustomFunctions().formatRupiah(itemCart.productPrice.toDouble())

            binding.lnrCatatan.visibility = View.GONE
            binding.txtCatatan.setOnClickListener {
                binding.txtCatatan.visibility = View.VISIBLE
            }
            binding.txtCatatan.setOnClickListener {
                binding.lnrCatatan.visibility = View.VISIBLE
            }

            binding.etValueProduct.setText(itemCart.qtyProduct.toString())
            binding.etValueProduct.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (count == 0) {
                        binding.etValueProduct.setText("1")
                    } else {
                        if (s.toString().toInt() > 1) {
                            binding.btnSubstract.setColorFilter(
                                ContextCompat.getColor(
                                    binding.root.context,
                                    R.color.primaryColor
                                )
                            )
                        } else {
                            binding.btnSubstract.setColorFilter(
                                ContextCompat.getColor(
                                    binding.root.context,
                                    R.color.text_shadow
                                )
                            )
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    val result: String = s.toString().replace(" ", "")
                    val tempData = s.toString()
                    if (tempData.startsWith("0")) {
                        binding.etValueProduct.setText("1")
                    }
                    if (s.toString() != result) {
                        binding.etValueProduct.setText(result)
                        binding.etValueProduct.setSelection(result.length)
                    }
                }
            })

            binding.checkboxProduct.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    val tempData = itemCart
                    tempData.qtyProduct = binding.etValueProduct.text.toString().toInt()
                    onCheckItem?.invoke(tempData, true)
                } else {
                    val tempData = itemCart
                    tempData.qtyProduct = binding.etValueProduct.text.toString().toInt()
                    onCheckItem?.invoke(tempData, false)
                }
            }

            binding.btnSubstract.setOnClickListener {
                var currentText = binding.etValueProduct.text.toString().toInt()
                if (currentText != 1) {
                    currentText--
                    onSubItem?.invoke(itemCart._id.toString(), currentText)
                }
                binding.etValueProduct.setText(currentText.toString())
            }

            binding.btnAdd.setOnClickListener {
                var currentText = binding.etValueProduct.text.toString().toInt()
                currentText++
                onAddItem?.invoke(itemCart._id.toString(), currentText)
                binding.etValueProduct.setText(currentText.toString())
            }

            Glide.with(itemView.context)
                .load(BUCKET_PRODUCT_URL + itemCart.imgProduct)
                .placeholder(R.drawable.loading_animation)
                .into(binding.imgThumbnailProduct)

            itemView.setOnClickListener {
                var intent = Intent(binding.root.context, DetailProductActivity::class.java)
                intent.putExtra("idProduct", itemCart.idProduct)
                intent.putExtra("idShop", itemCart.idSellerAccount)
                binding.root.context.startActivity(intent)
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<ItemCart>() {
        override fun areItemsTheSame(oldItem: ItemCart, newItem: ItemCart): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: ItemCart, newItem: ItemCart): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder =
        CartViewHolder(
            ItemKeranjangBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int = differ.currentList.size

}