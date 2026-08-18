package com.mlopes.gestor.ui.areas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mlopes.gestor.R
import com.mlopes.gestor.ui.components.EmptyState
import com.mlopes.gestor.ui.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreasScreen(viewModel: AreasViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.refresh() }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.areas_titulo)) }) }) { padding ->
        when {
            state.carregando -> LoadingIndicator(modifier = Modifier.padding(padding))
            state.areas.isEmpty() -> EmptyState(titulo = stringResource(R.string.areas_vazia))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(state.areas, key = { it.id }) { a ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp),
                        ) {
                            Surface(
                                color = parseCor(a.cor) ?: MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(16.dp).clip(CircleShape),
                            ) {}
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(a.nome, style = MaterialTheme.typography.titleMedium)
                                if (!a.icone.isNullOrBlank()) {
                                    Text(
                                        a.icone,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseCor(hex: String?): Color? {
    if (hex.isNullOrBlank()) return null
    return runCatching {
        val limpo = hex.removePrefix("#")
        val long = limpo.toLong(16)
        if (limpo.length == 6) Color(0xFF000000 or long) else Color(long)
    }.getOrNull()
}
