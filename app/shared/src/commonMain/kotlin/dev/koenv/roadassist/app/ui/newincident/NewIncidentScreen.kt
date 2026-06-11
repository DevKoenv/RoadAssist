package dev.koenv.roadassist.app.ui.newincident

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.koenv.roadassist.core.IncidentCategory
import kotlinx.coroutines.launch

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Incident") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Back") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> Snackbar(data) } },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))

            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = category.name.replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    IncidentCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name.replace('_', ' ')) },
                            onClick = {
                                viewModel.updateCategory(cat)
                                expanded = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                supportingText = { Text("${description.length}/500") },
            )

            Spacer(Modifier.height(12.dp))

            Text("Location", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Row {
                when {
                    locationLoading -> Text(
                        "Fetching...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).padding(top = 8.dp),
                    )
                    location != null -> Text(
                        "%.5f, %.5f".format(location!!.latitude, location!!.longitude),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).padding(top = 8.dp),
                    )
                    else -> Text(
                        "Location unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f).padding(top = 8.dp),
                    )
                }
                OutlinedButton(onClick = { viewModel.refreshLocation() }) {
                    Text("Refresh")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text("Photo (optional)", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            if (photoBytes != null) {
                Row {
                    Text(
                        "Photo selected (${photoBytes!!.size / 1024} KB)",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f).padding(top = 8.dp),
                    )
                    TextButton(onClick = { viewModel.removePhoto() }) { Text("Remove") }
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.pickPhoto() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Add photo")
                }
            }

            Spacer(Modifier.height(16.dp))

            if (submitState is SubmitState.Error) {
                Text(
                    text = (submitState as SubmitState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = { viewModel.submit() },
                enabled = submitState !is SubmitState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (submitState is SubmitState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Report Incident")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
