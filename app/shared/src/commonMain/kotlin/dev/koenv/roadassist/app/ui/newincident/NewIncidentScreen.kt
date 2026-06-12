package dev.koenv.roadassist.app.ui.newincident

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.app.geocoding.GeocodingResult
import dev.koenv.roadassist.app.theme.LocalRoadAssistColors
import dev.koenv.roadassist.app.ui.components.AppDivider
import dev.koenv.roadassist.app.ui.components.MobileAppBar
import dev.koenv.roadassist.app.ui.components.PrimaryButton
import dev.koenv.roadassist.app.ui.components.SecondaryButton
import dev.koenv.roadassist.app.ui.components.SectionLabel
import dev.koenv.roadassist.app.ui.home.RoadUserNavRail
import dev.koenv.roadassist.app.ui.home.RoadUserTab
import dev.koenv.roadassist.core.IncidentCategory
import dev.koenv.roadassist.core.LatLon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewIncidentScreen(
    viewModel: NewIncidentViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    val category by viewModel.category.collectAsState()
    val description by viewModel.description.collectAsState()
    val location by viewModel.location.collectAsState()
    val locationLoading by viewModel.locationLoading.collectAsState()
    val locationLabel by viewModel.locationLabel.collectAsState()
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
            DesktopLayout(viewModel, category, description, location, locationLabel, locationLoading, photoBytes, submitState, snackbarHostState, onBack, onLogout)
        } else {
            MobileLayout(viewModel, category, description, location, locationLabel, locationLoading, photoBytes, submitState, snackbarHostState, onBack)
        }
    }
}

