package com.just.assistant

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.assistant.ui.component.theme.AssistantTheme
import com.just.feature.capture.CaptureScene
import com.just.feature.memo.MemoDetailScene
import com.just.feature.memo.MemoListScene

@Composable
fun AssistantApp() {
    AssistantTheme {
        Surface {
            val backStack = rememberNavBackStack(AssistantRoute.Capture)
            NavDisplay(
                backStack = backStack,
                entryProvider =
                    entryProvider {
                        entry<AssistantRoute.Capture> {
                            CaptureScene(
                                onOpenMemoList = { backStack.add(AssistantRoute.MemoList) },
                            )
                        }
                        entry<AssistantRoute.MemoList> {
                            MemoListScene(
                                onOpenDetail = { id -> backStack.add(AssistantRoute.MemoDetail(id)) },
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }
                        entry<AssistantRoute.MemoDetail> {
                            MemoDetailScene(
                                onBack = { backStack.removeLastOrNull() },
                            )
                        }
                    },
            )
        }
    }
}
