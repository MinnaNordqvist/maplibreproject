package org.example.project

import androidx.compose.animation.animateBounds
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import maplibreproject.composeapp.generated.resources.Res
import maplibreproject.composeapp.generated.resources.bolt
import maplibreproject.composeapp.generated.resources.hourglass
import org.example.project.data.Response
import org.jetbrains.compose.resources.painterResource


fun String.toColour(): Color {
    val hex = this
    val colorLong = when (hex.length) {
        6 -> ("FF$hex").toLong(16) // Add full opacity if missing
        8 -> hex.toLong(16)        // Use provided alpha
        else -> throw IllegalArgumentException("Invalid hex color format")
    }
    return Color(colorLong)
}







@Composable
fun Timetable(
    bus: Response.Bus,
    routeDetails: MutableMap<String, String>
){

       Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0XFFf3f6f4)
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(2.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 0.dp, top = 0.dp, end = 6.dp, bottom = 0.dp)
                        .size(60.dp, 40.dp)
                        //.weight(0.8f)
                        .background(routeDetails[bus.lineref]!!.toColour()),

                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = bus.lineref,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(
                                start = 0.dp,
                                top = 6.dp,
                                end = 0.dp,
                                bottom = 0.dp
                            ),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    text = bus.destinationdisplay,
                    //fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(3.0f)
                        .padding(start = 0.dp, top = 6.dp, end = 0.dp, bottom = 0.dp),
                    style = MaterialTheme.typography.titleMedium.copy(hyphens = Hyphens.Auto),
                    softWrap = true,

                    )
                if (bus.aikaero() > 60) {
                    Image(
                        painter = painterResource(Res.drawable.hourglass),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp, 30.dp)
                            .padding(
                                start = 2.dp,
                                top = 8.dp,
                                end = 0.dp,
                                bottom = 0.dp
                            ),
                        alignment = Alignment.TopEnd
                    )
                }
                if (bus.aikaero() < -60) {
                    Image(
                        painter = painterResource(Res.drawable.bolt),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp, 30.dp)
                            .padding(
                                start = 2.dp,
                                top = 8.dp,
                                end = 0.dp,
                                bottom = 0.dp
                            ),
                        alignment = Alignment.TopEnd
                    )
                    println(bus.aikaero())
                }
                Text(
                    text = bus.getDeparture(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        //.weight(0.8f)
                        .padding(start = 0.dp, top = 6.dp, end = 0.dp, bottom = 0.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                println(bus.destinationdisplay + " aikataulun mukainen " + bus.getAimedDeparture() + " reealiaika " + bus.getDeparture())
            }
        }
    }
   }


