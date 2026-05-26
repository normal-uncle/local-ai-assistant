package com.just.assistant.local.device

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceProfilerTest {
    @Test
    fun probe_returns_reasonable_ram_and_sdk() {
        val profiler = DeviceProfiler(ApplicationProvider.getApplicationContext())
        val cap = profiler.probe()
        assertTrue("RAM should be at least 2GB on any modern device", cap.totalRamGb >= 2)
        assertTrue("RAM should be at most 64GB", cap.totalRamGb <= 64)
        assertTrue("SDK should be >= 31", cap.sdkInt >= 31)
    }
}
