package com.just.feature.capture

import androidx.compose.runtime.Composable
import com.just.feature.capture.capture.CaptureScreen

@Composable
fun CaptureScene(onOpenMemo: () -> Unit) {
    CaptureScreen(onOpenMemo = onOpenMemo)
}
