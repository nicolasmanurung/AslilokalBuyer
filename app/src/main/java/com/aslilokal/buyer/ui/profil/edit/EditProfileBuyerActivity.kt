package com.aslilokal.buyer.ui.profil.edit

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
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.ActivityEditProfileBuyerBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.remote.request.BiodataRequest
import com.aslilokal.buyer.model.remote.response.City
import com.aslilokal.buyer.model.remote.response.DetailBiodata
import com.aslilokal.buyer.model.remote.response.RajaOngkirAddress
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.aslilokal.buyer.utils.Constants.Companion.BUCKET_USR_URL
import com.aslilokal.buyer.utils.Constants.Companion.RO_KEY_ID
import com.aslilokal.buyer.utils.CustomFunctions
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.aslilokal.buyer.viewmodel.ROViewModel
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
import java.util.*

class EditProfileBuyerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBuyerBinding
    private lateinit var username: String
    private lateinit var token: String
    private lateinit var ROViewmodel: ROViewModel
    private lateinit var editViewmodel: EditProfileViewModel
    private lateinit var currentDataBuyer: DetailBiodata
    private var isSellectAutocomplete: Boolean? = false
    private lateinit var listCity: ArrayList<City>
    private lateinit var datastore : AslilokalDataStore
    private lateinit var filePhoto: File
    private var FILE_NAME: String = "imgSelfBuyer"
    private lateinit var imageRequestFile: RequestBody
    private lateinit var foto: MultipartBody.Part
    private lateinit var imgKey: String

    companion object {
        private val IMAGE_CHOOSE = 1000
        private val PERMISSION_CODE = 1001
        private val IMAGE_CAMERA = 13
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBuyerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        datastore = AslilokalDataStore(binding.root.context)
        setupEditProfileViewmodel()
        setupROViewmodel()
        setupPutObservers()
        setupROObserver()

        CoroutineScope(Dispatchers.Main).launch {
            token = datastore.read("TOKEN").toString()
            username = datastore.read("USERNAME").toString()
            ROViewmodel.getCitiesByRO(RO_KEY_ID)
        }

        currentDataBuyer =
            intent.getParcelableExtra<DetailBiodata>("currentDataBuyer") as DetailBiodata
        setupUI()

        binding.originLocation.addTextChangedListener {
            if (it?.isNotEmpty() == true) {
                binding.txtChangeProvince.visibility = View.VISIBLE
            }
//            if (getCityFromAutocomplete(it.toString()) == null) {
//                isSellectAutocomplete = false
//            }
            else {
                isSellectAutocomplete = false
                binding.txtChangeProvince.visibility = View.GONE
            }
        }

        binding.txtChangeProvince.setOnClickListener {
            isSellectAutocomplete = false
            binding.originLocation.setText("")
        }

        binding.btnSaveData.setOnClickListener {
            setupData()
        }

        binding.profileImage.setOnClickListener {
            onAlertClickImage()
        }

    }

    private fun onAlertClickImage() {
        val builder = AlertDialog.Builder(binding.root.context)
        builder.setTitle("Kelola Gambar")
        builder.setMessage("Mau apa?")
        builder.setPositiveButton("Ganti gambar") { dialog, id ->
            onAlertTakeImage()
        }

//        builder.setNegativeButton("Lihat gambar") { dialog, id ->
//            //Toast.makeText(this, imageProduct, Toast.LENGTH_SHORT).show()
//        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == IMAGE_CAMERA && resultCode == Activity.RESULT_OK) {
            filePhoto.let { imageFile ->
                lifecycleScope.launch {
                    filePhoto = Compressor.compress(binding.root.context, imageFile)
                    val takenPhoto =
                        CustomFunctions().rotateBitmapOrientation(filePhoto.absolutePath)
                    binding.profileImage.visibility = View.VISIBLE
                    binding.profileImage.setImageBitmap(takenPhoto)
                    imageRequestFile = filePhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
                    foto = MultipartBody.Part.createFormData(
                        FILE_NAME,
                        filePhoto.name,
                        imageRequestFile
                    )
                    setupPutImage()
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
                    binding.profileImage.visibility = View.VISIBLE
                    binding.profileImage.setImageURI(data?.data)

                    imageRequestFile = filePhoto.asRequestBody("image/jpg".toMediaTypeOrNull())
                    foto = MultipartBody.Part.createFormData(
                        FILE_NAME,
                        filePhoto.name,
                        imageRequestFile
                    )
                    setupPutImage()
                }
            }
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

    private fun setupPutImage() = CoroutineScope(Dispatchers.Main).launch {
        showProgress()
        try {
            val response = RetrofitInstance.api.putUpdateSelfImg(
                token,
                imgKey.toRequestBody("text/plain".toMediaTypeOrNull()),
                foto
            )
            if (response.body()?.success == true) {
                hideProgress()
                binding.root.context.cacheDir.deleteRecursively()
                Toast.makeText(
                    binding.root.context,
                    "Berhasil mengupdate",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                hideProgress()
                Glide.with(binding.root.context)
                    .load(BUCKET_USR_URL + imgKey)
                    .placeholder(R.drawable.loading_animation)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .priority(Priority.HIGH)
                    .into(binding.profileImage)
                Toast.makeText(
                    binding.root.context,
                    "Jaringan lemah, coba lagi...",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (exception: Exception) {
            hideProgress()
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

    private fun setupROViewmodel() {
        ROViewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.apiRO))
        ).get(ROViewModel::class.java)
    }

    private fun setupEditProfileViewmodel() {
        editViewmodel = ViewModelProvider(
            this,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(EditProfileViewModel::class.java)
    }

    private fun setupPutObservers() {
        editViewmodel.putBiodatas.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgress()
                    if (response.data?.success == true) {
                        onBackPressed()
                        Toast.makeText(
                            this,
                            "Berhasil di ubah",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                is Resource.Loading -> {
                    showProgress()
                }

                is Resource.Error -> {
                    hideProgress()
                    response.message?.let { message ->
                        Toast.makeText(
                            this,
                            "Sepertinya jaringanmu lemah, coba refresh...",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    private fun setupROObserver() {
        ROViewmodel.citiesResults.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    response.data.let { cityResponse ->
                        hideProgress()
                        initSpinner(cityResponse?.rajaongkir?.results ?: return@observe)
                    }
                }

                is Resource.Loading -> {
                    showProgress()
                }

                is Resource.Error -> {
                    Toast.makeText(
                        binding.root.context,
                        response.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun initSpinner(cityResults: ArrayList<City>) {
        listCity = cityResults
        val cities = mutableListOf<String>()
        for (i in cityResults.indices) cities.add(
            cityResults[i].province + ", " + cityResults[i].city_name ?: ""
        )
        val cityAdapter = ArrayAdapter(
            binding.root.context,
            android.R.layout.simple_spinner_dropdown_item,
            cities
        )
        binding.originLocation.setAdapter(cityAdapter)
        binding.originLocation.setOnItemClickListener { parent, view, position, id ->
            isSellectAutocomplete = true
        }

        if (currentDataBuyer.rajaOngkir != null) {
            binding.originLocation.setText(currentDataBuyer.rajaOngkir!!.province + ", " + currentDataBuyer.rajaOngkir!!.city_name)
        }

        if (currentDataBuyer.postalCodeInput != null) {
            binding.etPostalCode.setText(currentDataBuyer.postalCodeInput.toString())
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI() {
        Glide.with(binding.root.context)
            .load(BUCKET_USR_URL + currentDataBuyer.imgSelfBuyer)
            .placeholder(R.drawable.loading_animation)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .priority(Priority.HIGH)
            .into(binding.profileImage)

        imgKey = currentDataBuyer.imgSelfBuyer
        binding.etNameBuyer.setText(currentDataBuyer.nameBuyer)
        binding.etAddressBuyer.setText(currentDataBuyer.addressBuyer)
        binding.etBuyerTelpNumber.setText(currentDataBuyer.noTelpBuyer)
        binding.etPostalCode.setText(currentDataBuyer.postalCodeInput.toString())
        if (currentDataBuyer.rajaOngkir != null) {
            binding.originLocation.setText(currentDataBuyer.rajaOngkir?.province + ", " + currentDataBuyer.rajaOngkir?.city_name)
        }
    }

    private fun setupData() {
        if (binding.etNameBuyer.text.toString().isEmpty()) {
            binding.etNameBuyer.error = "Harap isi"
        }
        if (binding.etAddressBuyer.text.toString().isEmpty()) {
            binding.etAddressBuyer.error = "Harap Isi"
        }
        if (binding.etBuyerTelpNumber.text.toString().isEmpty()) {
            binding.etBuyerTelpNumber.error = "Harap isi"
        }
        if (binding.etPostalCode.text.toString().isEmpty()) {
            binding.etPostalCode.error = "Harap isi"
        } else {
            val tempCity = getCityFromAutocomplete(binding.originLocation.text.toString())
            Log.d("TEMPCITY", tempCity.toString())
            if (tempCity == null) {
                binding.originLocation.error = "Isi sesuai pilihan"
            } else {
                val tempAllData = BiodataRequest(
                    binding.etAddressBuyer.text.toString(),
                    binding.etNameBuyer.text.toString(),
                    binding.etBuyerTelpNumber.text.toString(),
                    binding.etPostalCode.text.toString(),
                    RajaOngkirAddress(
                        tempCity.city_id,
                        tempCity.city_name,
                        tempCity.postal_code,
                        tempCity.province,
                        tempCity.province_id,
                        tempCity.type
                    )
                )

                runBlocking {
                    editViewmodel.putBuyerBiodata(token, username, tempAllData)
                }
            }
        }
    }

    private fun getCityFromAutocomplete(city: String): City? {
        val textCity = city.split(", ")
        if (city == "") {
            return null
        }
        if (textCity.size < 2) {
            return null
        } else {
            val tempCity = listCity.filter {
                it.city_name.contains(textCity[1])
            }
            for (i in tempCity.indices) {
                val matchedCity = tempCity[i].city_name
                if (tempCity[i].city_name == matchedCity) {
                    return tempCity[i]
                }
            }
            return null
        }
    }


    fun hideProgress() {
        binding.llProgressBar.progressbar.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    fun showProgress() {
        binding.llProgressBar.progressbar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }
}