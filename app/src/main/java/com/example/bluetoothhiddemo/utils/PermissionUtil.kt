package com.example.bluetoothhiddemo.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionUtil {
    companion object {
        const val REQUEST_PERMISSION = 101
        /**
         * 检查权限，如果当前没有所需权限，则去申请
         * @param activity 传入申请主体
         * @param permissions 权限清单
         * @return 是否需要申请权限
         * */
        @JvmStatic
        fun checkPermissions(activity: Activity, permissions: Array<String>): Boolean {
            val checkList: MutableList<String> = mutableListOf()
            for (per: String in permissions) {
                if (PackageManager.PERMISSION_GRANTED !=
                    ActivityCompat.checkSelfPermission(activity, per)
                ) {
                    checkList.add(per)
                }
            }
            if (checkList.size > 0) {
                activity.requestPermissions(checkList.toTypedArray(), REQUEST_PERMISSION)
                return true
            }
            return false
        }

        @JvmStatic
        fun checkPermission(activity: Activity, permission: String) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val perArray = arrayOf(permission)
                activity.requestPermissions(perArray, REQUEST_PERMISSION)
            }
        }
    }
}