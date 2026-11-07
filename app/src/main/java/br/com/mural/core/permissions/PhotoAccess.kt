package br.com.mural.core.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

sealed interface PhotoAccess {
    data object Granted : PhotoAccess
    data object Partial : PhotoAccess
    data object Denied : PhotoAccess
    data object Unknown : PhotoAccess
}

object PhotoPermissionHelper {

    fun getPhotoAccessState(context: Context): PhotoAccess {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                val fullGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

                val partialGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                ) == PackageManager.PERMISSION_GRANTED

                when {
                    fullGranted -> PhotoAccess.Granted
                    partialGranted -> PhotoAccess.Partial
                    else -> PhotoAccess.Denied
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                val fullGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED

                if (fullGranted) PhotoAccess.Granted else PhotoAccess.Denied
            }
            else -> {
                val storageGranted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                if (storageGranted) PhotoAccess.Granted else PhotoAccess.Denied
            }
        }
    }
}
