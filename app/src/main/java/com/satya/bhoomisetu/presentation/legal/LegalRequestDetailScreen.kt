package com.satya.bhoomisetu.presentation.legal

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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.runtime.rememberCoroutineScope
import com.satya.bhoomisetu.util.FileDownloadHelper
import kotlinx.coroutines.launch
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.domain.model.DocTypeInfo
import com.satya.bhoomisetu.domain.model.LandVerificationDocTypes
import com.satya.bhoomisetu.domain.model.LegalDocumentItem
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalRequestDetailScreen(
    viewModel: LegalRequestDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val request = uiState.request
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showReviewDocDialog by remember { mutableStateOf<LegalDocumentItem?>(null) }
    var showOverallReviewDialog by remember { mutableStateOf(false) }
    var targetDocTypeForUpload by remember { mutableStateOf<DocTypeInfo?>(null) }
    var chatInputText by remember { mutableStateOf("") }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            val docInfo = targetDocTypeForUpload
            val docType = docInfo?.code ?: "OTHER"
            val title = docInfo?.title ?: "Land Document"
            viewModel.uploadDocument(selectedUri, docType, title, context)
            targetDocTypeForUpload = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = request?.propertyTitle ?: "Verification Request",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
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
                Text("Request details not found.")
            }
        } else {
            val isAdvisor = uiState.currentUserId == request.advisorId
            val isAccepted = request.status in listOf("ACCEPTED", "VERIFIED", "NEEDS_CHANGES", "COMPLETED")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Header & Status Overview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.paddingMedium),
                    shape = MaterialTheme.shapes.large,
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
                                text = request.propertyTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )

                            val (statusColor, statusBg) = when (request.status) {
                                "ACCEPTED" -> Pair(Color(0xFF2E7D32), Color(0xFFE8F5E9))
                                "VERIFIED" -> Pair(Color(0xFF1565C0), Color(0xFFE3F2FD))
                                "NEEDS_CHANGES" -> Pair(Color(0xFFE65100), Color(0xFFFFF3E0))
                                "REJECTED" -> Pair(Color(0xFFC62828), Color(0xFFFFEBEE))
                                else -> Pair(Color(0xFFF57C00), Color(0xFFFFF8E1))
                            }

                            Box(
                                modifier = Modifier
                                    .background(statusBg, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = request.status,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                        Text(
                            text = "Location: ${request.district}${if (request.mandal.isNotBlank()) ", ${request.mandal}" else ""}${if (request.village.isNotBlank()) ", ${request.village}" else ""}${if (request.surveyNumber.isNotBlank()) " | Sy No: ${request.surveyNumber}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                        // Participants & Contact Section
                        if (!isAccepted) {
                            // PENDING: Contact is hidden
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "🔒 Phone numbers & chat will be unlocked once accepted by advisor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            if (isAdvisor) {
                                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                                Button(
                                    onClick = { viewModel.acceptRequest() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Accept Request & Unlock Contact")
                                }
                            }
                        } else {
                            // ACCEPTED: Phone numbers visible
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = if (isAdvisor) "Client: ${request.userName}" else "Legal Advisor: ${request.advisorName}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        val displayPhone = if (isAdvisor) request.userPhone else request.advisorPhone
                                        Text(
                                            text = "Phone: +91 ${displayPhone ?: "Available"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }

                                    val phoneToCall = if (isAdvisor) request.userPhone else request.advisorPhone
                                    phoneToCall?.let { phone ->
                                        OutlinedButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                                context.startActivity(intent)
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32))
                                        ) {
                                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Call")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Tabs: Documents Verification vs In-App Chat
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Documents (${request.documents.size})") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("In-App Chat (${uiState.messages.size})") }
                    )
                }

                if (selectedTabIndex == 0) {
                    // Documents Verification View
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(Dimens.paddingMedium)
                    ) {
                        // Legal Review Summary Banner if available
                        if (request.verificationSummary.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Assignment,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )


                                        Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                                        Text(
                                            text = "Legal Opinion & Required Changes",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(Dimens.paddingXSmall))
                                    Text(
                                        text = request.verificationSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                        }

                        if (isAdvisor && isAccepted) {
                            Button(
                                onClick = { showOverallReviewDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.RateReview, contentDescription = null)
                                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                                Text("Submit / Update Legal Opinion")
                            }
                            Spacer(modifier = Modifier.height(Dimens.paddingMedium))
                        }

                        // Uploaded Documents Section
                        Text(
                            text = if (isAdvisor) "Client Uploaded Documents (${request.documents.size})" else "Uploaded Verification Documents (${request.documents.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                        if (request.documents.isEmpty()) {
                            Text(
                                text = if (isAdvisor) {
                                    "No documents have been uploaded by the client yet. Once the client uploads documents from their checklist, you will be able to view, download, and review them here."
                                } else {
                                    "No documents uploaded yet. Use the checklist below to upload required land documents for verification."
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            request.documents.forEach { doc ->
                                DocumentItemCard(
                                    doc = doc,
                                    isAdvisor = isAdvisor,
                                    onReviewClick = { showReviewDocDialog = doc },
                                    onOpenClick = {
                                        // Open document in browser / viewer
                                        val fullUrl = if (doc.fileUrl.startsWith("http")) doc.fileUrl else "${Constants.BASE_URL.removeSuffix("/api/v1/")}${doc.fileUrl.removePrefix("/")}"
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl))
                                        context.startActivity(intent)
                                    },
                                    onDownloadClick = {
                                        coroutineScope.launch {
                                            Toast.makeText(context, "Saving ${doc.originalFileName} to phone...", Toast.LENGTH_SHORT).show()
                                            val fullUrl = if (doc.fileUrl.startsWith("http")) {
                                                doc.fileUrl
                                            } else {
                                                val cleanPath = doc.fileUrl.removePrefix("/")
                                                val filename = cleanPath.substringAfterLast("/")
                                                "${Constants.BASE_URL}legal-requests/documents/download/$filename?name=${Uri.encode(doc.originalFileName)}"
                                            }
                                            FileDownloadHelper.downloadAndSaveFile(
                                                context = context,
                                                fileUrl = fullUrl,
                                                fileName = doc.originalFileName.ifBlank { "${doc.title}.pdf" },
                                                mimeType = doc.mimeType
                                            )
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                        // Pre-Purchase Land Document Checklist (Only for Clients / Buyers to upload)
                        if (!isAdvisor) {
                            Text(
                                text = "Required Land Purchase Documents Checklist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Documents recommended to verify before buying land in Andhra Pradesh. Tap Upload to submit each document for legal verification:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                            LandVerificationDocTypes.REQUIRED_DOCS.forEach { docTypeInfo ->
                                val alreadyUploaded = request.documents.any { it.documentType == docTypeInfo.code }
                                DocChecklistItemCard(
                                    info = docTypeInfo,
                                    isUploaded = alreadyUploaded,
                                    isUploading = uiState.isUploading,
                                    onUploadClick = {
                                        targetDocTypeForUpload = docTypeInfo
                                        filePickerLauncher.launch("*/*")
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Spacer(modifier = Modifier.height(Dimens.paddingLarge))
                        }
                    }
                } else {
                    // In-App Chat View
                    if (!isAccepted) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(Dimens.paddingLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.outline
                                )
                                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                                Text(
                                    text = "In-app messaging will unlock once the Legal Advisor accepts this request.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        // Real-time Chat Screen
                        Column(modifier = Modifier.fillMaxSize()) {
                            val listState = rememberLazyListState()

                            LaunchedEffect(uiState.messages.size) {
                                if (uiState.messages.isNotEmpty()) {
                                    listState.animateScrollToItem(uiState.messages.size - 1)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.paddingMedium, vertical = Dimens.paddingSmall),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.messages) { msg ->
                                    val isMe = msg.senderId == uiState.currentUserId
                                    ChatBubble(message = msg, isMe = isMe)
                                }
                            }

                            Divider()

                            // Input field
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.paddingSmall),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = chatInputText,
                                    onValueChange = { chatInputText = it },
                                    placeholder = { Text("Ask legal advisor or client...") },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 3
                                )

                                Spacer(modifier = Modifier.width(Dimens.paddingSmall))

                                IconButton(
                                    onClick = {
                                        if (chatInputText.isNotBlank()) {
                                            val senderName = if (isAdvisor) request.advisorName else request.userName
                                            viewModel.sendMessage(chatInputText.trim(), senderName)
                                            chatInputText = ""
                                        }
                                    },
                                    enabled = chatInputText.isNotBlank() && !uiState.isSendingMessage
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send",
                                        tint = if (chatInputText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Advisor reviews a document
    showReviewDocDialog?.let { doc ->
        ReviewDocumentDialog(
            doc = doc,
            onDismiss = { showReviewDocDialog = null },
            onSubmit = { status, remarks ->
                viewModel.reviewDocument(doc.id, status, remarks)
                showReviewDocDialog = null
            }
        )
    }

    // Dialog: Advisor submits overall verification review
    if (showOverallReviewDialog) {
        OverallReviewDialog(
            currentStatus = request?.status ?: "ACCEPTED",
            currentSummary = request?.verificationSummary ?: "",
            onDismiss = { showOverallReviewDialog = false },
            onSubmit = { status, summary ->
                viewModel.submitOverallReview(status, summary)
                showOverallReviewDialog = false
            }
        )
    }
}

@Composable
fun DocumentItemCard(
    doc: LegalDocumentItem,
    isAdvisor: Boolean,
    onReviewClick: () -> Unit,
    onOpenClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.paddingSmall)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                    Column {
                        Text(
                            text = doc.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${doc.originalFileName} (${doc.fileSize / 1024} KB)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val (badgeColor, badgeBg) = when (doc.verificationStatus) {
                    "VERIFIED" -> Pair(Color(0xFF2E7D32), Color(0xFFE8F5E9))
                    "NEEDS_CHANGES" -> Pair(Color(0xFFE65100), Color(0xFFFFF3E0))
                    "REJECTED" -> Pair(Color(0xFFC62828), Color(0xFFFFEBEE))
                    else -> Pair(Color(0xFFF57C00), Color(0xFFFFF8E1))
                }

                Box(
                    modifier = Modifier
                        .background(badgeBg, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = doc.verificationStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (doc.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Advisor Note: ${doc.remarks}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onOpenClick) {
                    Icon(imageVector = Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View")
                }

                TextButton(onClick = onDownloadClick) {
                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save to Phone")
                }

                if (isAdvisor) {
                    TextButton(onClick = onReviewClick) {
                        Icon(imageVector = Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Review Status")
                    }
                }
            }
        }
    }
}

@Composable
fun DocChecklistItemCard(
    info: DocTypeInfo,
    isUploaded: Boolean,
    isUploading: Boolean,
    onUploadClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (isUploaded) Color(0xFFF1F8E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isUploaded) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Uploaded",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = info.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(Dimens.paddingSmall))

            OutlinedButton(
                onClick = onUploadClick,
                enabled = !isUploading
            ) {
                Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isUploaded) "Re-upload" else "Upload")
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: com.satya.bhoomisetu.domain.model.ChatMessage,
    isMe: Boolean
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.sentAt) { timeFormat.format(Date(message.sentAt)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(
                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = if (isMe) 12.dp else 2.dp,
                        bottomEnd = if (isMe) 2.dp else 12.dp
                    )
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (!isMe) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isMe) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ReviewDocumentDialog(
    doc: LegalDocumentItem,
    onDismiss: () -> Unit,
    onSubmit: (status: String, remarks: String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(doc.verificationStatus) }
    var remarks by remember { mutableStateOf(doc.remarks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Verify: ${doc.title}") },
        text = {
            Column {
                Text("Select Document Status:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedStatus == "VERIFIED",
                        onClick = { selectedStatus = "VERIFIED" },
                        label = { Text("Verified ✓") }
                    )
                    FilterChip(
                        selected = selectedStatus == "NEEDS_CHANGES",
                        onClick = { selectedStatus = "NEEDS_CHANGES" },
                        label = { Text("Needs Changes ⚠") }
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Advisor Remarks / Missing Info") },
                    placeholder = { Text("e.g. Page 2 parent deed missing or blurry...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(selectedStatus, remarks.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun OverallReviewDialog(
    currentStatus: String,
    currentSummary: String,
    onDismiss: () -> Unit,
    onSubmit: (status: String, summary: String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(if (currentStatus == "PENDING") "ACCEPTED" else currentStatus) }
    var summary by remember { mutableStateOf(currentSummary) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Legal Verification Opinion") },
        text = {
            Column {
                Text("Overall Request Status:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = selectedStatus == "VERIFIED",
                        onClick = { selectedStatus = "VERIFIED" },
                        label = { Text("All Clear (Verified)") }
                    )
                    FilterChip(
                        selected = selectedStatus == "NEEDS_CHANGES",
                        onClick = { selectedStatus = "NEEDS_CHANGES" },
                        label = { Text("Changes Required") }
                    )
                }

                Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Legal Opinion & Required Changes *") },
                    placeholder = { Text("State your complete legal opinion, link document verification findings, and steps the client should take...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedStatus, summary.trim()) },
                enabled = summary.isNotBlank()
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
