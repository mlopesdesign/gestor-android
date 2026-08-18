package com.mlopes.gestor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.mlopes.gestor.ui.MainViewModel
import com.mlopes.gestor.ui.nav.GestorNavHost
import com.mlopes.gestor.ui.theme.GestorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = hiltViewModel()
            GestorTheme {
                GestorNavHost(authRepository = mainViewModel.authRepository)
            }
        }
    }
}
