package com.just.feature.memo.memoDetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.just.feature.memo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MemoDetailScreen(
    noteId: Long,
    onBack: () -> Unit,
    viewModel: MemoDetailViewModel =
        hiltViewModel<MemoDetailViewModel, MemoDetailViewModel.Factory>(
            key = "memo-detail-$noteId",
        ) { factory -> factory.create(noteId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.memo_detail_title)) }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            when (val s = state) {
                MemoDetailState.Loading -> Text(stringResource(R.string.memo_detail_loading))
                MemoDetailState.NotFound -> Text(stringResource(R.string.memo_detail_not_found))
                is MemoDetailState.Loaded -> {
                    Text(s.note.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(s.note.body, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
