package cn.fjc920.composetest.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper {

    const val REQUEST_CODE_WRITE_PERMISSION = 0x001

    /**
     * 检查是否有写入外部存储的权限
     *
     * @param context 上下文
     * @return 是否有权限
     */
    fun checkPermission(context: Context, activity: Activity): Boolean {
        val sdkInt = Build.VERSION.SDK_INT
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        return if (sdkInt < Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(
                context,
                permission
            )!= PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionHandle(activity)
            false
        }else {
            true
        }
    }

    /**
     * 请求写入外部存储的权限
     *
     * @param activity 活动
     */
    private fun requestPermissionHandle(activity: Activity) {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // 如果用户之前拒绝过权限请求，显示解释说明
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                REQUEST_CODE_WRITE_PERMISSION
            )
        } else {
            // 不需要解释，直接请求权限
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(permission),
                REQUEST_CODE_WRITE_PERMISSION
            )
        }
    }

}