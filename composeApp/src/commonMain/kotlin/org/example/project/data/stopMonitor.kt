package org.example.project.data

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


/*
 Linja-autokohtainen vastaus osoitteesta https://data.foli.fi/siri/sm/stop_code
 lineref: Linjan numero (esim "3"),
 monitored: onko linjasta saatavilla reaaliaikaista dataa (true jos on),
 destinationdisplay: Linjan tunnus (esim "Perno"),
 aimedarrivaltime: aikataulun mukainen saapumisaika (esim 1760615040),
 expectedarrivaltime: reaaliajan perusteella laskettu saapumisaika (esim 1760615070).

 Vastaus tulee muodossa
 {
  "sys": "SM",
  "status": "OK",
  "servertime": "2025.09.26 09:01:56",
  "result": [
   {
      "recordedattime": 1760697682,
      "lineref": "10",
      "dataframeref": "2025-10-17",
      "datedvehiclejourneyref": "[@10004.0.345075124@][2][1755149395917]/13",
      "directionname": "2",
      "originref": "1",
      "destinationref": "1704",
      "originaimeddeparturetime": 1760698500,
      "destinationaimedarrivaltime": 1760702430,
      "monitored": true,
      "incongestion": false,
      "longitude": 22.22282,
      "latitude": 60.43398,
      "blockref": "1010101100",
      "vehicleref": "550021",
      "visitnumber": 1,
      "vehicleatstop": false,
      "destinationdisplay": "Länsikeskus-Kupittaa-Uittamo",
      "aimedarrivaltime": 1760698500,
      "expectedarrivaltime": 1760697200,
      "aimeddeparturetime": 1760698500,
      "expecteddeparturetime": 1760698500,
      "destinationdisplay_sv": "Västcentrum-Kuppis-Uittamo",
      "__tripref": "00021067__1010101100",
      "__routeref": "18",
      "__directionid": "1"
   },
         .....
  ]
 }
*/

@OptIn(ExperimentalTime::class)
@Serializable
data class Response(
    val result: List<Bus>?
) {
    @Serializable
    data class Bus(
        var lineref: String,
        val monitored: Boolean,
        val destinationdisplay: String,
        val aimedarrivaltime: Long,
        val expectedarrivaltime: Long,
        val aimeddeparturetime: Long,
        val expecteddeparturetime: Long,
        @SerialName("__tripref")
        val trip_id: String? = null,
        @SerialName("__routeref")
        val route_id: String? = null,
        @SerialName("__directionid")
        val direction_id: String? = null,
    ) {

    }
}

interface SiriApi {
    suspend fun getBusList(stop_code: String?): List<Response.Bus>?
    suspend fun getResponseStatus(stop_code: String?): HttpStatusCode
}