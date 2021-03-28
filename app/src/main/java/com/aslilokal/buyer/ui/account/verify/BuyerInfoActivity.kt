package com.aslilokal.buyer.ui.account.verify

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.buyer.BerandaActivity
import com.aslilokal.buyer.databinding.ActivityBuyerInfoBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class BuyerInfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBuyerInfoBinding
    private var datastore = AslilokalDataStore(this)
    private lateinit var viewmodel: VerificationViewModel

    private lateinit var selfPhoto: File

    private var FILE_SELF = "imgSelfBuyer"
    private val REQUEST_CODE = 13

    private lateinit var imgSelfBuyer: RequestBody

    //Data Form
    private lateinit var fotoSelf: MultipartBody.Part

    private lateinit var token: String
    private lateinit var idBuyerAccount: RequestBody
    private lateinit var nameBuyer: RequestBody
    private lateinit var noTelpBuyer: RequestBody
    private var postalCode: RequestBody? =
        "".toRequestBody("text/plain".toMediaTypeOrNull())
    private lateinit var addressBuyer: RequestBody

    private var STATUS_IMG_PICK = "statusimgpick"

    companion object {
        private const val IMAGE_CHOOSE = 1000
        private const val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuyerInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupViewmodel()
        setupObserver()
        hideProgress()

        runBlocking {
            token = datastore.read("TOKEN").toString()
            idBuyerAccount = datastore.read("USERNAME").toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
        }

        binding.btnPickSelf.setOnClickListener {
            STATUS_IMG_PICK = "imgSelf"
            onAlertDialog()
        }

        binding.btnKirim.setOnClickListener {
            setupData()
        }
    }

    private fun setupViewmodel() {
        viewmodel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(VerificationViewModel::class.java)
    }

    private fun setupData() {
        if (binding.etNameSeller.text.toString().isEmpty()) {
            binding.etNameSeller.error = "Harap isi nama kamu"
        }
        if (binding.etAddressSeller.text.toString().isEmpty()) {
            binding.etAddressSeller.error = "Ini digunakan untuk pengantaran"
        }
        if (binding.etNoTelp.text.toString().isEmpty()) {
            binding.etNoTelp.error = "Harap isi nomor yang bisa di hubungi ya"
        }
        if (binding.txtFotoSelfNameFile.text.toString() == "") {
            binding.btnPickSelf.error = "Jangan lupa pilih foto mu ya..."
        } else {
            //setup data
            nameBuyer = binding.etNameSeller.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            noTelpBuyer = binding.etNoTelp.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            addressBuyer = binding.etAddressSeller.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            imgSelfBuyer = selfPhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
            fotoSelf = MultipartBody.Part.createFormData(
                FILE_SELF,
                selfPhoto.name,
                imgSelfBuyer
            )

            runBlocking {
                viewmodel.postBiodataBuyer(
                    token,
                    null,
                    fotoSelf,
                    idBuyerAccount,
                    nameBuyer,
                    noTelpBuyer,
                    postalCode,
                    addressBuyer
                )
            }
        }
    }

    private fun setupObserver() {
        viewmodel.biodataResults.observe(this, { response ->
            Log.d("RESPONSE", response.data.toString())
            when (response) {
                is Resource.Success -> {
                    response.data.let { statusResponse ->
                        // di dalam buyer info set ISLOGIN = true if submit
                        Log.d("STATUSRESPONSE", statusResponse?.success.toString())
                        if (statusResponse?.success == false) {
                            hideProgress()
                            Toast.makeText(
                                binding.root.context,
                                statusResponse.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (statusResponse?.success == true) {
                            hideProgress()
                            GlobalScope.launch(Dispatchers.IO) {
                                datastore.save(
                                    "ISLOGIN",
                                    "true"
                                )
                            }
                            startActivity(Intent(this, BerandaActivity::class.java))
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

    private fun onAlertDialog() {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(binding.root.context)
        // set title
        builder.setTitle("Ambil gambar")
        //set content area
        builder.setMessage("Silahkan pilih gambar produk kamu")
        builder.setPositiveButton(
            "Dari Galery"
        ) { dialog, id ->
            // User clicked Update Now button
            chooseImageGallery()
        }

        builder.setNegativeButton(
            "Dari Kamera"
        ) { dialog, id ->
            // User cancelled the dialog
            chooseFromCamera()
        }
        builder.show()
    }

    private fun chooseImageGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (PermissionChecker.checkSelfPermission(
                    binding.root.context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PermissionChecker.PERMISSION_DENIED || PermissionChecker.checkSelfPermission(
                    binding.root.context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PermissionChecker.PERMISSION_DENIED
            ) {
                val permissions = arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissions(permissions, PERMISSION_CODE)
            } else {
                val intent =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                intent.type = "image/*"
                startActivityForResult(intent, IMAGE_CHOOSE)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_CHOOSE)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun chooseFromCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        when (STATUS_IMG_PICK) {
            "imgSelf" -> {
                selfPhoto = getPhotoFile(FILE_SELF)
                val providerFile =
                    FileProvider.getUriForFile(
                        binding.root.context,
                        "com.aslilokal.buyer.fileprovider",
                        selfPhoto
                    )
                takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
            }
        }
        Toast.makeText(this, "Choose From Camera", Toast.LENGTH_SHORT).show()
        if (takePhotoIntent.resolveActivity(binding.root.context.packageManager) != null) {
            startActivityForResult(takePhotoIntent, REQUEST_CODE)
        } else {
            Toast.makeText(this, "Camera ga bisa dibuka nih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage =
            binding.root.context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val imgStatus = STATUS_IMG_PICK
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            when (imgStatus) {
                "imgSelf" -> {
                    selfPhoto.let { imageFile ->
                        lifecycleScope.launch {
                            selfPhoto = Compressor.compress(binding.root.context, imageFile)
                            binding.txtFotoSelfNameFile.text = imageFile.name.toString()
                        }
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == IMAGE_CHOOSE && resultCode == RESULT_OK) {
            when (imgStatus) {
                "imgSelf" -> {
                    selfPhoto = File(getFileUri(data?.data))
                    selfPhoto.let { imageFile ->
                        lifecycleScope.launch {
                            selfPhoto = Compressor.compress(binding.root.context, imageFile)
                            binding.txtFotoSelfNameFile.text = imageFile.name.toString()
                        }
                    }
                }
            }
        }
    }

    private fun getFileUri(uri: Uri?): String {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { result ->
                binding.root.context.contentResolver.query(
                    result,
                    filePathColumn,
                    null,
                    null,
                    null
                )
            }
        cursor?.moveToFirst()
        val columnIndex: Int = cursor!!.getColumnIndexOrThrow(filePathColumn[0])
        val path: String = cursor.getString(columnIndex)
        cursor.close()
        return path
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