@Composable
private fun MobileLayout(
    viewModel: NewIncidentViewModel,
    category: IncidentCategory,
    description: String,
    location: LatLon?,
    locationLabel: String?,
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
            MobileAppBar(title = "New incident", onBack = onBack)
            AppDivider()
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(12.dp))
                CategorySection(category = category, onCategoryChange = { viewModel.updateCategory(it) })
                Spacer(Modifier.height(12.dp))
                DescriptionSection(description = description, onDescriptionChange = { viewModel.updateDescription(it) })
                Spacer(Modifier.height(12.dp))
                LocationSection(
                    location = location,
                    locationLabel = locationLabel,
                    locationLoading = locationLoading,
                    onRefresh = { viewModel.refreshLocation() },
                    onManualLocation = { lat, lon, label -> viewModel.setManualLocation(lat, lon, label) },
                    onSearch = { query -> viewModel.searchLocations(query) },
                )
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
    locationLabel: String?,
    locationLoading: Boolean,
    photoBytes: ByteArray?,
    submitState: SubmitState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onLogout: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(data) } },
    ) { padding ->
        Row(modifier = Modifier.fillMaxSize().padding(padding)) {
            RoadUserNavRail(selectedTab = RoadUserTab.Active, onTabChange = {}, onLogout = onLogout)
            Box(modifier = Modifier.width(0.5.dp).fillMaxSize().background(LocalRoadAssistColors.current.border))
            Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(20.dp).clickable { onBack() },
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("New incident", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.weight(1f))
                    SecondaryButton(onClick = onBack) {
                        Text("Cancel", color = LocalRoadAssistColors.current.mutedForeground, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.width(8.dp))
                    SubmitButton(submitState = submitState, onSubmit = { viewModel.submit() })
                }
                AppDivider()
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
                            Column(Modifier.weight(1f)) {
                                CategorySection(category = category, onCategoryChange = { viewModel.updateCategory(it) })
                            }
                            Column(Modifier.weight(1f)) {
                                LocationSection(
                                    location = location,
                                    locationLabel = locationLabel,
                                    locationLoading = locationLoading,
                                    onRefresh = { viewModel.refreshLocation() },
                                    isDesktop = true,
                                    onManualLocation = { lat, lon, label -> viewModel.setManualLocation(lat, lon, label) },
                                    onSearch = { query -> viewModel.searchLocations(query) },
                                )
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
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = mutedColor, modifier = Modifier.size(18.dp))
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
private fun LocationSection(
    location: LatLon?,
    locationLabel: String?,
    locationLoading: Boolean,
    onRefresh: () -> Unit,
    isDesktop: Boolean = false,
    onManualLocation: ((Double, Double, String) -> Unit)? = null,
    onSearch: (suspend (String) -> List<GeocodingResult>)? = null,
) {
    val borderColor = LocalRoadAssistColors.current.border
    val mutedColor = LocalRoadAssistColors.current.mutedForeground
    val primaryColor = MaterialTheme.colorScheme.primary
    var searchMode by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<GeocodingResult>>(emptyList()) }
    var searching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val locationMissing = location == null && !locationLoading

    LaunchedEffect(query, searchMode) {
        if (!searchMode) return@LaunchedEffect
        results = emptyList()
        searching = false
        if (query.length >= 3) {
            delay(400)
            searching = true
            try {
                results = onSearch?.invoke(query) ?: emptyList()
            } finally {
                searching = false
            }
        }
    }

    LaunchedEffect(searchMode) {
        if (searchMode) focusRequester.requestFocus()
    }

    SectionLabel("LOCATION · AUTO")
    Spacer(Modifier.height(6.dp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (searchMode) primaryColor else borderColor, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp)),
    ) {
        if (!searchMode) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (locationMissing) Modifier.clickable { searchMode = true } else Modifier)
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (locationMissing) MaterialTheme.colorScheme.error else mutedColor,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = when {
                        locationLoading -> "Fetching location..."
                        locationLabel != null -> locationLabel
                        location != null -> "%.4f, %.4f".format(location.latitude, location.longitude)
                        else -> "Location unavailable — tap to search"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (locationMissing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (locationLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp, color = mutedColor)
                } else {
                    if (!isDesktop) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Retry GPS",
                            tint = mutedColor,
                            modifier = Modifier.size(18.dp).clickable(onClick = onRefresh),
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search for address",
                        tint = mutedColor,
                        modifier = Modifier.size(18.dp).clickable { searchMode = true },
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(primaryColor),
                    singleLine = true,
                    modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    "Search for address...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = mutedColor,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                Spacer(Modifier.width(8.dp))
                if (searching) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp, color = mutedColor)
                } else {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cancel search",
                        tint = mutedColor,
                        modifier = Modifier.size(18.dp).clickable {
                            searchMode = false
                            query = ""
                            results = emptyList()
                        },
                    )
                }
            }

            if (results.isNotEmpty()) {
                HorizontalDivider(color = borderColor, thickness = 0.5.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                ) {
                    results.forEachIndexed { index, result ->
                        Text(
                            text = result.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onManualLocation?.invoke(result.location.latitude, result.location.longitude, result.label)
                                    searchMode = false
                                    query = ""
                                    results = emptyList()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (index < results.lastIndex) {
                            HorizontalDivider(color = borderColor, thickness = 0.5.dp)
                        }
                    }
                }
            } else if (!searching && query.length >= 3) {
                HorizontalDivider(color = borderColor, thickness = 0.5.dp)
                Text(
                    "No results found.",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = mutedColor,
                )
            }

            HorizontalDivider(color = borderColor, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Map,
                    contentDescription = null,
                    tint = mutedColor.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Select on map",
                    style = MaterialTheme.typography.bodyMedium,
                    color = mutedColor.copy(alpha = 0.4f),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "Coming soon",
                    style = MaterialTheme.typography.labelSmall,
                    color = mutedColor.copy(alpha = 0.4f),
                )
            }
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
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = mutedColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Photo selected (${photoBytes.size / 1024} KB)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            TextButton(onClick = onRemovePhoto) {
                Text("Remove", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
            }
        }
    } else if (isDesktop) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onPickPhoto)
                .padding(vertical = 28.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = mutedColor, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(8.dp))
                Text("Drag a photo here, or click to add", style = MaterialTheme.typography.bodyMedium, color = mutedColor)
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onPickPhoto)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = mutedColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text("Add a photo", style = MaterialTheme.typography.bodyMedium, color = mutedColor)
        }
    }
}

@Composable
private fun SubmitButton(submitState: SubmitState, onSubmit: () -> Unit, modifier: Modifier = Modifier) {
    PrimaryButton(
        onClick = onSubmit,
        enabled = submitState !is SubmitState.Loading,
        modifier = modifier,
    ) {
        if (submitState is SubmitState.Loading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
            Text("Submit report", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun IncidentCategory.displayName(): String = name.lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun NiCategoryIcon(category: IncidentCategory, color: Color, modifier: Modifier = Modifier.size(18.dp)) {
    val icon = when (category) {
        IncidentCategory.BREAKDOWN -> Icons.Default.Build
        IncidentCategory.ACCIDENT -> Icons.Default.WarningAmber
        IncidentCategory.OBSTRUCTION -> Icons.Default.Inventory2
        IncidentCategory.OTHER -> Icons.Default.Help
    }
    Icon(icon, contentDescription = null, tint = color, modifier = modifier)
}
