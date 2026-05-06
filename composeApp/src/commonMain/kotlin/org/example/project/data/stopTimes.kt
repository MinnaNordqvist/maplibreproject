package org.example.project.data

import kotlinx.serialization.Serializable

/*
Lähetetään http-get pyyntö osoitteeseen https://data.foli.fi/gtfs/stop_times/stop/stop_id
esimerkki stop_id: 4
Vastaus tulee muodossa:
[
    {
        "trip_id": "00018143__1001010106",
        "arrival_time": "04:02:47",
        "departure_time": "04:02:47",
        "stop_sequence": 3,
        "stop_headsign": "",
        "pickup_type": 0,
        "drop_off_type": 0,
        "shape_dist_traveled": 1192,
        "timepoint": 0
    },
    ...
]
*/

@Serializable
data class StopTimes(
    val trip_id: String,
)