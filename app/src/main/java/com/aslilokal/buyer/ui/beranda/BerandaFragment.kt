package com.aslilokal.buyer.ui.beranda

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aslilokal.buyer.R
import com.aslilokal.buyer.databinding.FragmentBerandaBinding
import com.aslilokal.buyer.databinding.ItemMenuProductBinding
import com.aslilokal.buyer.model.data.api.ApiHelper
import com.aslilokal.buyer.model.data.api.RetrofitInstance
import com.aslilokal.buyer.model.local.ItemBerandaMenu
import com.aslilokal.buyer.ui.adapter.ProductGridAdapter
import com.aslilokal.buyer.ui.detail.lokal.LokalCategoryActivity
import com.aslilokal.buyer.utils.Resource
import com.aslilokal.buyer.viewmodel.AslilokalVMProviderFactory
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import kotlinx.coroutines.launch


class BerandaFragment : Fragment() {
    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    private val berandaMenuList = ArrayList<ItemBerandaMenu>()
    private val imageList = ArrayList<SlideModel>()

    private lateinit var viewModel: BerandaViewModel
    private lateinit var productGridAdapter: ProductGridAdapter

    var isError = false
    var isLastPage = false
    var isScrolling = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)

        showLoadingPopularList()

        setupViewModel()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getProducts()
        }


        binding.nestedBerandaFragment.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if (scrollY >= v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight &&
                    scrollY > oldScrollY
                ) {
                    val layoutManager = binding.rvPopularProduct.layoutManager as GridLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    Log.d(
                        "NESTEDSCROLL", "firstVisibleItemPosition: $firstVisibleItemPosition," +
                                "visibleItemCount: $visibleItemCount" +
                                "totalItemCount: $totalItemCount"
                    )
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount) {
                        if (!isLastPage) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                Log.d("GETPRODUCT", "true")
                                viewModel.getProducts()
                                isScrolling = false
                            }
                        }
                    }
                }
            }
        })

        setupRvPopularProduct()

        addDataRvMenuBeranda()
        showRvMenuBeranda()
        setupDataBanner()

        setupObserverPopularProduct()
        return binding.root
    }

    private fun setupObserverPopularProduct() {
        viewModel.products.observe(viewLifecycleOwner, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.result.let { productResponse ->
                        if (productResponse?.docs?.toList()?.size!! <= 0) {
                            hideLoadingPopularList()
                            showErrorMessage("Sepertinya ada kesalahan")
                            Toast.makeText(activity, "Sepertinya ada kesalahan", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            hideLoadingPopularList()
                            hideErrorMessage()
                            productGridAdapter.differ.submitList(productResponse.docs.toList())
                        }
                        //Pagination totalPages
                        val totalPages = productResponse.totalPages
                        isLastPage = (viewModel.productPage - 1) == totalPages
                        Log.d(
                            "ISLASTPAGEVALUE",
                            "isLastPage: $isLastPage, totalPages: $totalPages, currentPage: " + viewModel.productPage.toString()
                        )
                        if (isLastPage) {
                            binding.rvPopularProduct.setPadding(0, 0, 0, 0)
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    hideLoadingPopularList()
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occured: $message", Toast.LENGTH_SHORT)
                            .show()
                        showErrorMessage(message)
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                    hideErrorMessage()
                }
            }
        })

        binding.itemErrorMessage.btnRetry.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.getProducts()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupDataBanner() {
        imageList.add(SlideModel(R.drawable.slider1, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.slider2, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.slider3, ScaleTypes.CENTER_CROP))
        imageList.add(SlideModel(R.drawable.slider4, ScaleTypes.CENTER_CROP))

        binding.imageBerandaSlider.setImageList(imageList)
    }

    private fun addDataRvMenuBeranda() {
        berandaMenuList.add(ItemBerandaMenu(R.drawable.ic_aslisembako, "Sembako\n Lokal"))
        berandaMenuList.add(ItemBerandaMenu(R.drawable.ic_aslijasa, "Jasa\n Lokal"))
        berandaMenuList.add(ItemBerandaMenu(R.drawable.ic_aslikuliner, "Kuliner\n Lokal"))
        berandaMenuList.add(ItemBerandaMenu(R.drawable.ic_aslifashion, "Fashion\n Lokal"))
        berandaMenuList.add(ItemBerandaMenu(R.drawable.ic_baseline_more_horiz_24, "Coming\n soon"))
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(
            viewModelStore,
            AslilokalVMProviderFactory(ApiHelper(RetrofitInstance.api))
        ).get(BerandaViewModel::class.java)
    }

    private fun showRvMenuBeranda() {
        val menuAdapter = BerandaMenuAdapter(berandaMenuList)
        binding.rvMenuBeranda.apply {
            adapter = menuAdapter
            layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupRvPopularProduct() {
        productGridAdapter = ProductGridAdapter()
        binding.rvPopularProduct.apply {
            adapter = productGridAdapter
            layoutManager = GridLayoutManager(binding.root.context, 2)
        }
    }

    private fun showLoadingPopularList() {
        binding.rvSkeletonLayout.showShimmerAdapter()
        binding.rvPopularProduct.visibility = View.GONE
    }

    private fun hideLoadingPopularList() {
        binding.rvSkeletonLayout.hideShimmerAdapter()
        binding.rvPopularProduct.visibility = View.VISIBLE
    }

    private fun showErrorMessage(message: String) {
        binding.itemErrorMessage.itemError.visibility = View.VISIBLE
        binding.itemErrorMessage.tvErrorMessage.visibility = View.VISIBLE
        binding.itemErrorMessage.tvErrorMessage.text = message
        isError = true
    }

    private fun hideErrorMessage() {
        binding.itemErrorMessage.itemError.visibility = View.INVISIBLE
        isError = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.GONE
    }

    class BerandaMenuAdapter(private val listMenu: ArrayList<ItemBerandaMenu>) :
        RecyclerView.Adapter<BerandaMenuAdapter.BerandaViewHolder>() {
        inner class BerandaViewHolder(private val binding: ItemMenuProductBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(menu: ItemBerandaMenu) {
                Glide.with(itemView.context)
                    .load(menu.imgMenuName)
                    .into(binding.imgIconMenu)

                binding.txtNameIconMenu.text = menu.menuName

                itemView.setOnClickListener {
                    when (layoutPosition) {
                        0 -> {
                            val intent =
                                Intent(binding.root.context, LokalCategoryActivity::class.java)
                            intent.putExtra("category", "sembako")
                            binding.root.context.startActivity(intent)
                        }
                        1 -> {
                            val intent =
                                Intent(binding.root.context, LokalCategoryActivity::class.java)
                            intent.putExtra("category", "jasa")
                            binding.root.context.startActivity(intent)
                        }
                        2 -> {
                            val intent =
                                Intent(binding.root.context, LokalCategoryActivity::class.java)
                            intent.putExtra("category", "kuliner")
                            binding.root.context.startActivity(intent)
                        }
                        3 -> {
                            val intent =
                                Intent(binding.root.context, LokalCategoryActivity::class.java)
                            intent.putExtra("category", "fashion")
                            binding.root.context.startActivity(intent)
                        }
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BerandaViewHolder =
            BerandaViewHolder(
                ItemMenuProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun onBindViewHolder(holder: BerandaViewHolder, position: Int) {
            holder.bind(listMenu[position])
        }

        override fun getItemCount(): Int = listMenu.size
    }
}