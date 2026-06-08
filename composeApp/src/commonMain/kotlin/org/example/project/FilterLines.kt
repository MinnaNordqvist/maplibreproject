package org.example.project

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun FilterLines(isSelected: Boolean, stopSearch: String?,  label: String, selectedLines: Map<String, String>, busViewModel: BusViewModel){

    FilterChip(
        selected = isSelected,
        onClick = {
            if ( selectedLines[stopSearch!!] == label) {
                busViewModel.removeLine(stopSearch!!)

            } else {
                busViewModel.addLine(stopSearch!!, label)
                println(selectedLines.entries)

            }

        },

        label = { Text(label) },
        elevation = FilterChipDefaults.filterChipElevation(),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            labelColor = Color.Black,
            // selectedContainerColor = Color.Magenta,
        ),
        modifier = Modifier.padding(end = 5.dp)
    )
}