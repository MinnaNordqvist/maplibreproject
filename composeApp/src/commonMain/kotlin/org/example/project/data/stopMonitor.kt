package org.example.project.data

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration

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
  "servertime": 1781165341,
  "result": [
    {
      "recordedattime": 1781165341,
      "lineref": "10",
      "dataframeref": "2026-06-11",
      "datedvehiclejourneyref": "[@10004.0.358299174@][2][1767603127745]/49",
      "directionname": "2",
      "originref": "1",
      "destinationref": "1704",
      "originaimeddeparturetime": 1781165460,
      "destinationaimedarrivaltime": 1781169060,
      "monitored": true,
      "incongestion": false,
      "longitude": 22.21968,
      "latitude": 60.435,
      "blockref": "1010040200",
      "vehicleref": "550037",
      "visitnumber": 3,
      "vehicleatstop": false,
      "destinationdisplay": "Länsikeskus-Kupittaa-Skanssi-Uittamo",
      "aimedarrivaltime": 1781165556,
      "expectedarrivaltime": 1781165556,
      "aimeddeparturetime": 1781165556,
      "expecteddeparturetime": 1781165556,
      "destinationdisplay_sv": "Västcentrum-Kuppis-Skansen-Uittamo",
      "__tripref": "00018481__1010040200",
      "__routeref": "20",
      "__directionid": "1"
    },
         .....
  ]
 }
*/

@OptIn(ExperimentalTime::class)
@Serializable
data class Response(
    val status: String,
    val result: List<Bus>?,
    val servertime: Long
) {
    @Serializable
    data class Bus(
        val recordedattime: Long,
        val lineref: String,
        val destinationdisplay: String,
        val monitored: Boolean,
        val longitude: Double? = null,
        val latitude: Double? = null,
        val aimeddeparturetime: Long,
        val expecteddeparturetime: Long,
    ) {
        fun getDepartures(epochs: Long): String {
            val instant = Instant.fromEpochSeconds(epochs)
            val departure = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                .format(LocalDateTime.Format {
                    hour()
                    char(':')
                    minute()
                })
            return departure
        }


        fun aikaero() : Long {
            val erotus = expecteddeparturetime - aimeddeparturetime
            return erotus
        }

        fun getTimeStamp(): String {
            val instant = Instant.fromEpochSeconds(recordedattime)
            val timestamp = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                .format(LocalDateTime.Format {
                    hour()
                    char(':')
                    minute()
                    char(':')
                    second()
                })
            return timestamp
        }

        fun compareTimes(): Duration {
            val clock: Clock = Clock.System
            val instantNow = clock.now()
            val instantSiri = Instant.fromEpochSeconds(recordedattime)
            val erotus = instantNow - instantSiri
            return erotus
        }


    }

}

