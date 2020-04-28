package com.nepoapp.cameranativesurfaceview

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object PermissionUtils{

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.RECORD_AUDIO)

    fun validateExistPermissions(context: Context) : Boolean{
        val isCamera = permissionGranted(context,Manifest.permission.CAMERA)
        val isWriteStorage = permissionGranted(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val isAudio =  permissionGranted(context,Manifest.permission.RECORD_AUDIO)

        return isCamera && isWriteStorage && isAudio
    }

    fun requestPermissions(activity: Activity,requestCode:Int){
        ActivityCompat.requestPermissions(activity,permissions,requestCode)
    }

    private fun permissionGranted(context: Context,permission: String) : Boolean{
        return ActivityCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED
    }

}