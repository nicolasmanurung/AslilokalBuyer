package com.aslilokal.buyer.ui.account.verify

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.aslilokal.buyer.databinding.ActivityEmailVerificationBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class EmailVerificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEmailVerificationBinding
    private lateinit var datastore: AslilokalDataStore
    private lateinit var viewmodel: VerificationViewModel
    private lateinit var emailBuyer: String
    private lateinit var verifyToken: String
    private lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        hideProgress()
        setupViewModel()
        getTokenObservable()
        postResubmitObservable()

        runBlocking {
            token = datastore.read("TOKEN").toString()
        }

        emailBuyer = intent.getStringExtra("emailBuyer")!!
        binding.tvEmail.text = emailBuyer

        verifyToken = binding.etCode.text.toString()

        binding.etCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count == 6) {
                    verifyToken = s.toString()
                    //Toast.makeText(binding.root.context, s.toString(), Toast.LENGTH_SHORT).show()
                    binding.btnNextVerification.visibility = View.VISIBLE

                    CoroutineScope(Dispatchers.Main).launch {
                        showProgress()
                        viewmodel.getVerifyTokenCode(token, verifyToken)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val result: String = s.toString().replace(" ", "")
                if (s.toString() != result) {
                    binding.etCode.setText(result)
                    binding.etCode.setSelection(result.length)
                }
            }
        })

        countDownTimer.start()

        binding.btnNextVerification.setOnClickListener {
            showProgress()
            verifyToken = binding.etCode.text.toString()
            CoroutineScope(Dispatchers.Main).launch {
                viewmodel.getVerifyTokenCode(token, verifyToken)
            }
        }

        binding.txtResend.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                viewmodel.postResubmitSendTokenVerify(token)
            }
        }

    }

    private fun getTokenObservable() {
        viewmodel.emailVerivications.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgress()
                    response.data.let { tokenVerifyResponse ->
                        if (tokenVerifyResponse?.success == false) {
                            Toast.makeText(
                                binding.root.context,
                                tokenVerifyResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (tokenVerifyResponse?.success == true) {
                            startActivity(Intent(this, BuyerInfoActivity::class.java))
                            finish()
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgress()
                }

                is Resource.Error -> {
                    hideProgress()
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun postResubmitObservable() {
        viewmodel.tokenResubmits.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgress()
                    response.data.let { resubmitTokenResponse ->
                        if (resubmitTokenResponse?.success == false) {
                            Toast.makeText(
                                binding.root.context,
                                resubmitTokenResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (resubmitTokenResponse?.success == true) {
                            Toast.makeText(
                                binding.root.context,
                                resubmitTokenResponse.message,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            binding.txtResend.visibility = View.GONE
                            binding.tvReceive.visibility = View.VISIBLE
                            binding.tvTime.visibility = View.VISIBLE
                            countDownTimer.start()
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgress()
                }

                is Resource.Error -> {
                    hideProgress()
                    Toast.makeText(
                        binding.root.context,
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    private fun setupViewModel() {
        viewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(VerificationViewModel::class.java)
    }

    private val countDownTimer = object : CountDownTimer(30000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            binding.txtResend.visibility = View.GONE
            val secondLeft =
                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                )
            binding.tvTime.text = String.format(
                "Kirim ulang kode dalam %d:%d",
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                secondLeft
            )
        }

        override fun onFinish() {
            binding.txtResend.visibility = View.VISIBLE
            binding.tvReceive.visibility = View.GONE
            binding.tvTime.text = ""
        }
    }

    private fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}