package com.mlopes.gestor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mlopes.gestor.R
import com.mlopes.gestor.domain.model.Prioridade
import com.mlopes.gestor.ui.theme.PrioridadeAlta
import com.mlopes.gestor.ui.theme.PrioridadeBaixa
import com.mlopes.gestor.ui.theme.PrioridadeCritica
import com.mlopes.gestor.ui.theme.PrioridadeNormal

@Composable
fun PrioridadeChip(prioridade: Prioridade, modifier: Modifier = Modifier) {
    val (rotulo, cor) = when (prioridade) {
        Prioridade.CRITICA -> stringResource(R.string.prioridade_critica) to PrioridadeCritica
        Prioridade.ALTA -> stringResource(R.string.prioridade_alta) to PrioridadeAlta
        Prioridade.NORMAL -> stringResource(R.string.prioridade_normal) to PrioridadeNormal
        Prioridade.BAIXA -> stringResource(R.string.prioridade_baixa) to PrioridadeBaixa
    }
    Text(
        text = rotulo.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Color.White,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(cor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
