package com.dev.nihitb06.newlightningnote.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import org.jetbrains.annotations.NotNull

class PermissionManager {

    interface OnPermissionResultListener {
        fun onGranted()
        fun onDenied()
    }

    companion object {
        private val permissionResultListener = ArrayList<OnPermissionResultListener>()

        fun askForPermission(context: Context, permissions: Array<String>, listener: OnPermissionResultListener) {
            val requestCode = permissionResultListener.size

            permissionResultListener.add(requestCode, listener)

            if(permissions.isNotEmpty())
                ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)
            else
                listener.onGranted()
        }

        fun onRequestPermissionsResult(requestCode: Int, @NotNull permissions: Array<out String>, @NotNull results: IntArray) {
            try {
                val thisListener = permissionResultListener[requestCode]

                if(permissions.size == 1)
                    if (results[0] == PackageManager.PERMISSION_GRANTED)
                        thisListener.onGranted()
                    else
                        thisListener.onDenied()
                else {
                    var shouldExecute = true
                    for (index in permissions.indices) {
                        if (results[index] != PackageManager.PERMISSION_GRANTED)
                            shouldExecute = false
                    }

                    if(shouldExecute)
                        thisListener.onGranted()
                    else
                        thisListener.onDenied()
                }
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }
        }
    }
}