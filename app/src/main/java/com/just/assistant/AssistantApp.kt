package com.just.assistant

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.assistant.deeplink.DeepLinkRouter
import com.just.assistant.ui.component.AssistantBottomBar
import com.just.assistant.ui.component.AssistantScaffold
import com.just.assistant.ui.component.BottomNavItem
import com.just.assistant.ui.component.theme.AssistantTheme
import com.just.feature.capture.CaptureScene
import com.just.feature.chat.ChatScene
import com.just.feature.memo.MemoScene
import com.just.feature.onboarding.OnboardingScene
import com.just.feature.settings.SettingsScene
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@Composable
fun AssistantApp() {
    AssistantTheme {
        Surface {
            val context = LocalContext.current
            val deepLinkRouter = remember {
                EntryPointAccessors
                    .fromApplication(context.applicationContext, DeepLinkEntryPoint::class.java)
                    .deepLinkRouter()
            }

            val bootVm: AssistantBootViewModel = hiltViewModel()
            val initial by bootVm.initialRoute.collectAsState()
            val backStack = rememberNavBackStack(AssistantRoute.Onboarding)
            var pendingDeepLinkNoteId by remember { mutableStateOf<Long?>(null) }
            var memoDetailOpen by remember { mutableStateOf(false) }

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

            val current = backStack.lastOrNull()
            fun selectTab(route: AssistantRoute) {
                if (backStack.lastOrNull() != route) {
                    backStack.clear()
                    backStack.add(route)
                }
            }

            val topLevelTabs = setOf(
                AssistantRoute.Capture, AssistantRoute.Chat, AssistantRoute.Memo, AssistantRoute.Settings,
            )
            val showBottomBar = current in topLevelTabs && !(current == AssistantRoute.Memo && memoDetailOpen)

            BackHandler(enabled = current in topLevelTabs && current != AssistantRoute.Capture && !memoDetailOpen) {
                selectTab(AssistantRoute.Capture)
            }

            AssistantScaffold(
                bottomBar = {
                    if (showBottomBar) {
                        AssistantBottomBar(
                            items = listOf(
                                BottomNavItem(stringResource(R.string.nav_capture), Icons.Filled.Edit, current == AssistantRoute.Capture) { selectTab(AssistantRoute.Capture) },
                                BottomNavItem(stringResource(R.string.nav_chat), Icons.AutoMirrored.Filled.Chat, current == AssistantRoute.Chat) { selectTab(AssistantRoute.Chat) },
                                BottomNavItem(stringResource(R.string.nav_memo), Icons.Filled.Notes, current == AssistantRoute.Memo) { selectTab(AssistantRoute.Memo) },
                                BottomNavItem(stringResource(R.string.nav_settings), Icons.Filled.Settings, current == AssistantRoute.Settings) { selectTab(AssistantRoute.Settings) },
                            ),
                        )
                    }
                },
            ) { padding ->
                NavDisplay(
                    modifier = Modifier.padding(padding),
                    backStack = backStack,
                    entryProvider = entryProvider {
                        entry<AssistantRoute.Onboarding> {
                            OnboardingScene(
                                onDone = { selectTab(AssistantRoute.Capture) },
                            )
                        }
                        entry<AssistantRoute.Capture> { CaptureScene() }
                        entry<AssistantRoute.Memo> {
                            val deepLinkId = pendingDeepLinkNoteId
                            MemoScene(
                                onBackToApp = { selectTab(AssistantRoute.Capture) },
                                initialDetailNoteId = deepLinkId,
                                onDetailOpenChange = { memoDetailOpen = it },
                            )
                            LaunchedEffect(deepLinkId) {
                                if (deepLinkId != null) pendingDeepLinkNoteId = null
                            }
                        }
                        entry<AssistantRoute.Chat> {
                            ChatScene(onBack = { selectTab(AssistantRoute.Capture) })
                        }
                        entry<AssistantRoute.Settings> {
                            SettingsScene(
                                onBack = { selectTab(AssistantRoute.Capture) },
                                appVersion = BuildConfig.VERSION_NAME,
                            )
                        }
                    },
                )
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DeepLinkEntryPoint {
    fun deepLinkRouter(): DeepLinkRouter
}
