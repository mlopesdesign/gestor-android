package com.mlopes.gestor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ErroLight = Color(0xFFB3261E)
private val ErroDark = Color(0xFFF2B8B5)

private val LightColors = lightColorScheme(
    primary = MlAmarelo,
    onPrimary = MlBranco,
    primaryContainer = MlAmarelo,
    onPrimaryContainer = MlPreto,
    secondary = MlPreto,
    onSecondary = MlBranco,
    background = MlCinzaClaro,
    onBackground = MlPreto,
    surface = MlBranco,
    onSurface = MlPreto,
    surfaceVariant = MlCinzaClaro,
    onSurfaceVariant = MlPreto,
    error = ErroLight,
    onError = MlBranco,
)

private val DarkColors = darkColorScheme(
    primary = MlAmarelo,
    onPrimary = MlPreto,
    primaryContainer = MlAmareloEscuro,
    onPrimaryContainer = MlBranco,
    secondary = MlCinzaClaro,
    onSecondary = MlPreto,
    background = MlCinzaSuperEscuro,
    onBackground = MlBranco,
    surface = MlCinzaEscuro,
    onSurface = MlBranco,
    surfaceVariant = MlCinzaEscuro,
    onSurfaceVariant = MlCinzaClaro,
    error = ErroDark,
    onError = MlPreto,
)

@Composable
fun GestorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GestorTypography,
        content = content,
    )
}
