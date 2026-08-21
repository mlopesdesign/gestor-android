package com.mlopes.gestor

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.mlopes.gestor.ui.MainViewModel
import com.mlopes.gestor.ui.nav.GestorNavHost
import com.mlopes.gestor.ui.theme.GestorTheme
import dagger.hilt.android.AndroidEntryPoint

// FIX v0.1.4: trocado ComponentActivity por FragmentActivity.
// BiometricPrompt precisa de FragmentActivity (nao funciona com ComponentActivity).
@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            GestorTheme {
                GestorNavHost(
                    authRepository = mainViewModel.authRepository,
                    activity = this,
                )
            }
        }
    }
}
