package com.mlopes.gestor.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mlopes.gestor.R

/**
 * FIX v0.1.4: tela de login com 4 melhorias de UX:
 *  - Pre-preenchimento automatico de email/senha (se "Lembrar de mim" foi marcado antes).
 *  - Icone de olho (👁) no campo Senha pra mostrar/ocultar a senha digitada.
 *  - Checkbox "Lembrar de mim" pra gravar email/senha (criptografado).
 *  - Botao "Entrar com digital" aparece se biometria disponivel E credencial salva.
 */
@Composable
fun LoginScreen(
    onLogado: () -> Unit,
    activity: FragmentActivity? = null,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.logado) {
        if (state.logado) onLogado()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.login_titulo),
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitulo),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                label = { Text(stringResource(R.string.login_email)) },
                placeholder = { Text(stringResource(R.string.login_email_hint)) },
                singleLine = true,
                enabled = !state.carregando,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = state.senha,
                onValueChange = viewModel::onSenhaChange,
                label = { Text(stringResource(R.string.login_senha)) },
                singleLine = true,
                enabled = !state.carregando,
                visualTransformation = if (state.mostrarSenha) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = viewModel::onMostrarSenhaToggle) {
                        Icon(
                            imageVector = if (state.mostrarSenha) Icons.Filled.VisibilityOff
                            else Icons.Filled.Visibility,
                            contentDescription = if (state.mostrarSenha) "Ocultar senha"
                            else "Mostrar senha",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.lembrar,
                    onCheckedChange = viewModel::onLembrarToggle,
                    enabled = !state.carregando,
                )
                Text(
                    text = stringResource(R.string.login_lembrar),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (state.erro != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.erro.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::entrar,
                enabled = !state.carregando,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (state.carregando) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(20.dp),
                    )
                } else {
                    Text(stringResource(R.string.login_botao))
                }
            }
            // Botao de biometria so aparece se (a) biometria disponivel E (b) tem
            // credencial salva. Os 2 estados estao combinados em `state.biometriaDisponivel`.
            if (state.biometriaDisponivel && activity != null) {
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { viewModel.entrarComBiometria(activity) },
                    enabled = !state.carregando,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(R.string.login_botao_biometria))
                }
            }
        }
    }
}
