package com.just.assistant

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.assistant.ui.component.theme.AssistantTheme
import com.just.feature.capture.CaptureScene
import com.just.feature.memo.MemoScene

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
                                onOpenMemo = { backStack.add(AssistantRoute.Memo) },
                            )
                        }
                        entry<AssistantRoute.Memo> {
                            MemoScene(
                                onBackToApp = { backStack.removeLastOrNull() },
                            )
                        }
                    },
            )
        }
    }
}
