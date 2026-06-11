package org.example.project.data

import kotlinx.serialization.Serializable

/*
Lähetetään http get pyyntö osoitteeseen https://data.foli.fi/gtfs/trips/all
Vastaus tulee muodossa
[
    {
        "route_id": "28",
        "service_id": "1052010204",
        "trip_id": "00010000__1052010204",
        "trip_headsign": "Puutori",
        "direction_id": 0,
        "block_id": "1052010204",
        "shape_id": "109",
        "wheelchair_accessible": 2
    },
         ...
 ]
 */

@Serializable
data class Trips(
    val route_id: String,
    val service_id: String,
    val trip_id: String,
    val trip_headsign: String,
    val direction_id: Int
)

