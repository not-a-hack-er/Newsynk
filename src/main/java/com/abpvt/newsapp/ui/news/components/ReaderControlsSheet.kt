package com.abpvt.newsapp.ui.news.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abpvt.newsapp.data.repository.ReaderPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderControlsSheet(
    palette: ReaderPalette,
    fontScale: Float,
    lineHeight: Float,
    onPaletteChange: (ReaderPalette) -> Unit,
    onFontScaleChange: (Float) -> Unit,
    onLineHeightChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Reading preferences", fontWeight = FontWeight.ExtraBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ReaderPalette.entries.forEach { option ->
                    FilterChip(
                        selected = palette == option,
                        onClick = { onPaletteChange(option) },
                        label = { Text(option.name.lowercase().replaceFirstChar { it.titlecase() }) }
                    )
                }
            }
            Text("Text size")
            Slider(value = fontScale, onValueChange = onFontScaleChange, valueRange = 0.85f..1.35f)
            Text("Line spacing")
            Slider(value = lineHeight, onValueChange = onLineHeightChange, valueRange = 1.2f..1.8f)
        }
    }
}
