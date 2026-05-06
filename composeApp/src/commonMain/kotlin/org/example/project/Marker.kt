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
import mablibreproject.composeapp.generated.resources.Res
import mablibreproject.composeapp.generated.resources.bus
import org.jetbrains.compose.resources.painterResource

import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.imageResource


@Composable
expect fun Marker()

@Composable
fun Markera(
    
){

    val marker = painterResource(Res.drawable.bus)


}

