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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.CategoryChip
import dev.koenv.roadassist.app.ui.components.IncidentActivitySection
import dev.koenv.roadassist.app.ui.components.LocationRow
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.StatusBadge
import dev.koenv.roadassist.core.Comment
import dev.koenv.roadassist.core.Incident

@Composable
fun RoadUserDetailScreen(
    viewModel: RoadUserDetailViewModel,
    isDesktop: Boolean,
    onBack: () -> Unit,
) {
    val incident by viewModel.incident.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val address by viewModel.address.collectAsState()
    val commentInput by viewModel.commentInput.collectAsState()
    val commentPosting by viewModel.commentPosting.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()

    if (isDesktop) {
        RoadUserDetailDesktopLayout(
            incident = incident,
            loading = loading,
            comments = comments,
            address = address,
            commentInput = commentInput,
            commentPosting = commentPosting,
            onBack = onBack,
            onCommentChange = viewModel::updateCommentInput,
            onCommentSend = viewModel::postComment,
        )
    } else {
        RoadUserDetailMobileLayout(
            incident = incident,
            loading = loading,
            refreshing = refreshing,
            comments = comments,
            address = address,
            commentInput = commentInput,
            commentPosting = commentPosting,
            onBack = onBack,
            onRefresh = viewModel::refresh,
            onCommentChange = viewModel::updateCommentInput,
            onCommentSend = viewModel::postComment,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoadUserDetailMobileLayout(
    incident: Incident?,
    loading: Boolean,
    refreshing: Boolean,
    comments: List<Comment>,
    address: String?,
    commentInput: String,
    commentPosting: Boolean,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onCommentChange: (String) -> Unit,
    onCommentSend: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MobileAppBar(title = "Incident", onBack = onBack)
            AppDivider()
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = onRefresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                DetailBody(
                    incident = incident,
                    loading = loading,
                    comments = comments,
                    address = address,
                    commentInput = commentInput,
                    commentPosting = commentPosting,
                    onCommentChange = onCommentChange,
                    onCommentSend = onCommentSend,
                )
            }
        }
    }
}

@Composable
private fun RoadUserDetailDesktopLayout(
    incident: Incident?,
    loading: Boolean,
    comments: List<Comment>,
    address: String?,
    commentInput: String,
    commentPosting: Boolean,
    onBack: () -> Unit,
    onCommentChange: (String) -> Unit,
    onCommentSend: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        DesktopDetailHeader(title = "Incident", onBack = onBack)
        AppDivider()
        DetailBody(
            incident = incident,
            loading = loading,
            comments = comments,
            address = address,
            commentInput = commentInput,
            commentPosting = commentPosting,
            onCommentChange = onCommentChange,
            onCommentSend = onCommentSend,
        )
    }
}

@Composable
private fun DetailBody(
    incident: Incident?,
    loading: Boolean,
    comments: List<Comment>,
    address: String?,
    commentInput: String,
    commentPosting: Boolean,
    onCommentChange: (String) -> Unit,
    onCommentSend: () -> Unit,
) {
    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        incident != null -> RoadUserDetailContent(
            incident = incident,
            comments = comments,
            address = address,
            commentInput = commentInput,
            commentPosting = commentPosting,
            onCommentChange = onCommentChange,
            onCommentSend = onCommentSend,
        )
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Incident not found.", style = MaterialTheme.typography.bodyMedium, color = LocalRoadAssistColors.current.mutedForeground)
        }
    }
}

@Composable
internal fun RoadUserDetailContent(
    incident: Incident,
    comments: List<Comment>,
    address: String?,
    commentInput: String,
    commentPosting: Boolean,
    onCommentChange: (String) -> Unit,
    onCommentSend: () -> Unit,
) {
    val context = LocalPlatformContext.current
    val colors = LocalRoadAssistColors.current
    var showLightbox by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
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
            StatusBadge(incident.status, large = true)
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

        CommentInputRow(
            input = commentInput,
            posting = commentPosting,
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
private fun CommentInputRow(
    input: String,
    posting: Boolean,
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
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .onPreviewKeyEvent { event ->
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
                        Text("Add a message...", style = MaterialTheme.typography.bodySmall, color = colors.mutedForeground)
                    }
                    innerTextField()
                }
            },
        )
        IconButton(
            onClick = onSend,
            enabled = input.isNotBlank() && !posting,
        ) {
            if (posting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (input.isNotBlank()) MaterialTheme.colorScheme.primary else colors.mutedForeground,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun DesktopDetailHeader(title: String, onBack: () -> Unit, subtitle: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LocalRoadAssistColors.current.mutedForeground)
            }
        }
    }
}
