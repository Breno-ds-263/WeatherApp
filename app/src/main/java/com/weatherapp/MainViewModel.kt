package com.weatherapp

import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.weatherapp.model.City

class MainViewModel : ViewModel() {

    private val _cities = createCities().toMutableStateList()

    val cities
        get() = _cities.toList()

    fun remove(city: City) {
        _cities.remove(city)
    }

    fun add(name: String) {
        _cities.add(City(name = name))
    }

    private fun createCities() = List(20) { i ->
        City(
            name = "Cidade $i",
            weather = "Carregando clima..."
        )
    }

    fun add(name: String, location: LatLng? = null) {
        _cities.add(City(name = name, location = location))

    }
}