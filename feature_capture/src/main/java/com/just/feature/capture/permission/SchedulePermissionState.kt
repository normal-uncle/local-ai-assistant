package com.just.feature.capture.permission

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

@Stable
class SchedulePermissionState internal constructor(
    private val context: Context,
    private val calendarLauncher: (Array<String>) -> Unit,
    private val notificationLauncher: (String) -> Unit,
) {
    var lastResult: Result by mutableStateOf(Result.Idle)
        internal set

    fun hasCalendarPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) ==
            PackageManager.PERMISSION_GRANTED

    fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun canScheduleExactAlarms(): Boolean {
        val am = context.getSystemService<AlarmManager>() ?: return false
        return am.canScheduleExactAlarms()
    }

    fun requestCalendar() {
        calendarLauncher(
            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR),
        )
    }

    fun requestNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            lastResult = Result.Granted
            return
        }
        notificationLauncher(Manifest.permission.POST_NOTIFICATIONS)
    }

    fun openExactAlarmSettings() {
        val intent =
            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        context.startActivity(intent)
    }

    fun openAppSettings() {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        context.startActivity(intent)
    }

    fun shouldShowRationaleForCalendar(activity: Activity): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.READ_CALENDAR,
        ) ||
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.WRITE_CALENDAR,
            )

    fun shouldShowRationaleForNotification(activity: Activity): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    }

    sealed interface Result {
        data object Idle : Result

        data object Granted : Result

        data object DeniedTransient : Result

        data object DeniedPermanent : Result
    }
}

@Composable
fun rememberSchedulePermissionState(): SchedulePermissionState {
    val context = LocalContext.current
    val activity = context as? Activity

    val holder =
        remember {
            object {
                var instance: SchedulePermissionState? = null
            }
        }

    val calendarLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { granted ->
            val s = holder.instance ?: return@rememberLauncherForActivityResult
            val allGranted = granted.values.all { it }
            s.lastResult =
                when {
                    allGranted -> SchedulePermissionState.Result.Granted
                    activity != null && s.shouldShowRationaleForCalendar(activity) ->
                        SchedulePermissionState.Result.DeniedTransient
                    else -> SchedulePermissionState.Result.DeniedPermanent
                }
        }

    val notificationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted ->
            val s = holder.instance ?: return@rememberLauncherForActivityResult
            s.lastResult =
                when {
                    granted -> SchedulePermissionState.Result.Granted
                    activity != null && s.shouldShowRationaleForNotification(activity) ->
                        SchedulePermissionState.Result.DeniedTransient
                    else -> SchedulePermissionState.Result.DeniedPermanent
                }
        }

    return remember {
        SchedulePermissionState(
            context = context,
            calendarLauncher = { perms -> calendarLauncher.launch(perms) },
            notificationLauncher = { perm -> notificationLauncher.launch(perm) },
        ).also { holder.instance = it }
    }
}
