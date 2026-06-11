package dev.koenv.roadassist.app.ui.newincident

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.core.IncidentCategory
import dev.koenv.roadassist.core.LatLon
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewIncidentScreen(
    viewModel: NewIncidentViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    val category by viewModel.category.collectAsState()
    val description by viewModel.description.collectAsState()
    val location by viewModel.location.collectAsState()
    val locationLoading by viewModel.locationLoading.collectAsState()
    val photoBytes by viewModel.photoBytes.collectAsState()
    val submitState by viewModel.submitState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            scope.launch { snackbarHostState.showSnackbar("Incident reported") }
            onSuccess()
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        if (maxWidth >= 700.dp) {
            DesktopLayout(viewModel, category, description, location, locationLoading, photoBytes, submitState, snackbarHostState, onBack)
        } else {
            MobileLayout(viewModel, category, description, location, locationLoading, photoBytes, submitState, snackbarHostState, onBack)
        }
    }
}

@Composable
private fun MobileLayout(
    viewModel: NewIncidentViewModel,
    category: IncidentCategory,
    description: String,
    location: LatLon?,
    locationLoading: Boolean,
    photoBytes: ByteArray?,
    submitState: SubmitState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(data) } },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            IconButton(onClick = onBack, modifier = Modifier.padding(top = 8.dp, start = 4.dp)) {
                NiBackArrow(color = MaterialTheme.colorScheme.onBackground)
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(4.dp))
                CategorySection(category = category, onCategoryChange = { viewModel.updateCategory(it) })
                Spacer(Modifier.height(12.dp))
                DescriptionSection(description = description, onDescriptionChange = { viewModel.updateDescription(it) })
                Spacer(Modifier.height(12.dp))
                LocationSection(location = location, locationLoading = locationLoading, onRefresh = { viewModel.refreshLocation() })
                Spacer(Modifier.height(12.dp))
                PhotoSection(photoBytes = photoBytes, onPickPhoto = { viewModel.pickPhoto() }, onRemovePhoto = { viewModel.removePhoto() }, isDesktop = false)
                Spacer(Modifier.height(16.dp))
                if (submitState is SubmitState.Error) {
                    Text(
                        text = (submitState as SubmitState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                SubmitButton(submitState = submitState, onSubmit = { viewModel.submit() }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DesktopLayout(
    viewModel: NewIncidentViewModel,
    category: IncidentCategory,
    description: String,
    location: LatLon?,
    locationLoading: Boolean,
    photoBytes: ByteArray?,
    submitState: SubmitState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(data) } },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { NiBackArrow(color = MaterialTheme.colorScheme.onBackground) }
                Spacer(Modifier.width(8.dp))
                Text("New incident", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onBack) {
                    Text("Cancel", color = LocalRoadAssistColors.current.mutedForeground)
                }
                Spacer(Modifier.width(8.dp))
                SubmitButton(submitState = submitState, onSubmit = { viewModel.submit() })
            }
            HorizontalDivider(color = LocalRoadAssistColors.current.border, thickness = 0.5.dp)
            Box(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 760.dp)
                        .fillMaxWidth()
                        .border(1.dp, LocalRoadAssistColors.current.border, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.weight(1f)) {
                            CategorySection(category = category, onCategoryChange = { viewModel.updateCategory(it) })
                        }
                        Box(Modifier.weight(1f)) {
                            LocationSection(location = location, locationLoading = locationLoading, onRefresh = { viewModel.refreshLocation() })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    DescriptionSection(description = description, onDescriptionChange = { viewModel.updateDescription(it) })
                    Spacer(Modifier.height(12.dp))
                    PhotoSection(photoBytes = photoBytes, onPickPhoto = { viewModel.pickPhoto() }, onRemovePhoto = { viewModel.removePhoto() }, isDesktop = true)
                    if (submitState is SubmitState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = (submitState as SubmitState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySection(category: IncidentCategory, onCategoryChange: (IncidentCategory) -> Unit) {
    val borderColor = LocalRoadAssistColors.current.border
    val mutedColor = LocalRoadAssistColors.current.mutedForeground

    SectionLabel("CATEGORY")
    Spacer(Modifier.height(6.dp))
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        Row(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NiCategoryIcon(category = category, color = mutedColor)
            Spacer(Modifier.width(10.dp))
            Text(category.displayName(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            NiChevronDown(color = mutedColor)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            IncidentCategory.entries.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.displayName()) },
                    onClick = { onCategoryChange(cat); expanded = false },
                    leadingIcon = { NiCategoryIcon(category = cat, color = MaterialTheme.colorScheme.onSurface) },
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(description: String, onDescriptionChange: (String) -> Unit) {
    val borderColor = LocalRoadAssistColors.current.border
    val mutedColor = LocalRoadAssistColors.current.mutedForeground

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        SectionLabel("DESCRIPTION")
        Text("${description.length} / 500", style = MaterialTheme.typography.labelSmall, color = mutedColor)
    }
    Spacer(Modifier.height(6.dp))
    BasicTextField(
        value = description,
        onValueChange = onDescriptionChange,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
            .defaultMinSize(minHeight = 80.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
        maxLines = 6,
        decorationBox = { innerTextField ->
            Box {
                if (description.isEmpty()) {
                    Text("Describe what happened...", style = MaterialTheme.typography.bodyMedium, color = mutedColor)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun LocationSection(location: LatLon?, locationLoading: Boolean, onRefresh: () -> Unit) {
    val borderColor = LocalRoadAssistColors.current.border
    val mutedColor = LocalRoadAssistColors.current.mutedForeground

    SectionLabel("LOCATION · AUTO")
    Spacer(Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NiLocationPin(color = mutedColor)
        Spacer(Modifier.width(10.dp))
        Text(
            text = when {
                locationLoading -> "Fetching location..."
                location != null -> "%.4f, %.4f".format(location.latitude, location.longitude)
                else -> "Location unavailable"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (location == null && !locationLoading) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRefresh, modifier = Modifier.size(36.dp)) {
            NiRefreshIcon(color = mutedColor)
        }
    }
}

@Composable
private fun PhotoSection(
    photoBytes: ByteArray?,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    isDesktop: Boolean,
) {
    val borderColor = LocalRoadAssistColors.current.border
    val mutedColor = LocalRoadAssistColors.current.mutedForeground

    SectionLabel("PHOTO · OPTIONAL")
    Spacer(Modifier.height(6.dp))
    if (photoBytes != null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NiCameraIcon(color = mutedColor)
            Spacer(Modifier.width(10.dp))
            Text("Photo selected (${photoBytes.size / 1024} KB)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            TextButton(onClick = onRemovePhoto) {
                Text("Remove", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .clickable(onClick = onPickPhoto)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NiCameraIcon(color = mutedColor)
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (isDesktop) "Drag a photo here, or click to add" else "Add a photo",
                style = MaterialTheme.typography.bodyMedium,
                color = mutedColor,
            )
        }
    }
}

@Composable
private fun SubmitButton(submitState: SubmitState, onSubmit: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onSubmit,
        enabled = submitState !is SubmitState.Loading,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        if (submitState is SubmitState.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
            Text("Submit report", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp),
        color = LocalRoadAssistColors.current.mutedForeground,
    )
}

private fun IncidentCategory.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

// ---- Icons ----

@Composable
private fun NiBackArrow(color: Color) {
    Box(Modifier.size(20.dp).drawBehind {
        val s = size.width * 0.12f
        drawLine(color, Offset(size.width * 0.62f, size.height * 0.2f), Offset(size.width * 0.25f, size.height * 0.5f), s, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.25f, size.height * 0.5f), Offset(size.width * 0.62f, size.height * 0.8f), s, StrokeCap.Round)
    })
}

@Composable
private fun NiChevronDown(color: Color) {
    Box(Modifier.size(14.dp).drawBehind {
        val s = size.width * 0.14f
        drawLine(color, Offset(size.width * 0.2f, size.height * 0.38f), Offset(size.width * 0.5f, size.height * 0.65f), s, StrokeCap.Round)
        drawLine(color, Offset(size.width * 0.5f, size.height * 0.65f), Offset(size.width * 0.8f, size.height * 0.38f), s, StrokeCap.Round)
    })
}

@Composable
private fun NiCategoryIcon(category: IncidentCategory, color: Color) {
    Box(Modifier.size(18.dp).drawBehind {
        val s = size.width * 0.1f
        val w = size.width; val h = size.height
        when (category) {
            IncidentCategory.BREAKDOWN -> {
                drawLine(color, Offset(w * 0.25f, h * 0.75f), Offset(w * 0.75f, h * 0.25f), s * 1.3f, StrokeCap.Round)
                drawCircle(color, w * 0.19f, Offset(w * 0.76f, h * 0.22f), style = Stroke(width = s))
                drawCircle(color, w * 0.19f, Offset(w * 0.24f, h * 0.78f), style = Stroke(width = s))
            }
            IncidentCategory.ACCIDENT -> {
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.1f); lineTo(w * 0.92f, h * 0.87f); lineTo(w * 0.08f, h * 0.87f); close()
                }
                drawPath(path, color, style = Stroke(width = s, join = StrokeJoin.Round, cap = StrokeCap.Round))
                drawLine(color, Offset(w * 0.5f, h * 0.38f), Offset(w * 0.5f, h * 0.62f), s, StrokeCap.Round)
                drawCircle(color, s * 0.65f, Offset(w * 0.5f, h * 0.73f))
            }
            IncidentCategory.OBSTRUCTION -> {
                val path = Path().apply {
                    moveTo(w * 0.1f, h * 0.45f); lineTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.9f, h * 0.45f); lineTo(w * 0.9f, h * 0.85f)
                    lineTo(w * 0.1f, h * 0.85f); close()
                }
                drawPath(path, color, style = Stroke(width = s, join = StrokeJoin.Round))
            }
            IncidentCategory.OTHER -> {
                val cx = w / 2f; val cy = h / 2f
                drawCircle(color, cx * 0.85f, Offset(cx, cy), style = Stroke(width = s))
                drawLine(color, Offset(cx, cy - s * 0.6f), Offset(cx, cy + s * 0.8f), s, StrokeCap.Round)
                drawCircle(color, s * 0.6f, Offset(cx, cy + s * 2f))
            }
        }
    })
}

@Composable
private fun NiLocationPin(color: Color) {
    Box(Modifier.size(18.dp).drawBehind {
        val cx = size.width / 2f
        val r = size.width * 0.32f
        val cy = size.height * 0.36f
        val s = size.width * 0.1f
        drawCircle(color, r, Offset(cx, cy), style = Stroke(width = s))
        drawCircle(color, r * 0.32f, Offset(cx, cy))
        drawLine(color, Offset(cx - r * 0.85f, cy + r * 0.55f), Offset(cx, size.height * 0.92f), s, StrokeCap.Round)
        drawLine(color, Offset(cx + r * 0.85f, cy + r * 0.55f), Offset(cx, size.height * 0.92f), s, StrokeCap.Round)
    })
}

@Composable
private fun NiRefreshIcon(color: Color) {
    Box(Modifier.size(18.dp).drawBehind {
        val cx = size.width / 2f; val cy = size.height / 2f
        val r = size.width * 0.36f; val s = size.width * 0.11f
        drawArc(color, startAngle = -50f, sweepAngle = 280f, useCenter = false,
            topLeft = Offset(cx - r, cy - r), size = Size(r * 2, r * 2),
            style = Stroke(width = s, cap = StrokeCap.Round))
        val endRad = (-50 + 280).toDouble() * PI / 180.0
        val ex = (cx + r * cos(endRad)).toFloat(); val ey = (cy + r * sin(endRad)).toFloat()
        drawLine(color, Offset(ex, ey), Offset(ex - s * 1.3f, ey - s * 1.6f), s, StrokeCap.Round)
        drawLine(color, Offset(ex, ey), Offset(ex + s * 1.6f, ey - s * 0.7f), s, StrokeCap.Round)
    })
}

@Composable
private fun NiCameraIcon(color: Color) {
    Box(Modifier.size(20.dp).drawBehind {
        val w = size.width; val h = size.height; val s = w * 0.1f
        drawRoundRect(color, topLeft = Offset(w * 0.05f, h * 0.32f), size = Size(w * 0.9f, h * 0.55f),
            cornerRadius = CornerRadius(w * 0.08f), style = Stroke(width = s))
        drawCircle(color, w * 0.18f, Offset(w * 0.5f, h * 0.595f), style = Stroke(width = s))
        drawLine(color, Offset(w * 0.33f, h * 0.32f), Offset(w * 0.41f, h * 0.2f), s, StrokeCap.Round)
        drawLine(color, Offset(w * 0.41f, h * 0.2f), Offset(w * 0.59f, h * 0.2f), s, StrokeCap.Round)
        drawLine(color, Offset(w * 0.59f, h * 0.2f), Offset(w * 0.67f, h * 0.32f), s, StrokeCap.Round)
    })
}
