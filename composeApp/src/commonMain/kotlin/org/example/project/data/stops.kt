package org.example.project.data

import kotlinx.serialization.Serializable

/*
Haetaan pysäkit osoitteesta https://data.foli.fi/gtfs/stops
Vastaus tulee muodossa:
{
  "1": {
        "stop_code": "1",
        "stop_name": "Turun satama (Silja)",
        "stop_desc": "",
        "stop_lat": 60.43496999999999985675458447076380252838134765625,
        "stop_lon": 22.219660000000001076614353223703801631927490234375,
        "zone_id": "F\u00d6LI",
        "stop_url": "",
        "location_type": 0,
        "parent_station": 0,
        "stop_timezone": "Europe\/Helsinki",
        "wheelchair_boarding": 0
    },
    ....
 }
 */

@Serializable
data class Stop(
    val stop_code: String,
    val stop_name: String,
    val stop_lat: Double,
    val stop_lon: Double,
)