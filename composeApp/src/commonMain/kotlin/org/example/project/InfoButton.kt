package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import maplibreproject.composeapp.generated.resources.Res

import maplibreproject.composeapp.generated.resources.bolt
import maplibreproject.composeapp.generated.resources.bus_map_pin

import maplibreproject.composeapp.generated.resources.hourglass

import maplibreproject.composeapp.generated.resources.info
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoButton(){
    val tooltipState = rememberTooltipState(isPersistent = true)
    val coroutineScope = rememberCoroutineScope()

    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                TooltipAnchorPosition.Below
            ),
        tooltip = {
            RichTooltip(
                title = { Text("Info") },
                caretShape = null,
            ) {

                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Row() {
                        Image(
                            painter = painterResource(Res.drawable.bolt),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp, 25.dp)


                        )
                        Text(text = "Bussi on ainakin minuutin etuajassa aikataulusta. \n",  textAlign = TextAlign.Start, modifier = Modifier.padding(start = 4.dp))
                    }
                    Row() {
                        Image(
                            painter = painterResource(Res.drawable.hourglass),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp, 25.dp)


                        )
                        Text(text = "Bussi on ainakin minuutin myöhässä aikataulusta. \n",  textAlign = TextAlign.Start, modifier = Modifier.padding(start = 4.dp))
                    }
                    Row(){
                        Image(
                            painter = painterResource(Res.drawable.bus_map_pin),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(25.dp).background(Color.DarkGray).clip(CircleShape),

                        )
                        Text(text = "Näytä bussin sijainti kartalla. \n", textAlign = TextAlign.Start, modifier = Modifier.padding(start = 4.dp) )
                    }
                    Row(){
                        Box(
                            modifier = Modifier.background(Color(0xFFD7D8D9)).size(25.dp, 25.dp)
                        ){}
                        Text(text = "Bussista ei ole tällä hetkellä saatavilla reaaliaikaista dataa. \n", textAlign = TextAlign.Start, modifier = Modifier.padding(start = 4.dp))
                    }
                    Text("Lähde:\nTurun seudun joukkoliikenteen liikennöinti- ja aikatauludata. Aineiston ylläpitäjä on Turun kaupungin joukkoliikennetoimisto. Aineisto on ladattu palvelusta http://data.foli.fi/ lisenssillä Creative Commons Nimeä 4.0 Kansainvälinen (CC BY 4.0).")
                }
            }

        },
        state = tooltipState,

        ) {
        IconButton(
            modifier = Modifier.padding(
                start = 0.dp,
                top = 0.dp,
                end = 0.dp,
                bottom = 0.dp
            ),
            onClick = {
                coroutineScope.launch {
                    tooltipState.show()
                }
            }
        ) {
            Image(
                painter = painterResource(Res.drawable.info),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}


