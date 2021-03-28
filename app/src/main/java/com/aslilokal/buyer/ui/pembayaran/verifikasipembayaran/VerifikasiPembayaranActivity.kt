package com.aslilokal.buyer.ui.pembayaran.verifikasipembayaran

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aslilokal.buyer.BerandaActivity
import com.aslilokal.buyer.databinding.ActivityVerifikasiPembayaranBinding
import com.aslilokal.buyer.utils.AslilokalDataStore

class VerifikasiPembayaranActivity : AppCompatActivity() {
    private lateinit var binding: ActivityVerifikasiPembayaranBinding
    private var datastore = AslilokalDataStore(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVerifikasiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnConfirmPayment.setOnClickListener {
            startActivity(Intent(this, BerandaActivity::class.java))
            finish()
        }
    }
}