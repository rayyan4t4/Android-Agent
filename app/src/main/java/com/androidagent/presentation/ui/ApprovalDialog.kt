package com.androidagent.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.androidagent.domain.model.SafetyCheck

@Composable
fun ApprovalDialog(
    safetyCheck: SafetyCheck,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDeny() },
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                "Action Approval Required",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Risk Level: ${safetyCheck.riskLevel.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = when (safetyCheck.riskLevel) {
                        com.androidagent.domain.model.RiskLevel.CRITICAL -> MaterialTheme.colorScheme.error
                        com.androidagent.domain.model.RiskLevel.HIGH -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(Modifier.height(8.dp))
                Text("Action: ${safetyCheck.action.description}", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(4.dp))
                Text("Reason: ${safetyCheck.reason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            Button(
                onClick = onApprove,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Approve")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDeny) {
                Text("Deny")
            }
        }
    )
}
