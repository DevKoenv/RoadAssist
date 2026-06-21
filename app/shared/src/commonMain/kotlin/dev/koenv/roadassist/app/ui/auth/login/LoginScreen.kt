package dev.koenv.roadassist.app.ui.auth.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.theme.RoadAssistColors
import dev.koenv.roadassist.app.ui.components.ConnectivityBanner
import dev.koenv.roadassist.app.ui.components.PrimaryButton
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass
import dev.koenv.roadassist.app.ui.layouts.AuthBrandingPanel
import dev.koenv.roadassist.core.user.Role

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

    val windowSizeClass = LocalWindowSizeClass.current
    if (windowSizeClass == WindowSizeClass.Compact) {
        MobileLoginLayout(state, username, password, { username = it }, { password = it }, { viewModel.login(username, password) }, serverReachable)
    } else {
        DesktopFormPanel(state, username, password, { username = it }, { password = it }, { viewModel.login(username, password) }, serverReachable, modifier = Modifier.fillMaxSize())
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
        ConnectivityBanner(visible = !serverReachable) // offline banner
        // centered login form area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            RoadAssistAppIcon(size = 72.dp) // app icon
            Spacer(Modifier.height(16.dp))
            Text( // app name
                text = "RoadAssist",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(6.dp))
            Text( // tagline
                text = "Report a breakdown. Track help on the way.",
                style = MaterialTheme.typography.bodySmall,
                color = LocalRoadAssistColors.current.mutedForeground,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            LoginForm( // username/password fields + submit button
                state = state,
                username = username,
                password = password,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onLogin = onLogin,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            Text( // session persistence note
                text = "Signed in stays active on this device.",
                style = MaterialTheme.typography.labelMedium,
                color = LocalRoadAssistColors.current.mutedForeground,
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
    Column(modifier = modifier) {
        ConnectivityBanner(visible = !serverReachable)
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            // Left: brand panel
            AuthBrandingPanel(modifier = Modifier.weight(0.45f).fillMaxHeight())
            // Right: form panel
            Box(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
                    .background(Color.White),
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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
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
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
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
    PrimaryButton(
        onClick = onLogin,
        enabled = !loading,
        modifier = Modifier.fillMaxWidth().height(48.dp),
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
    val bg = if (lightScheme) RoadAssistColors.BrandDark else RoadAssistColors.BrandDarkSurface
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
                    color = RoadAssistColors.Primary,
                    radius = outerRadius,
                    center = center,
                    style = Stroke(width = strokeW, cap = StrokeCap.Round),
                )
                drawCircle(
                    color = RoadAssistColors.Primary,
                    radius = innerRadius,
                    center = center,
                )
            },
    )
}
