package com.abpvt.newsapp.ui.news.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abpvt.newsapp.data.repository.PersonalizationRepository

@Composable
fun PersonalizationDialog(
    selectedTopics: Set<String>,
    onToggleTopic: (String) -> Unit,
    onComplete: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onSkip,
        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
        title = { Text("Build your For You feed", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Choose the topics you care about. You can change these later in Explore.")
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PersonalizationRepository.AVAILABLE_TOPICS.forEach { topic ->
                        FilterChip(
                            selected = topic in selectedTopics,
                            onClick = { onToggleTopic(topic) },
                            label = { Text(topic) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onComplete, enabled = selectedTopics.isNotEmpty()) {
                Text("Create my feed")
            }
        },
        dismissButton = { TextButton(onClick = onSkip) { Text("Not now") } }
    )
}
