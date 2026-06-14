package com.androidagent.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidagent.domain.model.MemoryType
import com.androidagent.presentation.viewmodel.MemoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    viewModel: MemoryViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val memories by viewModel.memories.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedType.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearMemory() }) {
                        Icon(Icons.Default.DeleteSweep, "Clear")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == MemoryType.SHORT_TERM,
                    onClick = { viewModel.selectType(MemoryType.SHORT_TERM) },
                    label = { Text("Short-Term") }
                )
                FilterChip(
                    selected = selectedType == MemoryType.LONG_TERM,
                    onClick = { viewModel.selectType(MemoryType.LONG_TERM) },
                    label = { Text("Long-Term") }
                )
            }
            Spacer(Modifier.height(8.dp))
            if (memories.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No memories", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                val timeFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(memories) { entry ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(entry.key, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                    Text(timeFormat.format(Date(entry.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(entry.value, style = MaterialTheme.typography.bodySmall, maxLines = 4)
                            }
                        }
                    }
                }
            }
        }
    }
}
