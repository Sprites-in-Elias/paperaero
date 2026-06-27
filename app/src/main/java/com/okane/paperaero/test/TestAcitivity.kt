package com.okane.paperaero.test

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.Navigator
import androidx.navigation.compose.rememberNavController
import com.okane.paperaero.network.RetrofitClient
import com.okane.paperaero.ui.theme.PaperAeroTheme
import com.okane.paperaero.util.RealtimeToast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.http.GET

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

interface TestApiService {
    @GET("test/hello")
    suspend fun getHello(): String

    @GET("mListTest") // baseURL 뒤에 붙을 경로
    suspend fun getUserList(): UserResponse
}
val testApi: TestApiService by lazy {
    RetrofitClient.instance.create(TestApiService::class.java)
}
class TestViewModel : ViewModel() {
    val message = MutableStateFlow("")
    val userList = MutableStateFlow<List<User>>(emptyList())

    init {
        refreshAll()
    }

    fun refreshAll() {
        fetchHello()
        fetchUserList()
    }
    fun fetchHello() {

        message.value = "로딩 중..."
        viewModelScope.launch {
            try {
                message.value = testApi.getHello() // 서버 응답을 저장
            } catch (e: Exception) {
                message.value = "에러: ${e.message}"
            }
        }
    }
    fun fetchUserList() {

        userList.value = emptyList()
        viewModelScope.launch {
            try {
                val response = testApi.getUserList()
                userList.value = response.data // 데이터 리스트만 추출
            } catch (e: Exception) {
                // 에러 처리 (로그 출력 등)
            }
        }
    }
}

@Composable
fun TestGreetingScreen(navController: NavController = rememberNavController(), viewModel: TestViewModel = viewModel(), modifier: Modifier) {
    // 서버 데이터가 바뀌면 자동으로 UI가 다시 그려짐
    val msg by viewModel.message.collectAsState()
    val context = LocalContext.current
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Greeting(name = msg, modifier = modifier)
            UserListScreen(viewModel = viewModel)
            Button(
                onClick = {
                    viewModel.refreshAll()
                    RealtimeToast.show(context, "리프레시")
                }
            ) {
                Text (
                    text = "refresh"
                )
            }
        }
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
fun UserListScreen(viewModel: TestViewModel = viewModel()) {
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