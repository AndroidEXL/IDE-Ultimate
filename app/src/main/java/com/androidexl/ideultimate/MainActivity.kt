package com.androidexl.ideultimate

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.androidexl.ideultimate.ui.components.AppBottomBar
import com.androidexl.ideultimate.ui.components.AppBottomBarSpace
import com.androidexl.ideultimate.ui.components.AppTab
import com.androidexl.ideultimate.ui.screens.HomeScreen
import com.androidexl.ideultimate.ui.screens.SplashScreen
import com.androidexl.ideultimate.ui.screens.WorkspaceScreen
import com.androidexl.ideultimate.ui.theme.DeepNavy
import com.androidexl.ideultimate.ui.theme.IDEUltimateTheme

private enum class Screen { Splash, Home, Workspace }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )

        setContent {
            IDEUltimateTheme {
                var screen by rememberSaveable { mutableStateOf(Screen.Splash.name) }
                var splashKey by rememberSaveable { mutableStateOf(0) }

                val onMainScreen =
                    screen == Screen.Home.name || screen == Screen.Workspace.name

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(DeepNavy),
                ) {
                    when (screen) {
                        Screen.Home.name -> HomeScreen(
                            onReplaySplash = {
                                splashKey += 1
                                screen = Screen.Splash.name
                            },
                            onOpenWorkspace = { screen = Screen.Workspace.name },
                        )
                        Screen.Workspace.name -> WorkspaceScreen(
                            onBack = { screen = Screen.Home.name },
                            bottomBarPadding = AppBottomBarSpace,
                        )
                        else -> key(splashKey) {
                            SplashScreen(
                                onFinished = { screen = Screen.Home.name },
                            )
                        }
                    }

                    if (onMainScreen) {
                        AppBottomBar(
                            selected = if (screen == Screen.Home.name) AppTab.Home else AppTab.Workspace,
                            onSelect = { tab ->
                                screen = when (tab) {
                                    AppTab.Home -> Screen.Home.name
                                    AppTab.Workspace -> Screen.Workspace.name
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(bottom = 10.dp),
                        )
                    }
                }
            }
        }
    }
}
