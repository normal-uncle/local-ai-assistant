package com.just.feature.memo.memoList

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.just.assistant.ui.component.AssistantScaffold
import com.just.assistant.ui.component.AssistantTopBar
import com.just.assistant.ui.component.EmptyState
import com.just.assistant.ui.component.MemoCard
import com.just.assistant.ui.component.theme.Spacing
import com.just.feature.memo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MemoListScreen(
    onOpenDetail: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: MemoListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AssistantScaffold(topBar = { AssistantTopBar(stringResource(R.string.memo_list_title)) }) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .testTag("memo_list"),
        ) {
            when (val s = state) {
                MemoListState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is MemoListState.Loaded -> {
                    if (s.notes.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.Notes,
                            title = stringResource(R.string.memo_list_empty),
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(Spacing.lg),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md),
                        ) {
                            items(s.notes, key = { it.id }) { note ->
                                MemoCard(
                                    title = note.title,
                                    body = note.body,
                                    isCompleted = note.isCompleted,
                                    thumbnail = note.imageUri?.let { uri ->
                                        { m -> AsyncImage(model = uri, contentDescription = null, modifier = m, contentScale = ContentScale.Crop) }
                                    },
                                    onClick = { onOpenDetail(note.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
