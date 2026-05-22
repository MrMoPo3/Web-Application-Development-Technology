package com.pollcraft.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PollCraftApp()
        }
    }
}

@Serializable
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val gender: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserDto,
)

@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)

@Serializable
data class RegisterRequestDto(
    val name: String,
    val email: String,
    val gender: String,
    @SerialName("birth_date") val birthDate: String,
    val password: String,
)

@Serializable
data class AboutDto(
    val name: String,
    val emblem: String,
    val description: String,
)

@Serializable
data class ChoiceDto(
    val id: Int,
    val text: String,
    @SerialName("votes_count") val votesCount: Int = 0,
)

@Serializable
data class PollDto(
    val id: Int,
    val title: String,
    val description: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("created_by") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val choices: List<ChoiceDto> = emptyList(),
    @SerialName("total_votes") val totalVotes: Int = 0,
)

@Serializable
data class ChoiceRequestDto(val text: String)

@Serializable
data class CreatePollRequestDto(
    val title: String,
    val description: String,
    @SerialName("is_active") val isActive: Boolean = true,
    val choices: List<ChoiceRequestDto>,
)

@Serializable
data class VoteRequestDto(@SerialName("choice_id") val choiceId: Int)

@Serializable
data class VoteResponseDto(
    val message: String,
    @SerialName("choice_id") val choiceId: Int,
)

interface PollCraftApi {
    @POST("api/auth/login/")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("api/auth/register/")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @GET("api/auth/profile/")
    suspend fun profile(@Header("Authorization") token: String): UserDto

    @GET("api/about/")
    suspend fun about(): AboutDto

    @GET("api/polls/")
    suspend fun polls(): List<PollDto>

    @POST("api/polls/")
    suspend fun createPoll(
        @Header("Authorization") token: String,
        @Body request: CreatePollRequestDto,
    ): PollDto

    @POST("api/polls/{id}/vote/")
    suspend fun vote(
        @Header("Authorization") token: String,
        @Path("id") pollId: Int,
        @Body request: VoteRequestDto,
    ): VoteResponseDto
}

private fun createApi(baseUrl: String): PollCraftApi {
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    return Retrofit.Builder()
        .baseUrl(baseUrl.ensureTrailingSlash())
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create(PollCraftApi::class.java)
}

private fun String.ensureTrailingSlash(): String = if (endsWith("/")) this else "$this/"

private fun authHeader(token: String): String = "Token $token"

@Composable
fun PollCraftApp() {
    val colors = lightColorScheme(
        primary = androidx.compose.ui.graphics.Color(0xFF2454A6),
        secondary = androidx.compose.ui.graphics.Color(0xFF2F7D68),
        tertiary = androidx.compose.ui.graphics.Color(0xFF7A4B00),
        surface = androidx.compose.ui.graphics.Color(0xFFF8F9FC),
    )

    MaterialTheme(colorScheme = colors) {
        Surface(modifier = Modifier.fillMaxSize()) {
            var baseUrl by remember { mutableStateOf("http://10.0.2.2:8000/") }
            var api by remember { mutableStateOf(createApi(baseUrl)) }
            var token by remember { mutableStateOf<String?>(null) }
            var user by remember { mutableStateOf<UserDto?>(null) }
            var screen by remember { mutableStateOf(Screen.Login) }
            var status by remember { mutableStateOf<String?>(null) }

            Scaffold { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Header(
                        baseUrl = baseUrl,
                        onBaseUrlChange = {
                            baseUrl = it
                            api = createApi(it)
                            status = "API address changed"
                        },
                        user = user,
                        onLogout = {
                            token = null
                            user = null
                            screen = Screen.Login
                            status = null
                        },
                    )

                    val tabs = if (token == null) listOf(Screen.Login, Screen.Register) else listOf(Screen.Workspace)
                    TabRow(selectedTabIndex = tabs.indexOf(screen).coerceAtLeast(0)) {
                        tabs.forEach { item ->
                            Tab(
                                selected = screen == item,
                                onClick = { screen = item },
                                text = { Text(item.title) },
                            )
                        }
                    }

                    status?.let { StatusCard(it) }

                    when (screen) {
                        Screen.Login -> LoginScreen(
                            api = api,
                            onStatus = { status = it },
                            onAuthenticated = { response ->
                                token = response.token
                                user = response.user
                                screen = Screen.Workspace
                                status = "Signed in as ${response.user.email}"
                            },
                        )

                        Screen.Register -> RegisterScreen(
                            api = api,
                            onStatus = { status = it },
                            onAuthenticated = { response ->
                                token = response.token
                                user = response.user
                                screen = Screen.Workspace
                                status = "Account created for ${response.user.email}"
                            },
                        )

                        Screen.Workspace -> WorkspaceScreen(
                            api = api,
                            token = token.orEmpty(),
                            user = user,
                            onUserLoaded = { user = it },
                            onStatus = { status = it },
                        )
                    }
                }
            }
        }
    }
}

