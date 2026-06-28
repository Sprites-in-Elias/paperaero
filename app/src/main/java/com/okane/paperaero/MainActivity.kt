package com.okane.paperaero

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.okane.paperaero.lobby.LobbyScreen
import com.okane.paperaero.login.LoginScreen
import com.okane.paperaero.test.TestGreetingScreen
import com.okane.paperaero.test.TestRoomScreen
import com.okane.paperaero.ui.theme.PaperAeroTheme
import com.okane.paperaero.util.RealtimeToast

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaperAeroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()

    // 🌟 이 NavHost가 이제 화면들을 관리함
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    RealtimeToast.show(
                        context,
                        "대기방 리스트로 이동합니다"
                    )
                    navController.navigate("lobby"){
                        popUpTo("login") { inclusive = true }
                    }
                },
                onTestButtonClick = {
                    navController.navigate("test")
                }
            )
        }
        composable("lobby") {
            LobbyScreen(navController = navController)
        }
        composable(
            route = "test"
        ) {
            TestGreetingScreen(navController = navController, modifier = Modifier)
        }
        composable("testRoom") {
            TestRoomScreen(navController = navController)
        }
    }
}