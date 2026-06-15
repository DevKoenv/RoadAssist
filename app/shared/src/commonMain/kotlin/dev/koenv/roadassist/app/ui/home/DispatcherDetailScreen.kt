package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.CategoryChip
import dev.koenv.roadassist.app.ui.components.IncidentActivitySection
import dev.koenv.roadassist.app.ui.components.LocationRow
import dev.koenv.roadassist.app.ui.components.PrimaryButton
import dev.koenv.roadassist.app.ui.components.StatusEditChip
import dev.koenv.roadassist.app.ui.foundation.LocalWindowSizeClass
import dev.koenv.roadassist.app.ui.foundation.WindowSizeClass
import dev.koenv.roadassist.app.ui.layouts.DetailLayout
import dev.koenv.roadassist.app.util.timeAgo
import dev.koenv.roadassist.core.Comment
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DispatcherDetailScreen(
    viewModel: DispatcherDetailViewModel,
    onBack: () -> Unit,
) {
    val incident by viewModel.incident.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val address by viewModel.address.collectAsState()
    val commentInput by viewModel.commentInput.collectAsState()
    val commentPosting by viewModel.commentPosting.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    val serverReachable by viewModel.serverReachable.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000L)
            nowMillis = System.currentTimeMillis()
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((updateState as UpdateState.Error).message)
                viewModel.clearError()
            }
        }
    }

    val subtitle = incident?.let { "u-${it.userId} · ${timeAgo(it.createdAt, nowMillis)}" }

    DetailLayout(
        title = "Incident",
        subtitle = subtitle,
        onBack = onBack,
        serverReachable = serverReachable,
        snackbarHostState = snackbarHostState,
    ) { padding ->
        val windowSizeClass = LocalWindowSizeClass.current
        if (windowSizeClass == WindowSizeClass.Compact) {
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                DispatcherDetailBody(
                    incident = incident,
                    loading = loading,
                    comments = comments,
                    address = address,
                    commentInput = commentInput,
                    commentPosting = commentPosting,
                    serverReachable = serverReachable,
                    onStatusClick = if (serverReachable) { { showStatusDialog = true } } else { {} },
                    onCommentChange = viewModel::updateCommentInput,
                    onCommentSend = viewModel::postComment,
                )
            }
        } else {
            DispatcherDetailBody(
                incident = incident,
                loading = loading,
                comments = comments,
                address = address,
                commentInput = commentInput,
                commentPosting = commentPosting,
                serverReachable = serverReachable,
                onStatusClick = if (serverReachable) { { showStatusDialog = true } } else { {} },
                onCommentChange = viewModel::updateCommentInput,
                onCommentSend = viewModel::postComment,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }

    if (showStatusDialog && incident != null) {
        UpdateStatusDialog(
            currentStatus = selectedStatus ?: incident!!.status,
            notes = notes,
            isLoading = updateState is UpdateState.Loading,
            onStatusSelect = { viewModel.selectStatus(it) },
            onNotesChange = { viewModel.updateNotes(it) },
            onSave = { viewModel.saveUpdate { showStatusDialog = false } },
            onDismiss = {
                viewModel.cancelEdit()
                showStatusDialog = false
            },
        )
    }
}

@Composable
internal fun DispatcherDetailBody(
    incident: Incident?,
    loading: Boolean,
    comments: List<Comment>,
    address: String?,
    commentInput: String,
    commentPosting: Boolean,
    serverReachable: Boolean,
    onStatusClick: () -> Unit,
    onCommentChange: (String) -> Unit,
    onCommentSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        incident != null -> DispatcherDetailContent(
            incident = incident,
            comments = comments,
            address = address,
            commentInput = commentInput,
            commentPosting = commentPosting,
            serverReachable = serverReachable,
            onStatusClick = onStatusClick,
            onCommentChange = onCommentChange,
            onCommentSend = onCommentSend,
            modifier = modifier,
        )
        else -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Incident not found.", style = MaterialTheme.typography.bodyMedium, color = LocalRoadAssistColors.current.mutedForeground)
        }
    }
}

