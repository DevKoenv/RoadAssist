package dev.koenv.roadassist.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDesktopShell
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.CategoryChip
import dev.koenv.roadassist.app.ui.components.DispatcherNoteCard
import dev.koenv.roadassist.app.ui.components.LocationRow
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.NavRailItem
import dev.koenv.roadassist.app.ui.components.PrimaryButton
import dev.koenv.roadassist.app.ui.components.StatusBadge
import dev.koenv.roadassist.app.util.timeAgo
import dev.koenv.roadassist.core.Incident
import dev.koenv.roadassist.core.IncidentStatus
import kotlinx.coroutines.launch

@Composable
fun DispatcherDetailScreen(
    viewModel: DispatcherDetailViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val incident by viewModel.incident.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
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
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val subtitle = incident?.let { "u-${it.userId} · ${timeAgo(it.createdAt, nowMillis)}" }
            if (maxWidth >= 700.dp) {
                DispatcherDetailDesktopLayout(
                    incident = incident,
                    loading = loading,
                    selectedStatus = selectedStatus,
                    subtitle = subtitle,
                    onBack = onBack,
                    onLogout = onLogout,
                    onStatusChipClick = { showStatusDialog = true },
                )
            } else {
                DispatcherDetailMobileLayout(
                    incident = incident,
                    loading = loading,
                    selectedStatus = selectedStatus,
                    onBack = onBack,
                    onStatusChipClick = { showStatusDialog = true },
                )
            }
        }
        SnackbarHost(
            snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
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
            onDismiss = { showStatusDialog = false },
        )
    }
}

@Composable
private fun DispatcherDetailMobileLayout(
    incident: Incident?,
    loading: Boolean,
    selectedStatus: IncidentStatus?,
    onBack: () -> Unit,
    onStatusChipClick: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MobileAppBar(title = "Incident", onBack = onBack)
            AppDivider()
            DispatcherDetailBody(
                incident = incident,
                loading = loading,
                selectedStatus = selectedStatus,
                onStatusChipClick = onStatusChipClick,
            )
        }
    }
}

@Composable
private fun DispatcherDetailDesktopLayout(
    incident: Incident?,
    loading: Boolean,
    selectedStatus: IncidentStatus?,
    subtitle: String?,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onStatusChipClick: () -> Unit,
) {
    AppDesktopShell(
        onLogout = onLogout,
        navContent = {
            NavRailItem(
                selected = false,
                onClick = onBack,
                icon = { Icon(Icons.Default.List, contentDescription = null, tint = LocalRoadAssistColors.current.mutedForeground) },
                label = "Queue",
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DesktopDetailHeader(title = "Incident", subtitle = subtitle, onBack = onBack)
            AppDivider()
            DispatcherDetailBody(
                incident = incident,
                loading = loading,
                selectedStatus = selectedStatus,
                onStatusChipClick = onStatusChipClick,
            )
        }
    }
}

@Composable
private fun DispatcherDetailBody(
    incident: Incident?,
    loading: Boolean,
    selectedStatus: IncidentStatus?,
    onStatusChipClick: () -> Unit,
) {
    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        incident != null -> DispatcherDetailContent(
            incident = incident,
            selectedStatus = selectedStatus ?: incident.status,
            onStatusChipClick = onStatusChipClick,
        )
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Incident not found.", style = MaterialTheme.typography.bodyMedium, color = LocalRoadAssistColors.current.mutedForeground)
        }
    }
}

@Composable
private fun DispatcherDetailContent(
    incident: Incident,
    selectedStatus: IncidentStatus,
    onStatusChipClick: () -> Unit,
) {
    val context = LocalPlatformContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CategoryChip(incident = incident)
            Row(
                modifier = Modifier
                    .clickable(onClick = onStatusChipClick)
                    .border(1.dp, LocalRoadAssistColors.current.border, RoundedCornerShape(20.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusBadge(selectedStatus)
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Change status",
                    tint = LocalRoadAssistColors.current.mutedForeground,
                    modifier = Modifier.size(16.dp),
                )
            }
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
                    .clip(RoundedCornerShape(8.dp)),
            )
        }

        LocationRow(latitude = incident.latitude, longitude = incident.longitude)

        if (!incident.notes.isNullOrBlank()) {
            DispatcherNoteCard(notes = incident.notes!!)
        }
    }
}

@Composable
private fun DesktopDetailHeader(title: String, onBack: () -> Unit, subtitle: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.AutoMirrored.Default.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(20.dp).clickable(onClick = onBack),
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LocalRoadAssistColors.current.mutedForeground)
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
                    "NOTE TO ROAD USER · OPTIONAL",
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
                                Text("Add a note for the road user...", style = MaterialTheme.typography.bodySmall, color = muted)
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
