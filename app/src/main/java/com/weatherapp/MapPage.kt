package com.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.weatherapp.MainViewModel

@Composable
fun MapPage(modifier: Modifier = Modifier,
            viewModel: MainViewModel) {
    val recife = remember { MarkerState(LatLng(-8.05, -34.9)) }
    val caruaru = remember { MarkerState( LatLng(-8.27, -35.98)) }
    val joaopessoa = remember { MarkerState(LatLng(-7.12, -34.84)) }
    val camPosState = rememberCameraPositionState ()


    GoogleMap( modifier = modifier.fillMaxSize(), onMapClick = {
        viewModel.add("Cidade@${it.latitude}:${it.longitude}", location = it) },
        cameraPositionState = camPosState
    ) {
        viewModel.cities.forEach {
            if (it.location != null) {
                Marker( state = MarkerState(position = it.location),
                    title = it.name, snippet = "${it.location}")
            }
        }


    }
}