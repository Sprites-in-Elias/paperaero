package com.okane.paperaero.lobby
import android.R.id.message
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.okane.paperaero.test.User
import com.okane.paperaero.test.testApi
import com.okane.paperaero.ui.theme.PaperAeroTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class LobbyViewModel : ViewModel() {
    val dummyNumber = MutableStateFlow("0")

    init {
        fetchDummyNumber()
    }

    private fun fetchDummyNumber() {

        viewModelScope.launch {
            try {
                dummyNumber.value = lobbyApi.getDummyNumber() // 서버 응답을 저장
            } catch (e: Exception) {
                dummyNumber.value = "에러: ${e.message}"
            }
        }
    }
}

@Composable
fun LobbyScreen(
    navController: NavController = rememberNavController(),
    viewModel: LobbyViewModel = viewModel()
) {
    val dummyNumber by viewModel.dummyNumber.collectAsState()
    Scaffold(modifier = Modifier) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "현재 ${dummyNumber}명 접속 중",
                fontSize = 28.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LobbyPreview() {
    PaperAeroTheme {
        LobbyScreen()
    }
}