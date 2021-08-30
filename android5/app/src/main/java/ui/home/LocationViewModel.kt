package ui.home

import androidx.lifecycle.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import model.Gateway
import repository.BlockaRepository
import repository.LocationRepository
import ui.utils.cause
import utils.Logger
import java.lang.Exception

class LocationViewModel : ViewModel() {

    private val blocka = BlockaRepository

    private val _locations = MutableLiveData<List<Gateway>>()
    val locations: LiveData<List<Gateway>> = _locations.distinctUntilChanged()

    fun refreshLocations() {
        viewModelScope.launch {
            try {
                _locations.value = blocka.fetchGateways()
            } catch (ex: Exception) {
                Logger.w("Location", "Could not load locations".cause(ex))
            }
        }
    }

}