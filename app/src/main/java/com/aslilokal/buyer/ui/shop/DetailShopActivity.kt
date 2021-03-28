package com.aslilokal.buyer.ui.shop

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.aslilokal.buyer.databinding.ActivityDetailShopBinding

class DetailShopActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailShopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailShopBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}