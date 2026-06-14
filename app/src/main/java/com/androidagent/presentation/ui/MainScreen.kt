package com.androidagent.presentation.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidagent.domain.model.AgentState
import com.androidagent.presentation.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onNavigateToLogs: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToModels: () -> Unit
) {
    val agentState by viewModel.agentState.collectAsStateWithLifecycle()
    val taskInput by viewModel.taskInput.collectAsStateWithLifecycle()
    val isModelLoaded by viewModel.isModelLoaded.collectAsStateWithLifecycle()
    val modelName by viewModel.modelName.collectAsStateWithLifecycle()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val currentPlan by viewModel.currentPlan.collectAsStateWithLifecycle()
    val actionHistory by viewModel.actionHistory.collectAsStateWithLifecycle()
    val pendingApproval by viewModel.pendingApproval.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.checkState() }

    if (pendingApproval != null) {
        ApprovalDialog(
            safetyCheck = pendingApproval!!,
            onApprove = { viewModel.approveAction() },
            onDeny = { viewModel.denyAction() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android Agent", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = onNavigateToModels) {
                        Icon(Icons.Default.Memory, contentDescription = "Models")
                    }
                    IconButton(onClick = onNavigateToMemory) {
                        Icon(Icons.Default.Psychology, contentDescription = "Memory")
                    }
                    IconButton(onClick = onNavigateToLogs) {
                        Icon(Icons.Default.Terminal, contentDescription = "Logs")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item { StatusCard(agentState, isModelLoaded, modelName, isAccessibilityEnabled, statusMessage) }

            if (!isAccessibilityEnabled) {
                item { SetupCard(viewModel) }
            }

            item {
                TaskInputCard(
                    taskInput = taskInput,
                    onInputChange = viewModel::updateTaskInput,
                    agentState = agentState,
                    isModelLoaded = isModelLoaded,
                    isAccessibilityEnabled = isAccessibilityEnabled,
                    onStart = viewModel::startAgent,
                    onStop = viewModel::stopAgent,
                    onEmergencyStop = viewModel::emergencyStop
                )
            }

            if (currentPlan != null) {
                item { PlanCard(currentPlan!!) }
            }

            if (actionHistory.isNotEmpty()) {
                item { ActionHistoryCard(actionHistory) }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatusCard(
    state: AgentState,
    isModelLoaded: Boolean,
    modelName: String,
    isAccessibilityEnabled: Boolean,
    statusMessage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            when (state) {
                                AgentState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                                AgentState.COMPLETED -> MaterialTheme.colorScheme.primary
                                AgentState.ERROR -> MaterialTheme.colorScheme.error
                                AgentState.WAITING_APPROVAL -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.secondary
                            }
                        )
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Agent: ${state.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(Modifier.height(8.dp))
            Row {
                StatusChip("Model", if (isModelLoaded) modelName else "None", isModelLoaded)
                Spacer(Modifier.width(8.dp))
                StatusChip("A11y", if (isAccessibilityEnabled) "On" else "Off", isAccessibilityEnabled)
            }
            if (statusMessage.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(statusMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, value: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
    ) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text("$label: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SetupCard(viewModel: MainViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Setup Required", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Enable the Accessibility Service to allow the agent to read and interact with the screen.", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            Row {
                Button(onClick = { viewModel.openAccessibilitySettings() }) {
                    Icon(Icons.Default.Accessibility, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Enable Accessibility")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { viewModel.requestScreenCapture() }) {
                    Icon(Icons.Default.Screenshot, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Capture")
                }
            }
        }
    }
}

@Composable
private fun TaskInputCard(
    taskInput: String,
    onInputChange: (String) -> Unit,
    agentState: AgentState,
    isModelLoaded: Boolean,
    isAccessibilityEnabled: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = taskInput,
                onValueChange = onInputChange,
                label = { Text("What should the agent do?") },
                placeholder = { Text("e.g. Open YouTube and search Flutter tutorials") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = agentState == AgentState.IDLE,
                maxLines = 3
            )
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (agentState == AgentState.IDLE) {
                    Button(
                        onClick = onStart,
                        modifier = Modifier.weight(1f),
                        enabled = taskInput.isNotBlank() && isModelLoaded && isAccessibilityEnabled,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Agent")
                    }
                } else {
                    Button(
                        onClick = onStop,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop")
                    }
                    Button(
                        onClick = onEmergencyStop,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanCard(plan: com.androidagent.domain.model.AgentPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            plan.steps.forEachIndexed { index, step ->
                Row(modifier = Modifier.padding(vertical = 2.dp)) {
                    val icon = when {
                        index < plan.currentStepIndex -> Icons.Default.CheckCircle
                        index == plan.currentStepIndex -> Icons.Default.ArrowForward
                        else -> Icons.Default.Circle
                    }
                    Icon(
                        icon, contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = when {
                            index < plan.currentStepIndex -> MaterialTheme.colorScheme.primary
                            index == plan.currentStepIndex -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(step.description, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ActionHistoryCard(actions: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Action History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            actions.takeLast(10).reversed().forEach { action ->
                Text(
                    "• $action",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
