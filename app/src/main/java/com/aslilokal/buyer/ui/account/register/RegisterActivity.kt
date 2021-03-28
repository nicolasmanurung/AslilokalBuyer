package com.aslilokal.buyer.ui.account.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.buyer.databinding.ActivityRegisterBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.ui.account.login.LoginActivity
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var datastore = AslilokalDataStore(this)
    private lateinit var viewmodel: RegisterViewModel
    private lateinit var buyerData: AuthRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupObservers()

        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
        binding.lnrLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.buttonDaftar.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val passSeller = binding.etPassword.text.toString()
            if (email.isEmpty() || passSeller.isEmpty()) {
                Toast.makeText(
                    binding.root.context,
                    "Email dan Password Tidak boleh kosong",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else if (email.trim().isNotEmpty() || passSeller.trim().isNotEmpty()) {
                binding.llProgressBar.progressbar.visibility = View.VISIBLE
                buyerData = AuthRequest(
                    email,
                    passSeller,
                    false
                )

                lifecycleScope.launch {
                    viewmodel.postRegisterRequest(buyerData)
                }
            }
        }
    }

    private fun setupViewModel() {
        viewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(RegisterViewModel::class.java)
    }

    private fun setupObservers() {
        viewmodel.registers.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { registerResponse ->
                        if (registerResponse?.success == false) {
                            Toast.makeText(
                                binding.root.context,
                                registerResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (registerResponse?.success == true) {
                            if (!registerResponse.emailVerifyStatus) {
                                GlobalScope.launch(Dispatchers.IO) {
                                    datastore.save(
                                        "ISLOGIN",
                                        "false"
                                    )

                                    datastore.save(
                                        "USERNAME",
                                        registerResponse.username.toString()
                                    )

                                    datastore.save(
                                        "TOKEN",
                                        "JWT " + registerResponse.token.toString()
                                    )
                                }
                                binding.llProgressBar.progressbar.visibility = View.INVISIBLE

                                //val intent = Intent(this, VerifyEmailActivity::class.java)
//                                intent.putExtra("emailSeller", buyerData.emailBuyer)
//                                startActivity(intent)
//                                finish()
                            }
                        }
                    }
                }

                is Resource.Loading -> {
                    binding.llProgressBar.progressbar.visibility = View.VISIBLE
                }

                is Resource.Error -> {
                    binding.llProgressBar.progressbar.visibility = View.INVISIBLE
                    Toast.makeText(
                        binding.root.context,
                        "Maaf ada kesalahan",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }
}