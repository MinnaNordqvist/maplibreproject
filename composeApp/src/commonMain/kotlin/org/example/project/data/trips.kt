package org.example.project.data

import kotlinx.serialization.Serializable

/*
Lähetetään http get pyyntö osoitteeseen https://data.foli.fi/gtfs/trips/all
Vastaus tulee muodossa
[
  {
     "route_id": "206",
     "service_id": "1206020120",
     "trip_id": "00010000__1206020120",
     "trip_headsign": "Raisio-Kaanaa",
     "direction_id": 1,
     "block_id": "1206020120",
     "shape_id": "205",
     "wheelchair_accessible": 1,
     "bikes_allowed": 0
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

