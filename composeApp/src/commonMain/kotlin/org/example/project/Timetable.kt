package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import org.maplibre.compose.camera.CameraState


fun String.toColor(): Color {
    val hex = this.toLong(16)

    return Color(hex or 0xFF000000)
}

private var routeCol: Color by mutableStateOf(Color(0XFFf3f6f4))
private var lineNum by mutableStateOf("")
private var timeStamp by mutableStateOf("")

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
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
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
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Text(
                        text = bus.destinationdisplay,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1.0f).padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleMedium.copy(hyphens = Hyphens.Auto),
                        softWrap = true,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,

                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (bus.latitude != null && bus.longitude != null) {
                                OutlinedIconButton(
                                    onClick = {
                                        position =
                                            Position(latitude = bus.latitude, longitude = bus.longitude)
                                        position?.let { onIconClick(it) }
                                        timeStamp = bus.getTimeStamp()
                                        lineNum = bus.lineref
                                        routeCol = routeColor

                                    },
                                    modifier = Modifier.size(36.dp),
                                    colors = IconButtonColors(
                                        containerColor = Color.DarkGray,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.White,
                                        disabledContainerColor = Color.Gray
                                    )
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.bus_map_pin),
                                        contentDescription = "Bus location",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                        }
                        Box(
                            modifier = Modifier.padding(start = 4.dp).size(25.dp, 25.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (bus.monitored && difference > 60) {
                                Image(
                                    painter = painterResource(Res.drawable.hourglass),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )

                            }
                            if (bus.monitored && difference < -60) {
                                Image(
                                    painter = painterResource(Res.drawable.bolt),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )

                            }

                        }
                        Text(
                            text = departureTime,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.width(46.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                        println(bus.destinationdisplay + " " + bus.monitored + " " + bus.longitude + " " + bus.latitude + " " + " aikataulun mukainen " + bus.getDepartures(bus.aimeddeparturetime) + " reealiaika " + bus.getDepartures(bus.expecteddeparturetime) + " erotus " + bus.aikaero())

                    }


                }
            }
        }
    }


@Composable
fun BusLocationInfo(
    position: Position?,
    cameraState: CameraState,
    onClick: () -> Unit
){
    val dpTarg = remember(position, cameraState.position) {
        cameraState.projection?.screenLocationFromPosition(position!!)
    }
    val off = with(LocalDensity.current) { Offset(dpTarg?.x?.toPx() ?: 0f, dpTarg?.y?.toPx() ?: 0f) }
    Card(
        onClick = { onClick() },
        modifier = Modifier.absoluteOffset {
                IntOffset(
                    x = off.x.toInt() - 12,
                    y = off.y.toInt() - 24
                )
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = routeCol)

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.bus_map_pin),
                    contentDescription = "Bus location icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = lineNum,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = timeStamp,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

}