@Composable
internal fun DispatcherDetailContent(
    incident: Incident,
    comments: List<Comment>,
    address: String?,
    commentInput: String,
    commentPosting: Boolean,
    serverReachable: Boolean,
    onStatusClick: () -> Unit,
    onCommentChange: (String) -> Unit,
    onCommentSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalRoadAssistColors.current
    val context = LocalPlatformContext.current
    var showLightbox by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryChip(incident = incident)
            StatusEditChip(status = incident.status, onClick = onStatusClick)
        }

        Text(
            text = incident.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (!incident.photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(context).data(incident.photoUrl).build(),
                contentDescription = "Incident photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showLightbox = true },
            )
        }

        LocationRow(latitude = incident.latitude, longitude = incident.longitude, address = address)

        DispatcherCommentInputRow(
            input = commentInput,
            posting = commentPosting,
            enabled = serverReachable,
            onInputChange = onCommentChange,
            onSend = onCommentSend,
        )

        IncidentActivitySection(incident = incident, comments = comments)
    }
    if (showLightbox && !incident.photoUrl.isNullOrBlank()) {
        Dialog(onDismissRequest = { showLightbox = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF000000), RoundedCornerShape(8.dp))
                    .clickable { showLightbox = false }
                    .padding(4.dp),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(incident.photoUrl).build(),
                    contentDescription = "Incident photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
internal fun DispatcherDetailPanel(viewModel: DispatcherDetailViewModel) {
    val incident by viewModel.incident.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val address by viewModel.address.collectAsState()
    val commentInput by viewModel.commentInput.collectAsState()
    val commentPosting by viewModel.commentPosting.collectAsState()
    val serverReachable by viewModel.serverReachable.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateState) {
        if (updateState is UpdateState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((updateState as UpdateState.Error).message)
                viewModel.clearError()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        DispatcherDetailBody(
            incident = incident,
            loading = loading,
            comments = comments,
            address = address,
            commentInput = commentInput,
            commentPosting = commentPosting,
            serverReachable = serverReachable,
            onStatusClick = if (serverReachable) { { showStatusDialog = true } } else { {} },
            onCommentChange = viewModel::updateCommentInput,
            onCommentSend = viewModel::postComment,
            modifier = Modifier.fillMaxSize(),
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        ) { data -> Snackbar(data) }
    }

    if (showStatusDialog && incident != null) {
        UpdateStatusDialog(
            currentStatus = selectedStatus ?: incident!!.status,
            notes = notes,
            isLoading = updateState is UpdateState.Loading,
            onStatusSelect = { viewModel.selectStatus(it) },
            onNotesChange = { viewModel.updateNotes(it) },
            onSave = { viewModel.saveUpdate { showStatusDialog = false } },
            onDismiss = {
                viewModel.cancelEdit()
                showStatusDialog = false
            },
        )
    }
}

@Composable
private fun DispatcherCommentInputRow(
    input: String,
    posting: Boolean,
    enabled: Boolean = true,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val colors = LocalRoadAssistColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.border, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = input,
            onValueChange = if (enabled) onInputChange else { _ -> },
            readOnly = !enabled,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .onPreviewKeyEvent { event ->
                    if (!enabled) return@onPreviewKeyEvent false
                    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter && event.isCtrlPressed) {
                        onSend()
                        true
                    } else {
                        false
                    }
                },
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (input.isEmpty()) {
                        Text(
                            if (enabled) "Send a message to road user..." else "Offline — messages unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.mutedForeground,
                        )
                    }
                    innerTextField()
                }
            },
        )
        val canSend = enabled && input.isNotBlank() && !posting
        IconButton(onClick = onSend, enabled = canSend) {
            if (posting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (enabled && input.isNotBlank()) MaterialTheme.colorScheme.primary else colors.mutedForeground,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun UpdateStatusDialog(
    currentStatus: IncidentStatus,
    notes: String,
    isLoading: Boolean,
    onStatusSelect: (IncidentStatus) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val borderColor = LocalRoadAssistColors.current.border
    val muted = LocalRoadAssistColors.current.mutedForeground

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Update status", style = MaterialTheme.typography.titleMedium)
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close",
                    tint = muted,
                    modifier = Modifier.size(20.dp).clickable(onClick = onDismiss),
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                IncidentStatus.entries.forEach { status ->
                    val selected = status == currentStatus
                    val colors = LocalRoadAssistColors.current
                    val dotColor = when (status) {
                        IncidentStatus.NEW -> colors.statusNew
                        IncidentStatus.IN_PROGRESS -> colors.statusInProgress
                        IncidentStatus.EN_ROUTE -> colors.statusEnRoute
                        IncidentStatus.RESOLVED -> colors.statusResolved
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (selected) colors.statusInProgressBg.copy(alpha = 0.3f) else Color.Transparent,
                                RoundedCornerShape(8.dp),
                            )
                            .clickable { onStatusSelect(status) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(Modifier.size(8.dp).background(dotColor, CircleShape))
                        Text(
                            status.dialogDisplayName(),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                        if (selected) {
                            Text("✓", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "MESSAGE · OPTIONAL",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
                    color = muted,
                )
                BasicTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                        .height(72.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box {
                            if (notes.isEmpty()) {
                                Text("Add a message visible to the road user...", style = MaterialTheme.typography.bodySmall, color = muted)
                            }
                            innerTextField()
                        }
                    },
                )
            }

            PrimaryButton(
                onClick = onSave,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Save update", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun IncidentStatus.dialogDisplayName(): String = when (this) {
    IncidentStatus.NEW -> "New"
    IncidentStatus.IN_PROGRESS -> "In progress"
    IncidentStatus.EN_ROUTE -> "En route"
    IncidentStatus.RESOLVED -> "Resolved"
}
