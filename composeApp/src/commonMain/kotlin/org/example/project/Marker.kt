package org.example.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import maplibreproject.composeapp.generated.resources.Res
import maplibreproject.composeapp.generated.resources.bus
import org.jetbrains.compose.resources.painterResource



@Composable
expect fun Marker()

@Composable
fun Markera(
    
){

    val marker = painterResource(Res.drawable.bus)


}

