package com.just.assistant

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.assistant.ui.component.theme.AssistantTheme
import com.just.feature.capture.CaptureScene
import com.just.feature.memo.MemoScene
import com.just.feature.onboarding.OnboardingScene

@Composable
fun AssistantApp() {
    AssistantTheme {
        Surface {
            val bootVm: AssistantBootViewModel = hiltViewModel()
            val initial by bootVm.initialRoute.collectAsState()
            val backStack = rememberNavBackStack(AssistantRoute.Onboarding)

            LaunchedEffect(initial) {
                if (initial != null && backStack.lastOrNull() != initial) {
                    backStack.clear()
                    backStack.add(initial!!)
                }
            }

            NavDisplay(
                backStack = backStack,
                entryProvider =
                    entryProvider {
                        entry<AssistantRoute.Onboarding> {
                            OnboardingScene(
                                onDone = {
                                    backStack.clear()
                                    backStack.add(AssistantRoute.Capture)
                                },
                            )
                        }
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
