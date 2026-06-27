package com.okane.paperaero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.okane.paperaero.ui.theme.PaperAeroTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import com.okane.paperaero.BuildConfig

data class UserResponse(
    val status: String,
    val code: Int,
    val data: List<User>,
    val message: String
)

data class User(
    val id: Int,
    val name: String,
    val age: Int,
    val role: String
)
interface ApiService {
    @GET("test/hello")
    suspend fun getHello(): String

    @GET("mListTest") // baseURL 뒤에 붙을 경로
    suspend fun getUserList(): UserResponse
}
class MainViewModel : ViewModel() {
    // 서버에서 받은 문자열을 저장할 상태 변수
    val message = MutableStateFlow("로딩 중...")
    val userList = MutableStateFlow<List<User>>(emptyList())

    init {
        fetchHello()
        fetchUserList()
    }

    private fun fetchHello() {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.GO_SERVER_URL)
            .addConverterFactory(ScalarsConverterFactory.create()) // String 반환을 위해 필요
            .build()

        val api = retrofit.create(ApiService::class.java)

        viewModelScope.launch {
            try {
                message.value = api.getHello() // 서버 응답을 저장
            } catch (e: Exception) {
                message.value = "에러: ${e.message}"
            }
        }
    }
    private fun fetchUserList() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://okane-server.duckdns.org/")
            .addConverterFactory(GsonConverterFactory.create()) // GSON으로 JSON 파싱
            .build()

        val api = retrofit.create(ApiService::class.java)

        viewModelScope.launch {
            try {
                val response = api.getUserList()
                userList.value = response.data // 데이터 리스트만 추출
            } catch (e: Exception) {
                // 에러 처리 (로그 출력 등)
            }
        }
    }
}
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaperAeroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    GreetingScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun GreetingScreen(viewModel: MainViewModel = viewModel(), modifier: Modifier) {
    // 서버 데이터가 바뀌면 자동으로 UI가 다시 그려짐
    val msg by viewModel.message.collectAsState()

    Column(modifier = modifier) {
        Greeting(name = msg, modifier = modifier)
        UserListScreen()
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "서버응답 $name!",
        modifier = modifier
    )
}

@Composable
fun UserListScreen(viewModel: MainViewModel = viewModel()) {
    val users by viewModel.userList.collectAsState()

    LazyColumn {
        items(users) { user ->
            UserItem(user)
        }
    }
}

@Composable
fun UserItem(user: User) {
    Column {
        Text(text = "이름: ${user.name}")
        Text(text = "나이: ${user.age}")
        Text(text = "역할: ${user.role}")
        Text(text = "---")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PaperAeroTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}