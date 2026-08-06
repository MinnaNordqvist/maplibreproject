package org.example.project

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun FilterLines(
    isSelected: Boolean,
    stopSearch: String?,
    label: String,
    selectedLines: Map<String, String>,
    busViewModel: BusViewModel
){

    FilterChip(
        selected = isSelected,
        onClick = {
            stopSearch?.let { search ->
                if (selectedLines[search] == label) {
                    busViewModel.removeLine(search)
                } else {
                    busViewModel.addLine(search, label)
                }
            }
        },
        label = { Text(text = label, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },

        elevation = FilterChipDefaults.filterChipElevation(elevation = 2.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color(0xFFF5F5F5),
            labelColor = Color.DarkGray,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        modifier = Modifier.width(68.dp)
    )
}