private enum class Screen(val title: String) {
    Login("Login"),
    Register("Register"),
    Workspace("PollCraft"),
}

@Composable
private fun Header(
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    user: UserDto?,
    onLogout: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("PollCraft Mobile", style = MaterialTheme.typography.headlineSmall)
                    Text("REST API Android client", style = MaterialTheme.typography.bodyMedium)
                }
                if (user != null) {
                    TextButton(onClick = onLogout) {
                        Text("Logout")
                    }
                }
            }
            OutlinedTextField(
                value = baseUrl,
                onValueChange = onBaseUrlChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Server URL") },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun LoginScreen(
    api: PollCraftApi,
    onStatus: (String?) -> Unit,
    onAuthenticated: (AuthResponseDto) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Окремий стан завантаження тільки для кнопки логіну
    var loading by remember { mutableStateOf(false) }

    AuthFormCard(title = "Authentication page") {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )
        Button(
            enabled = !loading,
            onClick = {
                loading = true
                onStatus(null)
                scope.launch {
                    runCatching { api.login(LoginRequestDto(email.trim(), password)) }
                        .onSuccess(onAuthenticated)
                        .onFailure { onStatus(it.toReadableMessage()) }
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (loading) "Signing in..." else "Sign in")
        }
    }
}

@Composable
private fun RegisterScreen(
    api: PollCraftApi,
    onStatus: (String?) -> Unit,
    onAuthenticated: (AuthResponseDto) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("other") }
    var birthDate by remember { mutableStateOf("2000-01-01") }
    var password by remember { mutableStateOf("") }
    // Окремий стан завантаження тільки для кнопки реєстрації
    var loading by remember { mutableStateOf(false) }

    AuthFormCard(title = "Registration page") {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true,
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
        )
        OutlinedTextField(
            value = gender,
            onValueChange = { gender = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Gender") },
            singleLine = true,
        )
        OutlinedTextField(
            value = birthDate,
            onValueChange = { birthDate = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Birth date, YYYY-MM-DD") },
            singleLine = true,
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
        )
        Button(
            enabled = !loading,
            onClick = {
                loading = true
                onStatus(null)
                scope.launch {
                    val request = RegisterRequestDto(
                        name = name.trim(),
                        email = email.trim(),
                        gender = gender.trim(),
                        birthDate = birthDate.trim(),
                        password = password,
                    )
                    runCatching { api.register(request) }
                        .onSuccess(onAuthenticated)
                        .onFailure { onStatus(it.toReadableMessage()) }
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (loading) "Creating account..." else "Register")
        }
    }
}

@Composable
private fun AuthFormCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = {
                Text(title, style = MaterialTheme.typography.titleLarge)
                content()
            },
        )
    }
}

