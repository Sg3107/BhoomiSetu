package com.satya.bhoomisetu.presentation.documentwriter

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.domain.model.DocumentWriterRequest
import com.satya.bhoomisetu.domain.model.WriterClientDocument
import com.satya.bhoomisetu.domain.model.WriterDocTypes
import com.satya.bhoomisetu.domain.model.WriterDraftDocument
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.Constants
import com.satya.bhoomisetu.util.FileDownloadHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentWriterRequestDetailScreen(
    viewModel: DocumentWriterRequestDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) } // 0 = Client Docs, 1 = Final Drafts, 2 = Chat
    var showClientUploadDialog by remember { mutableStateOf(false) }
    var selectedClientFileUri by remember { mutableStateOf<Uri?>(null) }

    var showDraftUploadDialog by remember { mutableStateOf(false) }
    var selectedDraftFileUri by remember { mutableStateOf<Uri?>(null) }

    val clientDocPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedClientFileUri = uri
            showClientUploadDialog = true
        }
    }

    val draftDocPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedDraftFileUri = uri
            showDraftUploadDialog = true
        }
    }

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
        uiState.successMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessages()
        }
    }

    val request = uiState.request
    val isClient = request?.userId == uiState.currentUserId
    val isWriter = request?.writerId == uiState.currentUserId

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = request?.deedType?.replace('_', ' ') ?: "Deed Drafting Request",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = request?.propertyTitle ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && request == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (request == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Deed request not found or access denied.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Overview Card
                RequestHeaderOverviewCard(
                    request = request,
                    isClient = isClient,
                    isWriter = isWriter,
                    onAccept = { viewModel.acceptRequest() },
                    onComplete = { viewModel.completeRequest() }
                )

                // Tabs: 0: Supporting Docs, 1: Final Drafts, 2: Chat
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Client Docs (${request.clientDocuments.size})") },
                        icon = { Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Draft Deeds (${request.draftDocuments.size})") },
                        icon = { Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Chat") },
                        icon = { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                when (selectedTab) {
                    0 -> {
                        // Client Documents Tab
                        ClientDocumentsSection(
                            request = request,
                            isClient = isClient,
                            onPickFile = { clientDocPickerLauncher.launch("*/*") },
                            onDownloadFile = { url, fileName ->
                                coroutineScope.launch {
                                    val fullUrl = if (url.startsWith("http")) url else Constants.BASE_URL.replace("/api/v1/", "") + url
                                    FileDownloadHelper.downloadAndSaveFile(context, fullUrl, fileName)
                                }
                            }
                        )
                    }
                    1 -> {
                        // Draft Deeds Tab (Final drafts uploaded by writer)
                        DraftDeedsSection(
                            request = request,
                            isWriter = isWriter,
                            isClient = isClient,
                            onPickDraftFile = { draftDocPickerLauncher.launch("*/*") },
                            onCompleteOrder = { viewModel.completeRequest() },
                            onDownloadFile = { url, fileName ->
                                coroutineScope.launch {
                                    val fullUrl = if (url.startsWith("http")) url else Constants.BASE_URL.replace("/api/v1/", "") + url
                                    FileDownloadHelper.downloadAndSaveFile(context, fullUrl, fileName)
                                }
                            }
                        )
                    }
                    2 -> {
                        // In-App Chat Tab
                        WriterChatSection(
                            messages = uiState.messages,
                            currentUserId = uiState.currentUserId,
                            senderName = if (isWriter) request.writerName else request.userName,
                            isUnlocked = request.status != "PENDING",
                            isSending = uiState.isSendingMessage,
                            onSendMessage = { text, name -> viewModel.sendMessage(text, name) }
                        )
                    }
                }
            }
        }
    }

    // Dialog for client uploading a supporting doc
    if (showClientUploadDialog && selectedClientFileUri != null) {
        UploadClientDocDialog(
            fileUri = selectedClientFileUri!!,
            isUploading = uiState.isUploading,
            onDismiss = {
                showClientUploadDialog = false
                selectedClientFileUri = null
            },
            onUpload = { docType, title ->
                viewModel.uploadClientDocument(selectedClientFileUri!!, docType, title, context)
                showClientUploadDialog = false
                selectedClientFileUri = null
            }
        )
    }

    // Dialog for Document Writer uploading the Final Draft Deed!
    if (showDraftUploadDialog && selectedDraftFileUri != null) {
        UploadDeedDraftDialog(
            deedType = request?.deedType ?: "SALE_DEED",
            fileUri = selectedDraftFileUri!!,
            isUploading = uiState.isUploading,
            onDismiss = {
                showDraftUploadDialog = false
                selectedDraftFileUri = null
            },
            onUpload = { title, notes, isFinal ->
                viewModel.uploadDeedDraft(selectedDraftFileUri!!, title, notes, isFinal, context)
                showDraftUploadDialog = false
                selectedDraftFileUri = null
            }
        )
    }
}

