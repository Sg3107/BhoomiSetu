package com.satya.bhoomisetu.presentation.property

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.satya.bhoomisetu.domain.model.PropertyNote
import com.satya.bhoomisetu.domain.model.PropertyNoteType
import com.satya.bhoomisetu.presentation.components.PrimaryButton
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.Constants
import com.satya.bhoomisetu.util.CustomTabsLauncher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PropertyDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: PropertyDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val property by viewModel.property.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isDeleted by viewModel.isDeleted.collectAsState()

    var noteText by remember { mutableStateOf("") }
    var noteType by remember { mutableStateOf(PropertyNoteType.GENERAL) }
    var noteTypeExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(isDeleted) {
        if (isDeleted) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(property?.nickname ?: "Property", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete property", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            property?.let { prop ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Dimens.paddingLarge)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
                            Text("${prop.village}, ${prop.mandal}", style = MaterialTheme.typography.titleMedium)
                            Text(prop.district, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(Dimens.paddingXSmall))
                            Text("Survey No. ${prop.surveyNumber} \u2022 ${if (prop.recordType.name == "ADANGAL") "Adangal" else "1-B"}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Spacer(Modifier.height(Dimens.paddingLarge))
                    Text("Check the official sites", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Opens in a real browser tab -- you log in and search with your own details, exactly like visiting the site directly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(Dimens.paddingMedium))

                    PrimaryButton(
                        text = "Open Meebhoomi (Adangal / 1-B)",
                        onClick = {
                            CustomTabsLauncher.open(context, Constants.MEEBHOOMI_URL)
                            viewModel.addNote(PropertyNoteType.MEEBHOOMI_CHECK, "Opened Meebhoomi")
                        }
                    )
                    Spacer(Modifier.height(Dimens.paddingSmall))
                    OutlinedButton(
                        onClick = {
                            CustomTabsLauncher.open(context, Constants.IGRS_EC_URL)
                            viewModel.addNote(PropertyNoteType.EC_CHECK, "Opened IGRS-EC")
                        },
                        modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = null)
                        Spacer(Modifier.width(Dimens.paddingSmall))
                        Text("Open IGRS-EC (Encumbrance)")
                    }

                    Spacer(Modifier.height(Dimens.paddingXLarge))
                    Text("Land history (your notes)", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(Dimens.paddingSmall))

                    Row(verticalAlignment = Alignment.Top) {
                        OutlinedTextField(
                            value = noteText,
                            onValueChange = { noteText = it },
                            label = { Text("Add a note") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(Dimens.paddingSmall))
                        IconButton(onClick = {
                            viewModel.addNote(noteType, noteText)
                            noteText = ""
                        }) {
                            Icon(Icons.Default.Send, contentDescription = "Add note")
                        }
                    }
                    Spacer(Modifier.height(Dimens.paddingXSmall))

                    ExposedDropdownMenuBox(
                        expanded = noteTypeExpanded,
                        onExpandedChange = { noteTypeExpanded = !noteTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = noteType.label(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Note type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = noteTypeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        ExposedDropdownMenu(expanded = noteTypeExpanded, onDismissRequest = { noteTypeExpanded = false }) {
                            PropertyNoteType.entries.forEach { type ->
                                DropdownMenuItem(text = { Text(type.label()) }, onClick = {
                                    noteType = type
                                    noteTypeExpanded = false
                                })
                            }
                        }
                    }

                    Spacer(Modifier.height(Dimens.paddingMedium))

                    if (notes.isEmpty()) {
                        Text(
                            "No notes yet -- opening a site above adds one automatically, or add your own.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        notes.forEach { note -> NoteRow(note) }
                    }
                }
            }

            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete this property?") },
                    text = { Text("This removes it and all its notes from your device. This can't be undone.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteConfirm = false
                            viewModel.deleteProperty()
                        }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

private fun PropertyNoteType.label(): String = when (this) {
    PropertyNoteType.MEEBHOOMI_CHECK -> "Meebhoomi check"
    PropertyNoteType.EC_CHECK -> "EC check"
    PropertyNoteType.GENERAL -> "General note"
}

@Composable
private fun NoteRow(note: PropertyNote) {
    val dateText = remember(note.createdAt) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(note.createdAt))
    }
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.paddingXSmall),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevation)
    ) {
        Row(modifier = Modifier.padding(Dimens.paddingMedium), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = if (note.type == PropertyNoteType.GENERAL) Icons.Default.Description else Icons.Default.Gavel,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(Dimens.paddingSmall))
            Column {
                Text(note.text, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(Dimens.paddingXSmall))
                Text(dateText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
