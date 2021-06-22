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
import com.google.android.material.snackbar.Snackbar
import com.ly.anki_assist_app.R
import com.ly.anki_assist_app.databinding.FragmentHomeBinding
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

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        checkAllPermissions();
        return root
    }

    /**
     * 申请权限
     */
    private fun checkAllPermissions() {
        // check whether ankidroid is installed
        if (!Utils.isAppInstalled(this.context, "com.ichi2.anki")) {
            showMessage(
                R.string.ankidroid_not_found_title,
                R.string.ankidroid_not_found_description
            )
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
                showMessage(
                    R.string.missingpermissions_title,
                    R.string.missingpermissions_description
                )
                Log.e("HomeFragment", "requestPermissions error", e)
            }
            return
        }

        listDecks()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        Log.d("HomeFragment", "PermissionsResult")

        if(requestCode != PERMISSION_REQUEST_CODE) {
            return
        }
        var allPermissionsGranted = true
        for(grantResult in grantResults) {
            allPermissionsGranted = allPermissionsGranted && (grantResult == PackageManager.PERMISSION_GRANTED)
        }

        Log.d("HomeFragment", "PermissionsResult ${allPermissionsGranted}")

        if(!allPermissionsGranted) {
            showMessage(
                R.string.missingpermissions_title,
                R.string.missingpermissions_description
            )
            return
        }

        listDecks()
    }

    /**
     * 获取卡片数据
     */
    private fun listDecks() {

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

        _binding?.messageTitle?.setText(titleResource)
        _binding?.messageDescription?.setText(descriptionResource)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}