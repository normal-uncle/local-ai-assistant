package com.just.feature.memo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.feature.memo.memoDetail.MemoDetailScreen
import com.just.feature.memo.memoList.MemoListScreen

@Composable
fun MemoScene(
    onBackToApp: () -> Unit,
    initialDetailNoteId: Long? = null,
    onDetailOpenChange: (Boolean) -> Unit = {},
) {
    val backStack = rememberNavBackStack(MemoSubRoute.List)
    LaunchedEffect(backStack.lastOrNull()) {
        onDetailOpenChange(backStack.lastOrNull() is MemoSubRoute.Detail)
    }
    LaunchedEffect(initialDetailNoteId) {
        if (initialDetailNoteId != null && initialDetailNoteId >= 0L) {
            val last = backStack.lastOrNull()
            val alreadyShowing =
                last is MemoSubRoute.Detail && last.noteId == initialDetailNoteId
            if (!alreadyShowing) {
                backStack.add(MemoSubRoute.Detail(initialDetailNoteId))
            }
        }
    }
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
