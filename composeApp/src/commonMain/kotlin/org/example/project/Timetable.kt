package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import maplibreproject.composeapp.generated.resources.Res
import org.maplibre.spatialk.geojson.Position
import maplibreproject.composeapp.generated.resources.bolt
import maplibreproject.composeapp.generated.resources.bus_map_pin
import maplibreproject.composeapp.generated.resources.hourglass
import org.example.project.data.Response
import org.jetbrains.compose.resources.painterResource
import  androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import  androidx.compose.runtime.setValue


private fun String.toColor(): Color {
    val hex = this
    return Color(android.graphics.Color.parseColor("#$hex"))
}

@Composable
fun Timetable(
    bus: Response.Bus,
    routeDetails: MutableMap<String, String>,
    onIconClick: (position:Position?) -> Unit
){
       val departureTime = bus.getDepartures(bus.expecteddeparturetime)
       val difference = bus.aikaero()
       val routeColor =  routeDetails[bus.lineref]!!.toColor()
       val realTime = bus.monitored
       val lon = bus.longitude
       val lat = bus.latitude

       var position: Position? by remember{ mutableStateOf<Position?>(null) }

       Card(
        colors = CardDefaults.cardColors(
            containerColor = (if (realTime) Color(0XFFf3f6f4) else Color(0xFFD7D8D9))
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
                        .padding(end = 6.dp)
                        .size(60.dp, 40.dp)
                        .background(routeColor),

                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = bus.lineref,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = 6.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    text = bus.destinationdisplay,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .weight(3.0f)
                        .padding(top = 6.dp),
                    style = MaterialTheme.typography.titleMedium.copy(hyphens = Hyphens.Auto),
                    softWrap = true
                )

                if (bus.monitored) {
                    OutlinedIconButton(
                        onClick = {
                            position = Position(latitude = lat!!, longitude = lon!!)
                            position?.let { onIconClick(it) }
                            //println(position)
                        },
                        modifier = Modifier.padding(end = 6.dp)
                    ){
                        Icon(
                            painter = painterResource(Res.drawable.bus_map_pin),
                            contentDescription = null,
                            //modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                if (bus.monitored && difference > 60) {
                    Image(
                        painter = painterResource(Res.drawable.hourglass),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp, 30.dp)
                            .padding(start = 2.dp, top = 8.dp),
                        alignment = Alignment.TopEnd
                    )
                   // println(difference)
                }
                if (bus.monitored && difference < -60) {
                    Image(
                        painter = painterResource(Res.drawable.bolt),
                        contentDescription = null,
                        modifier = Modifier.size(25.dp, 30.dp)
                            .padding(start = 2.dp, top = 8.dp),
                        alignment = Alignment.TopEnd
                    )
                   // println(difference)
                }
                Text(
                    text = departureTime,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.titleMedium
                )
                println(bus.destinationdisplay + " " + bus.monitored + " " + bus.longitude +  " " + bus.latitude + " " + " aikataulun mukainen " + bus.getDepartures(bus.aimeddeparturetime) + " reealiaika " + bus.getDepartures(bus.expecteddeparturetime) + " erotus " + bus.aikaero())
            }
        }
    }
   }


