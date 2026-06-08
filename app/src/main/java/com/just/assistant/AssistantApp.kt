package com.just.assistant

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.assistant.deeplink.DeepLinkRouter
import com.just.assistant.ui.component.theme.AssistantTheme
import com.just.feature.capture.CaptureScene
import com.just.feature.chat.ChatScene
import com.just.feature.memo.MemoScene
import com.just.feature.onboarding.OnboardingScene
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Composable
fun AssistantApp() {
    AssistantTheme {
        Surface {
            val context = LocalContext.current
            val deepLinkRouter =
                remember {
                    EntryPointAccessors
                        .fromApplication(context.applicationContext, DeepLinkEntryPoint::class.java)
                        .deepLinkRouter()
                }

            val bootVm: AssistantBootViewModel = hiltViewModel()
            val initial by bootVm.initialRoute.collectAsState()
            val backStack = rememberNavBackStack(AssistantRoute.Onboarding)
            var pendingDeepLinkNoteId by remember { mutableStateOf<Long?>(null) }

            LaunchedEffect(initial) {
                if (initial != null && backStack.lastOrNull() != initial) {
                    backStack.clear()
                    backStack.add(initial!!)
                }
            }

            LaunchedEffect(Unit) {
                deepLinkRouter.events.collect { noteId ->
                    pendingDeepLinkNoteId = noteId
                    if (backStack.lastOrNull() != AssistantRoute.Memo) {
                        backStack.clear()
                        backStack.add(AssistantRoute.Memo)
                    }
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
                                onOpenChat = { backStack.add(AssistantRoute.Chat) },
                            )
                        }
                        entry<AssistantRoute.Memo> {
                            val deepLinkId = pendingDeepLinkNoteId
                            MemoScene(
                                onBackToApp = { backStack.removeLastOrNull() },
                                initialDetailNoteId = deepLinkId,
                            )
                            LaunchedEffect(deepLinkId) {
                                if (deepLinkId != null) {
                                    pendingDeepLinkNoteId = null
                                }
                            }
                        }
                        entry<AssistantRoute.Chat> {
                            ChatScene(onBack = { backStack.removeLastOrNull() })
                        }
                    },
            )
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DeepLinkEntryPoint {
    fun deepLinkRouter(): DeepLinkRouter
}
