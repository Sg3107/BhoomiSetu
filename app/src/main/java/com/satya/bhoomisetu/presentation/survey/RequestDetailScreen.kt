package com.satya.bhoomisetu.presentation.survey

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.satya.bhoomisetu.domain.model.SurveyRequest
import com.satya.bhoomisetu.domain.model.SurveyRequestStatus
import com.satya.bhoomisetu.domain.model.UserRole
import com.satya.bhoomisetu.presentation.components.ErrorBanner
import com.satya.bhoomisetu.presentation.components.LoadingIndicator
import com.satya.bhoomisetu.presentation.components.PrimaryButton
import com.satya.bhoomisetu.presentation.theme.Dimens
import com.satya.bhoomisetu.util.Result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    viewModel: RequestDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val requestState by viewModel.request.collectAsState()
    val messagesState by viewModel.messages.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Details") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val reqState = requestState) {
                is Result.Loading -> LoadingIndicator()
                is Result.Error -> ErrorBanner(message = reqState.message, onDismiss = onNavigateBack)
                is Result.Success -> {
                    val request = reqState.data
                    val isSurveyor = currentUser?.role == UserRole.SURVEYOR
                    
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Request Details Section
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimens.paddingMedium),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(Dimens.paddingMedium)) {
                                Text(
                                    text = "Location: ${request.district}, ${request.mandal}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (request.village.isNotBlank()) {
                                    Text("Village: ${request.village}", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (request.surveyNumber.isNotBlank()) {
                                    Text("Survey No: ${request.surveyNumber}", style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                                Text("Description:", fontWeight = FontWeight.Bold)
                                Text(request.description, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                                
                                Divider()
                                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                                
                                Text("Status: ${request.status.name}", fontWeight = FontWeight.Bold)
                                
                                if (isSurveyor || request.status != SurveyRequestStatus.OPEN) {
                                    Text("Requester: ${request.requesterName} (${request.requesterPhone})")
                                }
                                
                                if (request.surveyorName != null) {
                                    Text("Assigned Surveyor: ${request.surveyorName}")
                                }
                            }
                        }

                        if (actionState is Result.Error) {
                            ErrorBanner(
                                message = (actionState as Result.Error).message,
                                onDismiss = { viewModel.resetActionState() }
                            )
                        }

                        // Actions Section
                        if (request.status == SurveyRequestStatus.OPEN && isSurveyor) {
                            Box(modifier = Modifier.padding(Dimens.paddingMedium)) {
                                PrimaryButton(
                                    text = "Accept Request",
                                    onClick = { viewModel.acceptRequest() },
                                    isLoading = actionState is Result.Loading
                                )
                            }
                        } else if (request.status == SurveyRequestStatus.ACCEPTED) {
                            if (request.requesterId == currentUser?.id) {
                                Box(modifier = Modifier.padding(Dimens.paddingMedium)) {
                                    PrimaryButton(
                                        text = "Mark as Completed",
                                        onClick = { viewModel.completeRequest() },
                                        isLoading = actionState is Result.Loading
                                    )
                                }
                            }
                            
                            // Chat Section
                            Divider()
                            Text(
                                text = "Messages",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(Dimens.paddingMedium)
                            )
                            
                            // Messages List
                            Box(modifier = Modifier.weight(1f)) {
                                when (val msgs = messagesState) {
                                    is Result.Loading -> LoadingIndicator()
                                    is Result.Error -> Text("Failed to load messages", modifier = Modifier.padding(Dimens.paddingMedium))
                                    is Result.Success -> {
                                        LazyColumn(
                                            contentPadding = PaddingValues(horizontal = Dimens.paddingMedium),
                                            reverseLayout = true
                                        ) {
                                            items(msgs.data.reversed()) { msg ->
                                                val isMe = msg.senderId == currentUser?.id
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                                ) {
                                                    Surface(
                                                        shape = MaterialTheme.shapes.medium,
                                                        color = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                        modifier = Modifier.padding(vertical = 4.dp).widthIn(max = 250.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(8.dp)) {
                                                            Text(
                                                                text = msg.senderName,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                fontWeight = FontWeight.Bold
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
                                    }
                                }
                            }
                            
                            // Message Input
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Dimens.paddingMedium),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = messageText,
                                    onValueChange = { messageText = it },
                                    placeholder = { Text("Type a message...") },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 3
                                )
                                Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                                IconButton(
                                    onClick = {
                                        if (messageText.isNotBlank()) {
                                            viewModel.sendMessage(messageText)
                                            messageText = ""
                                        }
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.primary, shape = MaterialTheme.shapes.large)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                        }
                    }
                }
                null -> {}
            }
        }
    }
}
