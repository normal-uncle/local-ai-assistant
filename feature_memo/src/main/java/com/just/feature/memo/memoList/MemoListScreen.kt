package com.just.feature.memo.memoList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.just.feature.memo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MemoListScreen(
    onOpenDetail: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: MemoListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.memo_list_title)) }) }) { padding ->
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
                        Text(
                            stringResource(R.string.memo_list_empty),
                            Modifier.align(Alignment.Center),
                        )
                    } else {
                        LazyColumn(Modifier.fillMaxSize()) {
                            items(s.notes, key = { it.id }) { note ->
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenDetail(note.id) }
                                        .padding(16.dp),
                                ) {
                                    Text(
                                        note.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null,
                                    )
                                    if (note.body.isNotBlank()) {
                                        Text(
                                            note.body.take(80),
                                            style = MaterialTheme.typography.bodyMedium,
                                            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null,
                                        )
                                    }
                                    note.imageUri?.let { uri ->
                                        AsyncImage(
                                            model = uri,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}
