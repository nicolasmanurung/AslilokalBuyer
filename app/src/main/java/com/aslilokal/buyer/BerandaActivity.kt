package com.aslilokal.buyer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.aslilokal.buyer.databinding.ActivityBerandaBinding
import com.aslilokal.buyer.ui.account.login.LoginActivity
import com.aslilokal.buyer.ui.search.SearchActivity
import com.aslilokal.buyer.utils.AslilokalDataStore
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.*

const val NOTIFICATION_TOPIC = "/topics/notification-"

class BerandaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBerandaBinding
    private lateinit var datastore: AslilokalDataStore
    private var isLogin: Boolean? = false
    private lateinit var username: String
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBerandaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        datastore = AslilokalDataStore(binding.root.context)
        runBlocking {
            isLogin = datastore.read("ISLOGIN").toString().toBoolean()
            username = datastore.read("USERNAME").toString()
        }

        if (isLogin == true) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener {
                CoroutineScope(Dispatchers.IO).launch {
                    datastore.save("devicetoken", it)
                }
            }
            val finalTopic = "$NOTIFICATION_TOPIC$username"
            Log.d("FINALTOPIC", finalTopic)
            FirebaseMessaging.getInstance().subscribeToTopic(finalTopic)
        }

        setSupportActionBar(binding.mainToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val navView: BottomNavigationView = findViewById(R.id.navView)
        val navController = findNavController(R.id.nav_host_fragment)

        NavigationUI.setupWithNavController(navView, navController)
        navView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_beranda -> {
                    setSupportActionBar(binding.mainToolbar)
                    navController.navigate(R.id.navigation_beranda)
                    binding.mainToolbar.title = ""
                    binding.mainToolbar.visibility = View.VISIBLE
                    true
                }

                R.id.navigation_aslilokal -> {
                    setSupportActionBar(binding.mainToolbar)
                    navController.navigate(R.id.navigation_aslilokal)
                    binding.mainToolbar.title = ""
                    binding.mainToolbar.visibility = View.VISIBLE
                    true
                }

                R.id.navigation_pesanan -> {
                    if (isLogin == true) {
                        setSupportActionBar(binding.mainToolbar)
                        navController.navigate(R.id.navigation_pesanan)
                        binding.mainToolbar.title = ""
                        binding.mainToolbar.visibility = View.GONE
                        true
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                        false
                    }
                }

                R.id.navigation_keranjang -> {
                    if (isLogin == true) {
                        setSupportActionBar(binding.mainToolbar)
                        navController.navigate(R.id.navigation_keranjang)
                        binding.mainToolbar.title = ""
                        binding.mainToolbar.visibility = View.GONE
                        true
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                        false
                    }
                }

                R.id.navigation_profil -> {
                    if (isLogin == true) {
                        setSupportActionBar(binding.mainToolbar)
                        navController.navigate(R.id.navigation_profil)
                        binding.mainToolbar.title = ""
                        binding.mainToolbar.visibility = View.GONE
                        true
                    } else {
                        startActivity(Intent(this, LoginActivity::class.java))
                        false
                    }
                }
                else -> {
                    setSupportActionBar(binding.mainToolbar)
                    navController.navigate(R.id.navigation_beranda)
                    binding.mainToolbar.title = ""
                    binding.mainToolbar.visibility = View.VISIBLE
                    true
                }
            }
        }

        binding.lnrSearchMain.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.notification -> {
//                startActivity(Intent(this, NotificationActivity::class.java))
                Toast.makeText(
                    binding.root.context,
                    "Sedang dalam tahap pengembangan",
                    Toast.LENGTH_SHORT
                ).show()
                return true
            }
//            R.id.message -> {
//                Toast.makeText(
//                    binding.root.context,
//                    "Sedang dalam tahap pengembangan",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return true
//            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
//        val navController = findNavController(R.id.nav_host_fragment)
//        when (navController.currentDestination?.id) {
//            R.id.navigation_beranda -> {
//                setSupportActionBar(binding.mainToolbar)
//                navController.navigate(R.id.navigation_beranda)
//                binding.mainToolbar.title = ""
//                binding.mainToolbar.visibility = View.VISIBLE
//            }
//
//            R.id.navigation_aslilokal -> {
//                setSupportActionBar(binding.mainToolbar)
//                navController.navigate(R.id.navigation_aslilokal)
//                binding.mainToolbar.title = ""
//                binding.mainToolbar.visibility = View.VISIBLE
//            }
//        }
        // Check if back is already pressed. If yes, then exit the app.
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            finishAffinity()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Tekan kembali sekali lagi untuk keluar", Toast.LENGTH_SHORT)
            .show()
        GlobalScope.launch(Dispatchers.Main) {
            delay(2000)
            doubleBackToExitPressedOnce = false
        }
    }

}