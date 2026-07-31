package com.weatherapp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.weatherapp.api.WeatherService
import com.weatherapp.api.toForecast
import com.weatherapp.api.toWeather
import com.weatherapp.repo.Repository
import com.weatherapp.model.City
import com.weatherapp.model.Forecast
import com.weatherapp.model.User
import com.weatherapp.model.Weather
import com.weatherapp.monitor.ForecastMonitor
import com.weatherapp.ui.nav.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repo: Repository,
    private val service: WeatherService,
    private val monitor: ForecastMonitor
) : ViewModel(), Repository.Listener {

    private var _city = mutableStateOf<String?>(null)
    var city: String?
        get() = _city.value
        set(tmp) { _city.value = tmp }

    private var _page = mutableStateOf<Route>(Route.Home)
    var page: Route
        get() = _page.value
        set(tmp) { _page.value = tmp }

    private val _cities: Flow<Map<String, City>> = repo.cities.map { cityList ->
        cityList.associateBy { it.name }
    }
    val cities = _cities.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    private val _weather = MutableStateFlow<Map<String, Weather>>(emptyMap())
    val weather = _weather.asStateFlow()

    private val _forecast = MutableStateFlow<Map<String, List<Forecast>?>>(emptyMap())
    val forecast = _forecast.asStateFlow()

    val user = repo.user.stateIn(viewModelScope, SharingStarted.Lazily, null)

    init {
        repo.setListener(this)
    }

    fun weather(name: String): Weather {
        val w = _weather.value[name]
        if (w == null || w == Weather.ERROR) {
            loadWeather(name)
            return Weather.LOADING
        }
        if (w.bitmap == null && w != Weather.LOADING) {
            loadBitmap(name)
        }
        return w
    }

    fun forecast(name: String): List<Forecast>? {
        val f = _forecast.value[name]
        if (f == null) {
            loadForecast(name)
        }
        return f
    }

    fun remove(city: City) {
        repo.remove(city)
    }

    fun update(city: City) {
        repo.update(city)
    }

    fun addCity(name: String) = viewModelScope.launch(Dispatchers.IO) {
        val location = service.getLocation(name)
        if (location != null) {
            repo.add(City(name = name, location = location))
        }
    }

    fun addCity(location: LatLng) = viewModelScope.launch(Dispatchers.IO) {
        val name = service.getName(location.latitude, location.longitude)
        if (name != null) {
            repo.add(City(name = name, location = location))
        }
    }

    fun loadWeather(name: String) {
        if (_weather.value[name] != null && _weather.value[name] != Weather.ERROR) return
        viewModelScope.launch(Dispatchers.Main) {
            _weather.update { current -> current + (name to Weather.LOADING) }
            runCatching {
                service.getWeather(name)?.toWeather()
            }.onSuccess { weather ->
                val w = weather ?: Weather.ERROR
                _weather.update { curr -> curr + (name to w) }
                if (w != Weather.ERROR) {
                    loadBitmap(name)
                }
            }.onFailure {
                _weather.update { curr -> curr + (name to Weather.ERROR) }
            }
        }
    }

    fun loadForecast(name: String) {
        if (_forecast.value[name] != null) return
        viewModelScope.launch(Dispatchers.Main) {
            runCatching {
                service.getForecast(name)?.toForecast()
            }.onSuccess { forecast ->
                _forecast.update { curr -> curr + (name to (forecast ?: emptyList())) }
            }.onFailure {
                _forecast.update { curr -> curr + (name to emptyList()) }
            }
        }
    }

    fun loadBitmap(name: String) {
        val weather = _weather.value[name]
        if (weather == null || weather == Weather.LOADING || weather == Weather.ERROR ||
            weather.bitmap != null
        ) return
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                service.getBitmap(weather.imgUrl)
            }.onSuccess { bitmap ->
                if (bitmap != null) {
                    _weather.update { curr ->
                        val currentCityWeather = curr[name]
                        if (currentCityWeather != null) {
                            curr + (name to currentCityWeather.copy(bitmap = bitmap))
                        } else curr
                    }
                }
            }
        }
    }

    override fun onUserLoaded(user: User) {
        // O stateIn já cuida de atualizar o Flow 'user'
    }

    override fun onUserSignOut() {
        monitor.cancelAll()
    }

    override fun onCityAdded(city: City) {
        monitor.updateCity(city)
    }

    override fun onCityUpdated(city: City) {
        monitor.updateCity(city)
    }

    override fun onCityRemoved(city: City) {
        monitor.cancelCity(city)
    }
}

class MainViewModelFactory(
    private val repo: Repository,
    private val service: WeatherService,
    private val monitor: ForecastMonitor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repo, service, monitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
