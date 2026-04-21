package com.wyjqwy.app

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wyjqwy.app.data.ApiClient
import com.wyjqwy.app.data.PreferencesStore
import com.wyjqwy.app.data.SessionStore
import com.wyjqwy.app.ui.main.BookkeepingLoginScreen
import com.wyjqwy.app.ui.main.MainShell
import com.wyjqwy.app.ui.AppViewModel
import com.wyjqwy.app.ui.theme.BookkeepingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
        val store = SessionStore(applicationContext)
        val prefs = PreferencesStore(applicationContext)
        setContent {
            val vm: AppViewModel = viewModel(factory = AppViewModelFactory(store, prefs))
            val state by vm.uiState.collectAsState()
            BookkeepingTheme(themeMode = state.themeMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                    if (state.loggedIn) {
                        MainShell(state, vm)
                    } else {
                        BookkeepingLoginScreen(state, vm)
                    }
                }
            }
        }
    }
}

private class AppViewModelFactory(
    private val store: SessionStore
    , private val prefs: PreferencesStore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AppViewModel(ApiClient.service, store, prefs) as T
    }
}
