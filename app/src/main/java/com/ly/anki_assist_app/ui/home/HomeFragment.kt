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
import com.google.android.material.snackbar.Snackbar
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.databinding.FragmentHomeBinding
import com.ly.anki_assist_app.utils.Status
import com.ly.anki_assist_app.utils.Utils

const val ANKI_PERMISSIONS = "com.ichi2.anki.permission.READ_WRITE_DATABASE"
const val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
const val INTERNET_PERMISSION = Manifest.permission.INTERNET
const val PERMISSION_REQUEST_CODE = 1023

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

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.checkStatus.observe(viewLifecycleOwner, Observer {
            when(it){
                CheckStatus.ANKI_UNSTALL -> showMessage(
                    R.string.ankidroid_not_found_title,
                    R.string.ankidroid_not_found_description
                )
                CheckStatus.NOT_PERMISSION -> showMessage(
                    R.string.missingpermissions_title,
                    R.string.missingpermissions_description
                )
                CheckStatus.SUCCESS -> showContent()
            }
        })

        val overviewTextView = _binding?.studyOverview
        homeViewModel.dueOverview.observe(viewLifecycleOwner, Observer {
            if (it.status == Status.SUCCESS) {
                overviewTextView?.text = "今日需复习 ${it.data?.reviewNums} 张，需学习新卡片 ${it.data?.newNums} 张"
            }
        })

        _binding?.printReviewBtn?.setOnClickListener {
            it.findNavController().navigate(R.id.action_home_to_print)
        }

        val retryBtn = binding.retry
        retryBtn.setOnClickListener {
            checkAllPermissions()
        }

        checkAllPermissions()
        return root
    }

    /**
     * 申请权限
     */
    private fun checkAllPermissions() {
        // check whether ankidroid is installed
        if (!Utils.isAppInstalled(this.context, "com.ichi2.anki")) {
            homeViewModel.updateCheckStatus(CheckStatus.ANKI_UNSTALL)
            return;
        }

        // 判断权限有没有授权
        if(!permissionsGranted()) {
            val activity = this.activity ?: return

            Log.d("HomeFragment", "begin permissionsGranted")

            try {
                this.requestPermissions(
                    arrayOf(ANKI_PERMISSIONS, READ_STORAGE_PERMISSION, INTERNET_PERMISSION),
                    PERMISSION_REQUEST_CODE
                )
            }catch (e: Throwable) {
                homeViewModel.updateCheckStatus(CheckStatus.NOT_PERMISSION)
                Log.e("HomeFragment", "requestPermissions error", e)
            }
            return
        }

        homeViewModel.updateCheckStatus(CheckStatus.SUCCESS)
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

        Log.d("HomeFragment", "PermissionsResult ${allPermissionsGranted}")

        if(!allPermissionsGranted) {
            homeViewModel.updateCheckStatus(CheckStatus.NOT_PERMISSION)
            return
        }

        homeViewModel.updateCheckStatus(CheckStatus.SUCCESS)
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

    private fun showMessage(titleResource: Int, descriptionResource: Int) {
        _binding?.message?.visibility = View.VISIBLE
        _binding?.content?.visibility = View.GONE

        _binding?.messageTitle?.setText(titleResource)
        _binding?.messageDescription?.setText(descriptionResource)
    }
    private fun showContent(){
        _binding?.message?.visibility = View.GONE
        _binding?.content?.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}