@Composable
private fun WorkspaceScreen(
    api: PollCraftApi,
    token: String,
    user: UserDto?,
    onUserLoaded: (UserDto) -> Unit,
    onStatus: (String?) -> Unit,
) {
    val scope = rememberCoroutineScope()
    var about by remember { mutableStateOf<AboutDto?>(null) }
    var polls by remember { mutableStateOf<List<PollDto>>(emptyList()) }

    // Розділені стани завантаження — кожна дія незалежна
    var loadingWorkspace by remember { mutableStateOf(false) }
    var loadingCreate by remember { mutableStateOf(false) }
    // Стан голосування per-poll: pollId -> true/false
    val loadingVote = remember { mutableStateMapOf<Int, Boolean>() }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var choices by remember { mutableStateOf("Yes\nNo") }

    fun loadWorkspace(silent: Boolean = false) {
        if (!silent) loadingWorkspace = true
        scope.launch {
            runCatching {
                val aboutResult = api.about()
                val pollResult = api.polls()
                val profileResult = api.profile(authHeader(token))
                Triple(aboutResult, pollResult, profileResult)
            }.onSuccess { result ->
                about = result.first
                polls = result.second
                onUserLoaded(result.third)
                if (!silent) onStatus(null)
            }.onFailure {
                onStatus(it.toReadableMessage())
            }
            if (!silent) loadingWorkspace = false
        }
    }

    LaunchedEffect(api, token) {
        loadWorkspace(silent = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Web app workspace", style = MaterialTheme.typography.titleLarge)
                Text(about?.description ?: "PollCraft polls, voting and statistics.")
                Text("User: ${user?.name ?: "loading"}")
                // Тільки кнопка Refresh залежить від loadingWorkspace
                OutlinedButton(onClick = { loadWorkspace() }, enabled = !loadingWorkspace) {
                    Text(if (loadingWorkspace) "Refreshing..." else "Refresh from REST API")
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Create poll", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Title") },
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description") },
                )
                OutlinedTextField(
                    value = choices,
                    onValueChange = { choices = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    label = { Text("Choices, one per line") },
                )
                // Тільки кнопка Create залежить від loadingCreate
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !loadingCreate,
                    onClick = {
                        loadingCreate = true
                        scope.launch {
                            val parsedChoices = choices.lines()
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .map { ChoiceRequestDto(it) }
                            val request = CreatePollRequestDto(title.trim(), description.trim(), true, parsedChoices)
                            runCatching { api.createPoll(authHeader(token), request) }
                                .onSuccess {
                                    title = ""
                                    description = ""
                                    choices = "Yes\nNo"
                                    loadWorkspace(silent = true)
                                }
                                .onFailure { onStatus(it.toReadableMessage()) }
                            loadingCreate = false
                        }
                    },
                ) {
                    Text(if (loadingCreate) "Creating..." else "Create through API")
                }
            }
        }

        Text("Polls", style = MaterialTheme.typography.titleLarge)
        if (loadingWorkspace && polls.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        polls.forEach { poll ->
            PollCard(
                poll = poll,
                isVoting = loadingVote[poll.id] == true,
                onVote = { choiceId ->
                    loadingVote[poll.id] = true
                    scope.launch {
                        runCatching { api.vote(authHeader(token), poll.id, VoteRequestDto(choiceId)) }
                            .onSuccess {
                                onStatus(it.message)
                                loadWorkspace(silent = true)
                            }
                            .onFailure { onStatus(it.toReadableMessage()) }
                        loadingVote[poll.id] = false
                    }
                },
            )
        }
    }
}

@Composable
private fun PollCard(
    poll: PollDto,
    isVoting: Boolean,
    onVote: (Int) -> Unit,
) {
    var selectedChoiceId by remember(poll.id) { mutableStateOf<Int?>(null) }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(poll.title, style = MaterialTheme.typography.titleMedium)
            if (poll.description.isNotBlank()) {
                Text(poll.description)
            }
            Text("Author: ${poll.createdBy ?: "-"} | Total votes: ${poll.totalVotes}")
            poll.choices.forEach { choice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedChoiceId == choice.id,
                        onClick = { selectedChoiceId = choice.id },
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("${choice.text} (${choice.votesCount})")
                }
            }
            // Кнопка Vote залежить тільки від свого isVoting
            Button(
                enabled = selectedChoiceId != null && poll.isActive && !isVoting,
                onClick = { selectedChoiceId?.let(onVote) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when {
                        !poll.isActive -> "Poll is closed"
                        isVoting -> "Voting..."
                        else -> "Vote"
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(12.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

private fun Throwable.toReadableMessage(): String {
    return when (this) {
        is HttpException -> "HTTP ${code()}: ${response()?.errorBody()?.string() ?: message()}"
        else -> localizedMessage ?: "Request failed"
    }
}
