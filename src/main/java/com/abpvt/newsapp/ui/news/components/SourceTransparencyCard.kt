package com.abpvt.newsapp.ui.news.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abpvt.newsapp.data.model.Article

@Composable
fun SourceTransparencyCard(article: Article, onOpenOriginal: () -> Unit) {
    val isOpinion = article.sectionName.contains("opinion", true) ||
        article.title.contains("opinion", true) || article.title.contains("analysis", true)
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Source transparency", fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            Text("Publisher: ${article.source.name}", style = MaterialTheme.typography.bodyMedium)
            article.author?.takeIf { it.isNotBlank() }?.let {
                Text("Byline: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                if (isOpinion) "Content type: Opinion or analysis" else "Content type: Reported article",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "Corrections and publisher updates are available on the original article.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onOpenOriginal) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Text("  Open original reporting")
            }
        }
    }
}
