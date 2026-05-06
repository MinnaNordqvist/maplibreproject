package org.example.project.data

import kotlinx.serialization.Serializable

/*
Lähetetään http get pyyntö osoitteeseen https://data.foli.fi/gtfs/routes
vastaus tulee muodossa
[
  {
        "route_id": "1",
        "agency_id": "1",
        "route_short_name": "1",
        "route_long_name": "Lentoasema-Keskusta-Satama",
        "route_desc": "",
        "route_type": 3,
        "route_url": "",
        "route_color": "0bbbef",
        "route_text_color": "000000"
    },
    ...
 ]
 */

@Serializable
data class Routes(
    val route_id: String,
    val route_short_name: String,
    val route_long_name: String,
    val route_color: String,
    val route_text_color: String,
)