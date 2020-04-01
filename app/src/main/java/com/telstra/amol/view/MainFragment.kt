package com.telstra.amolassignmenttestra.view

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.telstra.amolassignmenttestra.R
import com.telstra.amolassignmenttestra.adapter.DataAdapter
import com.telstra.amolassignmenttestra.databinding.ActivityMainBinding
import com.telstra.amolassignmenttestra.room.AppDAO
import com.telstra.amolassignmenttestra.room.AppDB
import com.telstra.amolassignmenttestra.room.AppEntity
import com.telstra.amolassignmenttestra.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.view.*


class MainFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, MainViewModel.APiResponse {
    lateinit var datalayoutManager: LinearLayoutManager
    private lateinit var appStoreHomeViewModel: MainViewModel
    lateinit var binding: ActivityMainBinding
    lateinit var adapter: DataAdapter
    lateinit var appDao: AppDAO
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_main, container, false)
        appStoreHomeViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        binding.mainModel = appStoreHomeViewModel
        appStoreHomeViewModel.apiStatus(this)
        appDao = AppDB.getInstance(context!!)
        bindview()

        return binding.root
    }


    // to handle a orientation changes
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            subscribeDataCallBack()
        } else {

        }
    }
    // Create  button in action bar for refresh data
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    //    Refresh data on click refresh Button
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.refreshmenu)
            binding.swapRefreshLayout.isRefreshing = true
        subscribeDataCallBack()
        return super.onOptionsItemSelected(item)
    }


    // Call API
    private fun subscribeDataCallBack() {
        appStoreHomeViewModel.getProjectList()
    }


    // Bindviews
    private fun bindview() {
        datalayoutManager = LinearLayoutManager(context)
        datalayoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.root.Recycleview.layoutManager = datalayoutManager
        binding.swapRefreshLayout.isRefreshing = true
        binding.swapRefreshLayout.setOnRefreshListener(this)
        adapter = DataAdapter(context!!, appDao.getallData())


        // Check Network
        if (isNetwork(context!!)) {
            if (appDao.getallData().isEmpty()) {
                appStoreHomeViewModel.getProjectList()
            }
        } else {
            callToast(context!!.getString(R.string.networkMessage))
            binding.swapRefreshLayout.isRefreshing = false
        }

        binding.Recycleview.adapter = adapter

        //  Handel a Action bar Title
        binding.Recycleview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                (activity as MainActivity).supportActionBar?.title =
                    appDao.getallData()
                        .get(datalayoutManager.findFirstVisibleItemPosition()).title
            }
        })
    }

    // Show toast notification
    private fun callToast(message: String) {
        val myToast = Toast.makeText(
            context,
            message,
            Toast.LENGTH_SHORT
        )
        myToast.show()
    }


    // Checking the Network

    @Suppress("DEPRECATION")
    private fun isNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                return true
            }
        }
        return false
    }


    // pull to Refresh
    override fun onRefresh() {
        if (isNetwork(context!!)) {
            subscribeDataCallBack()
        } else {
            binding.swapRefreshLayout.isRefreshing = false
            callToast(this.getString(R.string.networkMessage))
        }
    }

    override fun apiStatus(datList: List<AppEntity>, status: Boolean) {
        adapter.updateData(datList)

        binding.swapRefreshLayout.isRefreshing = false

    }
}