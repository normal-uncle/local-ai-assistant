package com.just.assistant.local.device

import android.app.ActivityManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class DeviceCapability(
    val totalRamGb: Int,
    val sdkInt: Int,
)

@Singleton
class DeviceProfiler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        /**
         * 현재 기기의 가용 가능한 RAM과 SDK를 반환.
         * `totalRamGb`는 ceil 처리 (예: 5.7GB → 6).
         */
        fun probe(): DeviceCapability {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = ActivityManager.MemoryInfo()
            am.getMemoryInfo(info)
            val totalBytes = info.totalMem
            val totalGbCeil = ((totalBytes + (1L shl 30) - 1) shr 30).toInt()
            return DeviceCapability(
                totalRamGb = totalGbCeil,
                sdkInt = android.os.Build.VERSION.SDK_INT,
            )
        }
    }
