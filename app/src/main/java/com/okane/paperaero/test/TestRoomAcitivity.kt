package com.okane.paperaero.test

import android.Manifest
import android.R.id.message
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.okane.paperaero.BuildConfig
import com.okane.paperaero.login.LoginScreen
import com.okane.paperaero.network.ApiResponse
import com.okane.paperaero.network.RetrofitClient
import com.okane.paperaero.ui.theme.PaperAeroTheme
import com.okane.paperaero.util.RealtimeToast
import io.livekit.android.compose.local.RoomScope
import io.livekit.android.compose.state.rememberConnectionState
import io.livekit.android.compose.state.rememberParticipants
import io.livekit.android.room.ConnectionState
import io.livekit.android.room.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.http.GET
import androidx.core.net.toUri

sealed class UiState {
    object Loading : UiState()
    data class Success(val token: String) : UiState()
    data class Error(val message: String) : UiState()
}
data class TokenData(
    val token: String
)
interface TestRoomApiService {
    @GET("test/livekit/token")
    suspend fun getTestToken(): ApiResponse<TokenData>
}
val testRoomApi: TestRoomApiService by lazy {
    RetrofitClient.instance.create(TestRoomApiService::class.java)
}
class TestRoomViewModel() : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()
    val testToken = MutableStateFlow("...")

    init {
        testGet()
    }

    fun testGet() {
        viewModelScope.launch {
            try {
                testToken.value = testRoomApi.getTestToken().data.token // 서버 응답을 저장
                _uiState.value = UiState.Success(testToken.value)
            } catch (e: Exception) {
                testToken.value = "에러: ${e.message}"
            }
        }
    }
}

@Composable
fun RequestMicrophonePermission(
    onPermissionResult: (Boolean) -> Unit
) {
    val context = LocalContext.current

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { granted ->
            onPermissionResult(granted)
        }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (granted) {
            onPermissionResult(true)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
fun TestRoomScreen(
    navController: NavController = rememberNavController(),
    viewModel: TestRoomViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var hasMicPermission by remember {
        mutableStateOf(false)
    }

    RequestMicrophonePermission { granted ->
        hasMicPermission = granted
    }
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (hasMicPermission) {
                when (uiState) {
                    is UiState.Loading -> {
                        // 로딩 중: 프로그레스 바를 보여줌
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is UiState.Success -> {
                        // 토큰이 준비됨: RoomScope 시작
                        val token = (uiState as UiState.Success).token
                        RoomScope(
                            url = BuildConfig.LIVE_KIT_SERVER_URL,
                            token = token,
                            audio = true,
                            video = false,
                            connect = true
                        ) {
                            val connectionState by rememberConnectionState()
                            val participants by rememberParticipants()

                            Column {
                                Text("상태 : $connectionState")
                                Text("참가자 수 : ${participants.size}")
                            }
                        }
                    }

                    is UiState.Error -> {
                        // 에러 시 처리
                        Text("연결 실패: ${(uiState as UiState.Error).message}")
                    }
                }
            } else {
                Text("마이크 권한이 필요합니다.")
                Button(
                    onClick = {
                        openAppSettings(context)
                    }
                ) {
                    Text("설정에서 마이크 권한 켜기")
                }
            }
        }
    }
}
fun openAppSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        "package:${context.packageName}".toUri()
    )
    context.startActivity(intent)
}
@Preview(showBackground = true)
@Composable
fun TestRoomPreview() {
    PaperAeroTheme {
        TestRoomScreen(

        )
    }
}