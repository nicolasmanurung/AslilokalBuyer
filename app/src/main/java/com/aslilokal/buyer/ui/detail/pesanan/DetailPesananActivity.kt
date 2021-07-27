package com.aslilokal.buyer.ui.detail.pesanan

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ActivityDetailPesananBinding
import com.aslilokal.buyer.databinding.ItemMicroPesananBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.response.DetailOrder
import com.aslilokal.buyer.model.remote.response.ProductOrder
import com.aslilokal.buyer.ui.adapter.PesananProductAdapter
import com.aslilokal.buyer.ui.detail.DetailViewModel
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_USR_EVIDANCE_URL
import com.aslilokal.buyer.utils.CustomFunctions
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class DetailPesananActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPesananBinding
    private lateinit var productPesananAdapter: PesananProductAdapter
    private lateinit var detailViewmodel: DetailViewModel
    private lateinit var datastore : AslilokalDataStore
    private lateinit var idOrder: String
    private lateinit var token: String
    private lateinit var imgKey: String

    private lateinit var filePhoto: File
    private var FILE_NAME: String = "orderAttachmentImg"
    private lateinit var imageRequestFile: RequestBody
    private lateinit var foto: MultipartBody.Part
    private var requestmethod: String? = ""

    companion object {
        private val IMAGE_CHOOSE = 1000
        private val PERMISSION_CODE = 1001
        private val IMAGE_CAMERA = 13
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPesananBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        setupDetailViewModel()
        setupRvProductsPesanan()
        showLoadingProgress()
        setupObserverDetailOrder()

        idOrder = intent.getStringExtra("idOrder").toString()

        runBlocking {
            token = datastore.read("TOKEN").toString()
            detailViewmodel.getDetailProduct(
                token,
                idOrder
            )
        }

        binding.imgEvidancePayment.setOnClickListener {
            onAlertClickImage()
        }

        binding.btnAttachEvidance.setOnClickListener {
            onAlertTakeImage()
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setupRvProductsPesanan() {
        productPesananAdapter = PesananProductAdapter()
        binding.rvProductPesanan.apply {
            adapter = productPesananAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupRvMicroPembayaran(listProducts: ArrayList<ProductOrder>) {
        val itemCartAdapter = MicroPembayaranAdapter(listProducts)
        binding.rvMicroPesanan.apply {
            adapter = itemCartAdapter
            layoutManager = LinearLayoutManager(binding.root.context)
        }
    }

    private fun setupDetailViewModel() {
        detailViewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(DetailViewModel::class.java)
    }

    private fun setupObserverDetailOrder() {
        detailViewmodel.detailOrders.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideLoadingProgress()
                    response.data?.result.let { detailOrderResponse ->
                        if (detailOrderResponse == null) {
                            Toast.makeText(this, "Sepertinya ada kesalahan", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            showDataDetail(detailOrderResponse)
                        }
                    }
                }

                is Resource.Loading -> {
                    showLoadingProgress()
                }

                is Resource.Error -> {
                    hideLoadingProgress()
                    Toast.makeText(this, response.message.toString(), Toast.LENGTH_SHORT)
                        .show()
                }
            }

        })
    }

    @SuppressLint("SetTextI18n")
    private fun showDataDetail(response: DetailOrder) {
        when (response.statusOrder) {
            "acceptrequired" -> {
                binding.txtNoteBatal.visibility = View.GONE
                binding.lnrBatal.visibility = View.GONE
            }
            "process" -> {
                binding.txtNoteBatal.visibility = View.GONE
                binding.lnrBatal.visibility = View.GONE
            }
            "delivered" -> {
                binding.txtNoteBatal.visibility = View.GONE
                binding.lnrBatal.visibility = View.GONE
            }
            "done" -> {
                binding.txtNoteBatal.visibility = View.GONE
                binding.lnrBatal.visibility = View.GONE
            }
        }
        binding.txtNameBuyer.text = response.nameBuyer
        binding.txtAlamatBuyer.text = response.addressBuyer
        binding.txtNoTelp.text = response.numberTelp
        productPesananAdapter.differ.submitList(response.products.toList())
        setupRvMicroPembayaran(response.products)
        var tempSumTotalValue = 0
        for (item in response.products) {
            tempSumTotalValue = item.priceAt * item.qty
        }
        if (response.voucherId.isNotEmpty()) {
            binding.lnrVoucherApply.visibility = View.VISIBLE
            var tempVoucherActualPrice =
                tempSumTotalValue - (response.totalPayment - response.courierCost)
            binding.txtVoucherActualRupiah.text =
                "-" + CustomFunctions().formatRupiah(tempVoucherActualPrice.toDouble())
        }

        if (response.courierType != "CUSTOM") {
            binding.txtNameCourier.text = response.courierType
            binding.txtNameExpedition.text = response.courierType
            binding.txtNoResi.text = response.resiCode
        } else {
            binding.txtNameExpedition.text = "Penjual"
            binding.txtNoResi.visibility = View.GONE
        }
        binding.txtDeliveryPrice.text =
            CustomFunctions().formatRupiah(response.courierCost.toDouble())
        binding.txtSumPrice.text = CustomFunctions().formatRupiah(response.totalPayment.toDouble())
        binding.txtTimeRemainingOrder.text =
            CustomFunctions().isoTimeToAddDaysTime(response.orderAt)
        Log.d("TIMEORDER", CustomFunctions().isoTimeToAddDaysTime(response.orderAt))

        if (response.imgPayment.isNullOrEmpty()) {
            requestmethod = "post"
            binding.txtEmpty.visibility = View.VISIBLE
            binding.btnAttachEvidance.visibility = View.VISIBLE
            binding.imgEvidancePayment.visibility = View.GONE
        } else {
            binding.imgEvidancePayment.visibility = View.VISIBLE
            Glide.with(binding.root.context)
                .load(BUCKET_USR_EVIDANCE_URL + response.imgPayment)
                .placeholder(R.drawable.loading_animation)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .priority(Priority.HIGH)
                .into(binding.imgEvidancePayment)
            imgKey = response.imgPayment
            requestmethod = "put"
            binding.txtEmpty.visibility = View.GONE
            binding.btnAttachEvidance.visibility = View.GONE
        }
    }

    private fun showLoadingProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun hideLoadingProgress() {
        binding.llProgressBar.progressbar.visibility = View.INVISIBLE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun onAlertClickImage() {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Kelola Gambar")
        builder.setMessage("Mau apa?")
        builder.setPositiveButton("Ganti gambar") { dialog, id ->
            onAlertTakeImage()
        }

        builder.setNegativeButton("Lihat gambar") { dialog, id ->
            //Toast.makeText(this, imageProduct, Toast.LENGTH_SHORT).show()
        }
        builder.show()
    }

    private fun onAlertTakeImage() {
        //Instantiate builder variable
        val builder = AlertDialog.Builder(binding.root.context)
        // set title
        builder.setTitle("Ambil gambar")
        //set content area
        builder.setMessage("Silahkan pilih gambar bukti pembayaranmu")
        //set negative button
        builder.setPositiveButton(
            "Dari Galery"
        ) { dialog, id ->
            // User clicked Update Now button
            chooseImageGallery()
        }
        //set positive button
        builder.setNegativeButton(
            "Dari Kamera"
        ) { dialog, id ->
            // User cancelled the dialog
            chooseFromCamera()
        }
        builder.show()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun chooseFromCamera() {
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        filePhoto = getPhotoFile(FILE_NAME)
        val providerFile =
            FileProvider.getUriForFile(this, "com.aslilokal.buyer.fileprovider", filePhoto)
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerFile)
        if (takePhotoIntent.resolveActivity(this.packageManager) != null) {
            startActivityForResult(takePhotoIntent, IMAGE_CAMERA)
        } else {
            Toast.makeText(this, "Camera ga bisa dibuka nih", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPhotoFile(fileName: String): File {
        val directoryStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName, ".jpg", directoryStorage)
    }

    private fun chooseImageGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_DENIED
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

    private fun getFileUri(uri: Uri?): String {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor? =
            uri?.let { contentResolver.query(it, filePathColumn, null, null, null) }
        cursor?.moveToFirst()
        val columnIndex: Int = cursor!!.getColumnIndex(filePathColumn[0])
        val path: String = cursor.getString(columnIndex)
        cursor.close()
        return path
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            filePhoto.let { imageFile ->
                lifecycleScope.launch {
                    filePhoto = Compressor.compress(binding.root.context, imageFile)
                    val takenPhoto =
                        CustomFunctions().rotateBitmapOrientation(filePhoto.absolutePath)
                    binding.imgEvidancePayment.visibility = View.VISIBLE
                    binding.imgEvidancePayment.setImageBitmap(takenPhoto)
                    imageRequestFile = filePhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
                    foto = MultipartBody.Part.createFormData(
                        FILE_NAME,
                        filePhoto.name,
                        imageRequestFile
                    )
                    setupPutImage(requestmethod.toString())
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == IMAGE_CHOOSE && resultCode == Activity.RESULT_OK) {
            filePhoto = File(getFileUri(data?.data))
            filePhoto.let { imageFile ->
                lifecycleScope.launch {
                    filePhoto = Compressor.compress(binding.root.context, imageFile)
                    binding.imgEvidancePayment.visibility = View.VISIBLE
                    binding.imgEvidancePayment.setImageURI(data?.data)

                    imageRequestFile = filePhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
                    foto = MultipartBody.Part.createFormData(
                        FILE_NAME,
                        filePhoto.name,
                        imageRequestFile
                    )
                    setupPutImage(requestmethod.toString())
                }
            }
        }
    }

    private fun setupPutImage(condition: String) = CoroutineScope(Dispatchers.Main).launch {
        showLoadingProgress()
        try {
            if (condition == "put") {
                val response = RetrofitInstance.api.putAttachmentOrderImg(
                    token,
                    idOrder,
                    imgKey.toRequestBody("text/plain".toMediaTypeOrNull()),
                    foto
                )
                if (response.body()?.success == true) {
                    hideLoadingProgress()
                    binding.root.context.cacheDir.deleteRecursively()
                    Toast.makeText(
                        binding.root.context,
                        "Berhasil mengupdate",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    hideLoadingProgress()
                    Glide.with(binding.root.context)
                        .load(BUCKET_USR_EVIDANCE_URL + imgKey)
                        .placeholder(R.drawable.loading_animation)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .priority(Priority.HIGH)
                        .into(binding.imgEvidancePayment)
                    Toast.makeText(
                        binding.root.context,
                        "Jaringan lemah, coba lagi...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (condition == "post") {
                val response = RetrofitInstance.api.postAttachmentOrderImg(token, idOrder, foto)
                if (response.body()?.success == true) {
                    hideLoadingProgress()
                    binding.root.context.cacheDir.deleteRecursively()
                    Toast.makeText(
                        binding.root.context,
                        "Berhasil mengupdate",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnAttachEvidance.visibility = View.GONE
                    binding.imgEvidancePayment.visibility = View.GONE
                } else {
                    hideLoadingProgress()
                    binding.imgEvidancePayment.setImageResource(android.R.color.transparent)
                    Toast.makeText(
                        binding.root.context,
                        "Jaringan lemah",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (exception: Exception) {
            hideLoadingProgress()
            when (exception) {
                is IOException -> Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                    Toast.makeText(
                        binding.root.context,
                        exception.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("KESALAHAN", exception.toString())
                }
            }
        }
    }

    class MicroPembayaranAdapter(private val listItem: ArrayList<ProductOrder>) :
        RecyclerView.Adapter<MicroPembayaranAdapter.MicroPembayaranViewHolder>() {
        inner class MicroPembayaranViewHolder(private val binding: ItemMicroPesananBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("SetTextI18n")
            fun bind(itemProduct: ProductOrder) {
                binding.txtNameProduct.text = itemProduct.nameProduct
                binding.txtSumProduct.text = itemProduct.qty.toString() + "X"
                binding.txtPriceProduct.text =
                    CustomFunctions().formatRupiah(itemProduct.priceAt.toDouble())
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MicroPembayaranViewHolder = MicroPembayaranViewHolder(
            ItemMicroPesananBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        override fun onBindViewHolder(holder: MicroPembayaranViewHolder, position: Int) {
            holder.bind(listItem[position])
        }

        override fun getItemCount(): Int = listItem.size
    }
}