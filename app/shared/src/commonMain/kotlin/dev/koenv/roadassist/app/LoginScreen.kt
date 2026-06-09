package dev.koenv.roadassist.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.koenv.roadassist.core.Role

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (Role) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val serverReachable by viewModel.serverReachable.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is LoginState.Success) {
            onLoginSuccess((state as LoginState.Success).role)
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        val isWide = maxWidth >= 700.dp
        if (isWide) {
            DesktopLoginLayout(
                state = state,
                username = username,
                password = password,
                onUsernameChange = { username = it },
                onPasswordChange = { password = it },
                onLogin = { viewModel.login(username, password) },
                serverReachable = serverReachable,
            )
        } else {
            MobileLoginLayout(
                state = state,
                username = username,
                password = password,
                onUsernameChange = { username = it },
                onPasswordChange = { password = it },
                onLogin = { viewModel.login(username, password) },
                serverReachable = serverReachable,
            )
        }
    }
}

@Composable
private fun MobileLoginLayout(
    state: LoginState,
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    serverReachable: Boolean,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ConnectivityBanner(visible = !serverReachable)
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            RoadAssistAppIcon(size = 72.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "RoadAssist",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Report a breakdown. Track help on the way.",
                style = MaterialTheme.typography.bodySmall,
                color = LocalRoadAssistColors.current.mutedForeground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            LoginForm(
                state = state,
                username = username,
                password = password,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onLogin = onLogin,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Signed in stays active on this device.",
                style = MaterialTheme.typography.labelMedium,
                color = LocalRoadAssistColors.current.mutedForeground,
            )
        }
    }
}

@Composable
private fun DesktopLoginLayout(
    state: LoginState,
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    serverReachable: Boolean,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        DesktopBrandPanel(modifier = Modifier.weight(0.42f).fillMaxHeight())
        DesktopFormPanel(
            state = state,
            username = username,
            password = password,
            onUsernameChange = onUsernameChange,
            onPasswordChange = onPasswordChange,
            onLogin = onLogin,
            serverReachable = serverReachable,
            modifier = Modifier.weight(0.58f).fillMaxHeight(),
        )
    }
}

@Composable
private fun DesktopBrandPanel(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Color(0xFF1A1D23)), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.padding(40.dp)) {
            RoadAssistAppIcon(size = 56.dp, lightScheme = false)
            Spacer(Modifier.height(40.dp))
            Box(modifier = Modifier.height(2.dp).width(40.dp).background(RoadAssistColors.Primary))
            Spacer(Modifier.height(20.dp))
            Text(
                text = "Help on the way, faster.",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                ),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Report a breakdown with your location and a photo. Dispatchers triage every report and keep you updated.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF9AA2B1), lineHeight = 20.sp),
            )
        }
    }
}

@Composable
private fun DesktopFormPanel(
    state: LoginState,
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    serverReachable: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.background(Color.White)) {
        ConnectivityBanner(visible = !serverReachable)
        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(modifier = Modifier.widthIn(max = 400.dp).padding(horizontal = 40.dp)) {
                Text("Sign in", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.height(6.dp))
                Text("Use your RoadAssist credentials.", style = MaterialTheme.typography.bodySmall, color = LocalRoadAssistColors.current.mutedForeground)
                Spacer(Modifier.height(24.dp))
                LoginForm(state = state, username = username, password = password, onUsernameChange = onUsernameChange, onPasswordChange = onPasswordChange, onLogin = onLogin, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LoginForm(
    state: LoginState,
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        CredentialFields(username = username, password = password, onUsernameChange = onUsernameChange, onPasswordChange = onPasswordChange, onDone = onLogin)
        if (state is LoginState.Error) {
            Spacer(Modifier.height(10.dp))
            Text(text = state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(18.dp))
        LoginButton(loading = state is LoginState.Loading, onLogin = onLogin)
    }
}

@Composable
private fun CredentialFields(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDone: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    FieldLabel("USERNAME")
    Spacer(Modifier.height(4.dp))
    CompactOutlinedField(
        value = username,
        onValueChange = onUsernameChange,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
    )
    Spacer(Modifier.height(14.dp))
    FieldLabel("PASSWORD")
    Spacer(Modifier.height(4.dp))
    CompactOutlinedField(
        value = password,
        onValueChange = onPasswordChange,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
    )
}

@Composable
private fun CompactOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    val extColors = LocalRoadAssistColors.current
    val textColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) primaryColor else extColors.border
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(primaryColor),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .border(1.dp, borderColor, RoundedCornerShape(9.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                innerTextField()
            }
        },
    )
}

@Composable
private fun LoginButton(loading: Boolean, onLogin: () -> Unit) {
    Button(
        onClick = onLogin,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(9.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
            Text(text = "Log in", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = LocalRoadAssistColors.current.mutedForeground,
    )
}

@Composable
fun RoadAssistAppIcon(size: Dp, lightScheme: Boolean = true) {
    val bg = if (lightScheme) Color(0xFF1A1D23) else Color(0xFF2A2E38)
    val cornerRadius = size * 0.22f
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bg)
            .border(
                width = size * 0.014f,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(cornerRadius),
            )
            .drawBehind {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val outerRadius = this.size.width * 0.28f
                val innerRadius = this.size.width * 0.1f
                val strokeW = this.size.width * 0.075f
                drawCircle(
                    color = Color(0xFFE0590B),
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )
                drawCircle(
                    color = Color(0xFFE0590B),
                    radius = innerRadius,
                    center = center,
                )
            },
    )
}
