package com.goatinsurance.app.ui.assistant

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.goatinsurance.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    onBack: () -> Unit = {},
) {
    var userMessage by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    "Hello! I'm your Goat Insurance AI Assistant. How can I help you today?",
                    isUser = false
                ),
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Filled.SmartToy, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("AI Assistant", style = MaterialTheme.typography.titleMedium)
                            Text("Online", style = MaterialTheme.typography.bodySmall, color = StatusActive)
                        }
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Quick Actions
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "Vaccination Guide" to Icons.Filled.Vaccines,
                    "Disease Diagnosis" to Icons.Filled.MedicalServices,
                    "Claim Help" to Icons.Filled.Help,
                    "FAQs" to Icons.Filled.QuestionAnswer,
                ).forEach { (label, icon) ->
                    AssistChip(
                        onClick = {
                            messages = messages + ChatMessage(label, isUser = true)
                            messages = messages + ChatMessage(
                                "Here's information about $label. This is a comprehensive guide based on our database...",
                                isUser = false
                            )
                        },
                        label = { Text(label) },
                        leadingIcon = { Icon(icon, null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }

            HorizontalDivider()

            // Chat Messages
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true,
            ) {
                items(messages.reversed()) { message ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
                    ) {
                        if (!message.isUser) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.SmartToy, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Card(
                            modifier = Modifier.widthIn(max = 280.dp),
                            shape = RoundedCornerShape(
                                topStart = if (message.isUser) 16.dp else 4.dp,
                                topEnd = if (message.isUser) 4.dp else 16.dp,
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp,
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (message.isUser)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                        ) {
                            Text(
                                text = message.text,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (message.isUser)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // Input
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 8.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = userMessage,
                        onValueChange = { userMessage = it },
                        placeholder = { Text("Type your message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (userMessage.isNotBlank()) {
                                messages = messages + ChatMessage(userMessage, isUser = true)
                                messages = messages + ChatMessage(
                                    "I understand your question about \"$userMessage\". Let me help you with that...",
                                    isUser = false
                                )
                                userMessage = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                    ) {
                        Icon(Icons.Filled.Send, "Send", modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

private data class ChatMessage(val text: String, val isUser: Boolean)
