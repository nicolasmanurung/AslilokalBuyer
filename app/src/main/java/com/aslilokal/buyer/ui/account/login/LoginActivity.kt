package com.aslilokal.buyer.ui.account.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.buyer.BerandaActivity
import com.aslilokal.buyer.databinding.ActivityLoginBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.request.AuthRequest
import com.aslilokal.buyer.ui.account.register.RegisterActivity
import com.aslilokal.buyer.ui.account.verify.BuyerInfoActivity
import com.aslilokal.buyer.ui.account.verify.EmailVerificationActivity
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var datastore = AslilokalDataStore(this)
    private lateinit var viewmodel: LoginViewModel
    private lateinit var buyerData: AuthRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewModel()
        setupObservers()

        binding.lnrDaftar.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
        binding.buttonLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val passBuyer = binding.etPassword.text.toString()
            if (email.isEmpty() || passBuyer.isEmpty()) {
                Toast.makeText(
                    binding.root.context,
                    "Email dan Password Tidak boleh kosong",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else if (email.trim().isNotEmpty() || passBuyer.trim().isNotEmpty()) {
                binding.llProgressBar.progressbar.visibility = View.VISIBLE
                buyerData = AuthRequest(
                    email,
                    passBuyer,
                    null
                )
                lifecycleScope.launch {
                    viewmodel.postLoginRequest(buyerData)
                }
            }
        }

    }

    private fun setupViewModel() {
        viewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(LoginViewModel::class.java)
    }

    private fun setupObservers() {
        viewmodel.logins.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { loginResponse ->
                        if (loginResponse?.success == false) {
                            Toast.makeText(
                                binding.root.context,
                                loginResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (loginResponse?.success == true) {
                            if (loginResponse.emailVerifyStatus && loginResponse.biodataVerifyStatus) {
                                GlobalScope.launch(Dispatchers.IO) {
                                    datastore.save(
                                        "ISLOGIN",
                                        "true"
                                    )

                                    datastore.save(
                                        "USERNAME",
                                        loginResponse.username.toString()
                                    )

                                    datastore.save(
                                        "TOKEN",
                                        "JWT " + loginResponse.token.toString()
                                    )
                                }

                                binding.llProgressBar.progressbar.visibility = View.INVISIBLE

                                val intent = Intent(this, BerandaActivity::class.java)
                                startActivity(intent)
                                finish()

                            } else if (!loginResponse.emailVerifyStatus && !loginResponse.biodataVerifyStatus) {
                                GlobalScope.launch(Dispatchers.IO) {
                                    datastore.save(
                                        "ISLOGIN",
                                        "false"
                                    )

                                    datastore.save(
                                        "USERNAME",
                                        loginResponse.username.toString()
                                    )

                                    datastore.save(
                                        "TOKEN",
                                        "JWT " + loginResponse.token.toString()
                                    )
                                }
                                binding.llProgressBar.progressbar.visibility = View.INVISIBLE
                                val intent = Intent(this, EmailVerificationActivity::class.java)
                                intent.putExtra("emailBuyer", buyerData.emailBuyer)
                                startActivity(intent)
                                finish()
                            } else if (loginResponse.emailVerifyStatus && !loginResponse.biodataVerifyStatus) {
                                GlobalScope.launch(Dispatchers.IO) {
                                    datastore.save(
                                        "ISLOGIN",
                                        "false"
                                    )

                                    datastore.save(
                                        "USERNAME",
                                        loginResponse.username.toString()
                                    )

                                    datastore.save(
                                        "TOKEN",
                                        "JWT " + loginResponse.token.toString()
                                    )
                                }
                                binding.llProgressBar.progressbar.visibility = View.INVISIBLE
                                val intent = Intent(this, BuyerInfoActivity::class.java)
                                intent.putExtra("emailBuyer", buyerData.emailBuyer)
                                startActivity(intent)
                                finish()
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