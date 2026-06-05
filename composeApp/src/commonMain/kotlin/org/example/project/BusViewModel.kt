package org.example.project

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.example.project.data.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.example.project.data.SiriApi
import kotlin.collections.plus
import kotlin.time.Duration.Companion.minutes

data class BusListUIState(
    val busList:  List<Response.Bus> = emptyList(),
)

class BusViewModel : ViewModel(), KoinComponent {
    private val siriApi: SiriApi by inject()
    private val _uiState: MutableStateFlow<BusListUIState> = MutableStateFlow(BusListUIState())
    val uiState: StateFlow<BusListUIState> = _uiState
    private val _linesList = MutableStateFlow<List<String>>(emptyList())
    val linesList:  StateFlow<List<String>> = _linesList

    private var refreshJob: Job? = null

    fun getBusList(stop_code: String?) {
        refreshJob?.cancel()
        val lineList = mutableListOf<String>()
        viewModelScope.launch() {
            siriApi.getBusList(stop_code)?.let { busList ->
                _uiState.value = BusListUIState(busList)

            }
            _uiState.value.busList.forEach{ bus ->  lineList.add(bus.lineref)}
            _linesList.value = lineList.distinct()
            startAutoRefreshTimer(stop_code)

        }
    }

    suspend fun getResponseStatus(stop_code: String?) : HttpStatusCode {
        return siriApi.getResponseStatus(stop_code)

    }

    private fun startAutoRefreshTimer(stop_code: String?) {
        // Cancel previous timer just to be safe
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            // Wait for exactly 1 minute
            delay(1.minutes)

            // Loop back to fetch fresh data
            getBusList(stop_code)
            println("Refreshing")
        }
    }

}
/*
val _selectedLines = remember { MutableStateFlow(listOf<String>()) }
val selectedLines by remember { _selectedLines }.collectAsState()

fun addLine(line: String) {
    val newList = ArrayList(selectedLines)
    newList.add(line)
    _selectedLines.value = newList
}

 */

class StateFlowViewModel : ViewModel() {
    private val _selectedLines = MutableStateFlow<Map<String, String>>(emptyMap())
    var selectedLines : StateFlow<Map<String, String>> = _selectedLines.asStateFlow()

    fun addLine(stopSearch: String, label: String){
        _selectedLines.update { oldmap ->
            oldmap + (stopSearch to label)
        }
    }

    fun removeLine(stopSearch: String){
        _selectedLines.update{ oldmap ->
            oldmap - stopSearch
        }
    }


}