@Composable
private fun RequestHeaderOverviewCard(
    request: DocumentWriterRequest,
    isClient: Boolean,
    isWriter: Boolean,
    onAccept: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val statusColor = when (request.status) {
        "COMPLETED" -> Color(0xFF2E7D32)
        "DRAFT_UPLOADED" -> Color(0xFF1565C0)
        "ACCEPTED" -> Color(0xFF00796B)
        "REJECTED" -> Color(0xFFC62828)
        else -> Color(0xFFEF6C00)
    }

    val statusText = when (request.status) {
        "COMPLETED" -> "Deed Completed"
        "DRAFT_UPLOADED" -> "Final Draft Deed Ready"
        "ACCEPTED" -> "Accepted / In Progress"
        "REJECTED" -> "Declined"
        else -> "Pending Writer Review"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.paddingMedium),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.deedType.replace('_', ' '),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${request.district}${if (request.mandal.isNotBlank()) ", " + request.mandal else ""}${if (request.village.isNotBlank()) ", " + request.village else ""}${if (request.surveyNumber.isNotBlank()) " | Sy: " + request.surveyNumber else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Notes: ${request.description}",
                style = MaterialTheme.typography.bodySmall
            )

            Divider(modifier = Modifier.padding(vertical = Dimens.paddingSmall))

            // Parties Contact Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Client Info
                Column(modifier = Modifier.weight(1f)) {
                    Text("Client", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(request.userName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    if (request.userPhone != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.userPhone}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(request.userPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Locked until accepted", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }

                // Writer Info
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Document Writer", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(request.writerName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text("Lic: ${request.writerLicense}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (request.writerPhone != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.writerPhone}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(request.writerPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Action button if Pending for Writer
            if (isWriter && request.status == "PENDING") {
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Accept Order & Start Drafting")
                }
            }
        }
    }
}

@Composable
private fun ClientDocumentsSection(
    request: DocumentWriterRequest,
    isClient: Boolean,
    onPickFile: () -> Unit,
    onDownloadFile: (url: String, fileName: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Supporting Documents from Client",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (isClient && request.status != "COMPLETED") {
                Button(
                    onClick = onPickFile,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload Doc")
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

        if (request.clientDocuments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No documents uploaded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (isClient) {
                        Text("Upload your parent deed, 1B, EC, or Aadhaar so writer can draft accurately.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                items(request.clientDocuments) { doc ->
                    ClientDocCard(doc = doc, onDownload = { onDownloadFile(doc.fileUrl, doc.originalFileName) })
                }
            }
        }
    }
}

@Composable
private fun ClientDocCard(
    doc: WriterClientDocument,
    onDownload: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.documentType.replace('_', ' '),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = doc.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${doc.originalFileName} • ${dateFormat.format(Date(doc.uploadedAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            IconButton(onClick = onDownload) {
                Icon(Icons.Default.Download, contentDescription = "Download Document", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun DraftDeedsSection(
    request: DocumentWriterRequest,
    isWriter: Boolean,
    isClient: Boolean,
    onPickDraftFile: () -> Unit,
    onCompleteOrder: () -> Unit,
    onDownloadFile: (url: String, fileName: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.paddingMedium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Final Draft Deeds",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Drafts prepared and uploaded by the Certified Writer",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            if (isWriter && request.status in listOf("ACCEPTED", "DRAFT_UPLOADED")) {
                Button(
                    onClick = onPickDraftFile,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload Draft")
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

        if (request.draftDocuments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No draft deeds uploaded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (isWriter) {
                        Text("Once you have drafted the deed, upload the document here for the client.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    } else {
                        Text("The document writer will upload the drafted deed here once prepared.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                items(request.draftDocuments) { draft ->
                    DraftDocCard(draft = draft, onDownload = { onDownloadFile(draft.fileUrl, draft.originalFileName) })
                }
            }

            if (request.status == "DRAFT_UPLOADED") {
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                Button(
                    onClick = onCompleteOrder,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Mark Deed Drafting as COMPLETED")
                }
            }
        }
    }
}

@Composable
private fun DraftDocCard(
    draft: WriterDraftDocument,
    onDownload: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0).copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = draft.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }

                if (draft.isFinal) {
                    Text(
                        text = "FINAL DRAFT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (draft.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Writer Notes: ${draft.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${draft.originalFileName} • ${dateFormat.format(Date(draft.uploadedAt))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Button(
                    onClick = onDownload,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0))
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download Deed")
                }
            }
        }
    }
}

@Composable
private fun WriterChatSection(
    messages: List<com.satya.bhoomisetu.domain.model.ChatMessage>,
    currentUserId: String,
    senderName: String,
    isUnlocked: Boolean,
    isSending: Boolean,
    onSendMessage: (text: String, name: String) -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!isUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.paddingLarge),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                    Text(
                        "Chat is locked until the Document Writer accepts the order.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderId == currentUserId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (isMe) "You" else msg.senderName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = { Text("Message Document Writer...") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            onSendMessage(messageText.trim(), senderName)
                            messageText = ""
                        }
                    },
                    enabled = messageText.isNotBlank() && !isSending
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadClientDocDialog(
    fileUri: Uri,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onUpload: (docType: String, title: String) -> Unit
) {
    var selectedDocType by remember { mutableStateOf(WriterDocTypes.REQUIRED_DOCS.first().code) }
    var menuExpanded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Client Document", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)) {
                Text("Select Document Type:", style = MaterialTheme.typography.labelMedium)
                ExposedDropdownMenuBox(
                    expanded = menuExpanded,
                    onExpandedChange = { menuExpanded = it }
                ) {
                    val currentTitle = WriterDocTypes.REQUIRED_DOCS.find { it.code == selectedDocType }?.title ?: selectedDocType
                    OutlinedTextField(
                        value = currentTitle,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        WriterDocTypes.REQUIRED_DOCS.forEach { doc ->
                            DropdownMenuItem(
                                text = { Text(doc.title) },
                                onClick = {
                                    selectedDocType = doc.code
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Description / Title") },
                    placeholder = { Text("e.g. Previous 2012 Sale Deed") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpload(selectedDocType, title.ifBlank { "Client Document" })
                },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Upload")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun UploadDeedDraftDialog(
    deedType: String,
    fileUri: Uri,
    isUploading: Boolean,
    onDismiss: () -> Unit,
    onUpload: (title: String, notes: String, isFinal: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("${deedType.replace('_', ' ')} Draft") }
    var notes by remember { mutableStateOf("") }
    var isFinal by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Draft Deed Document", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Draft Title *") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes for Client") },
                    placeholder = { Text("e.g. Please verify boundary schedule and spelling of names.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isFinal,
                        onCheckedChange = { isFinal = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ready for Registration (Final Draft)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onUpload(title.trim(), notes.trim(), isFinal)
                    }
                },
                enabled = !isUploading && title.isNotBlank()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Upload Draft Deed")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUploading) {
                Text("Cancel")
            }
        }
    )
}
