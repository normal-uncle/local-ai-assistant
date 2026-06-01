package com.just.feature.memo

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.feature.memo.memoDetail.MemoDetailScreen
import com.just.feature.memo.memoList.MemoListScreen

@Composable
fun MemoScene(onBackToApp: () -> Unit) {
    val backStack = rememberNavBackStack(MemoSubRoute.List)
    NavDisplay(
        backStack = backStack,
        entryProvider =
            entryProvider {
                entry<MemoSubRoute.List> {
                    MemoListScreen(
                        onOpenDetail = { id -> backStack.add(MemoSubRoute.Detail(id)) },
                        onBack = onBackToApp,
                    )
                }
                entry<MemoSubRoute.Detail> { route ->
                    MemoDetailScreen(
                        noteId = route.noteId,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
    )
}
