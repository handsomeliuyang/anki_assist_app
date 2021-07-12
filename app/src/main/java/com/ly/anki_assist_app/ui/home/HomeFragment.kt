package com.ly.anki_assist_app.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.snackbar.Snackbar
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.databinding.FragmentHomeBinding
import com.ly.anki_assist_app.databinding.FragmentPrintOptionsBinding
import com.ly.anki_assist_app.utils.Status
import com.ly.anki_assist_app.utils.Utils

const val ANKI_PERMISSIONS = "com.ichi2.anki.permission.READ_WRITE_DATABASE"
const val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
const val INTERNET_PERMISSION = Manifest.permission.INTERNET
const val PERMISSION_REQUEST_CODE = 1023
const val ARGUMENT_PRINT_ID = "print_id"

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false).apply {
            viewmodel = homeViewModel
            lifecycleOwner = this@HomeFragment.viewLifecycleOwner
            recyclerView.adapter = HomeAadpter()
            // 设置分隔线
            recyclerView.addItemDecoration(DividerItemDecoration(this@HomeFragment.context, DividerItemDecoration.VERTICAL))
        }

        homeViewModel.dueOverview.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                it.data?.let { overView ->
                    (_binding?.recyclerView?.adapter as HomeAadpter?)?.updateOverview(overView)
                }
            }
        })

        homeViewModel.printList.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                it.data?.let { list ->
                    (_binding?.recyclerView?.adapter as HomeAadpter?)?.updatePrint(list)
                }
            }
        })

        val retryBtn = binding.retry
        retryBtn.setOnClickListener {
            checkAllPermissions()
        }

        checkAllPermissions()
        return binding.root
    }

    /**
     * 申请权限
     */
    private fun checkAllPermissions() {
        // check whether ankidroid is installed
        if (!Utils.isAppInstalled(this.context, "com.ichi2.anki")) {
            homeViewModel.updateCheckResult(false, getString(R.string.ankidroid_not_found_title))
            return;
        }

        // 判断权限有没有授权
        if(!permissionsGranted()) {
            val activity = this.activity ?: return
            try {
                this.requestPermissions(
                    arrayOf(ANKI_PERMISSIONS, READ_STORAGE_PERMISSION, INTERNET_PERMISSION),
                    PERMISSION_REQUEST_CODE
                )
            }catch (e: Throwable) {
                homeViewModel.updateCheckResult(false, getString(R.string.missingpermissions_title))
            }
            return
        }

        homeViewModel.updateCheckResult(true, "完成")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode != PERMISSION_REQUEST_CODE) {
            return
        }
        var allPermissionsGranted = true
        for(grantResult in grantResults) {
            allPermissionsGranted = allPermissionsGranted && (grantResult == PackageManager.PERMISSION_GRANTED)
        }

        if(!allPermissionsGranted) {
            homeViewModel.updateCheckResult(false, getString(R.string.missingpermissions_title))
            return
        }

        homeViewModel.updateCheckResult(true, "完成")
    }

    private fun permissionsGranted(): Boolean {
        val context = this.context ?: return false

        if(ContextCompat.checkSelfPermission(context, ANKI_PERMISSIONS) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(context, READ_STORAGE_PERMISSION) != PackageManager.PERMISSION_GRANTED
            || ContextCompat.checkSelfPermission(context, INTERNET_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}