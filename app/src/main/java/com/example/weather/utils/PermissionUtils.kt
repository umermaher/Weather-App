package com.example.weather.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun isPermissionGranted(context: Context,permission: String):Boolean{
    return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_GRANTED
}

fun requestPermissions(activity: Activity?, permission: String, requestCode:Int){
    ActivityCompat.requestPermissions(activity!!, arrayOf(permission),requestCode)
}
