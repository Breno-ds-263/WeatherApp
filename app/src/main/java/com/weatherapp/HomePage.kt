package com.weatherapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.weatherapp.model.Forecast
import com.weatherapp.model.Weather
import java.text.DecimalFormat

@Composable
fun HomePage(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel
) {
    Column {

        if (viewModel.city == null) {

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(Color.Blue)
                    .wrapContentSize(Alignment.Center)
            ) {

                Text(
                    text = "Selecione uma cidade!",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp
                )
            }

        } else {
            val cities = viewModel.cities.collectAsStateWithLifecycle(emptyMap()).value
            val city = cities[viewModel.city!!]
            val weather = viewModel.weather.collectAsStateWithLifecycle(emptyMap())
                .value[viewModel.city!!]
            val icon = if (city?.isMonitored == true) Icons.Filled.Notifications else
                Icons.Outlined.Notifications
            val forecasts = viewModel.forecast.collectAsStateWithLifecycle(emptyMap())
                .value[viewModel.city!!]

            LaunchedEffect(viewModel.city!!) {
                viewModel.loadForecast(viewModel.city!!)
            }

            Row {

                AsyncImage(
                    model = weather?.imgUrl ?: viewModel.weather(viewModel.city!!).imgUrl,
                    modifier = Modifier.size(140.dp),
                    error = painterResource(id = R.drawable.loading),
                    contentDescription = "Imagem"
                )

                Column {

                    Spacer(modifier = Modifier.size(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = city?.name ?: "Selecione uma cidade...",
                            fontSize = 28.sp
                        )

                        Spacer(modifier = Modifier.size(8.dp))

                        city?.let { c ->

                            Icon(
                                imageVector = icon,
                                contentDescription = "Monitorada?",
                                modifier = Modifier
                                    .size(32.dp)
                                    .clickable {
                                        viewModel.update(
                                            c.copy(
                                                isMonitored = !c.isMonitored
                                            )
                                        )
                                    }
                            )
                        }
                    }

                    viewModel.city?.let { name ->

                        val w = weather ?: viewModel.weather(name)

                        Spacer(modifier = Modifier.size(12.dp))

                        Text(
                            text = w.desc,
                            fontSize = 15.sp
                        )

                        Spacer(modifier = Modifier.size(12.dp))

                        Text(
                            text = "Temp: ${w.temp}℃",
                            fontSize = 15.sp
                        )
                    }
                }
            }

            forecasts?.let { forecastsList ->

                LazyColumn {

                    items(forecastsList) { forecast ->

                        ForecastItem(
                            forecast = forecast,
                            onClick = { }
                        )

                    }

                }

            }

        }

    }
}

@Composable
fun ForecastItem(
    forecast: Forecast,
    modifier: Modifier = Modifier,
    onClick: (Forecast) -> Unit
) {

    val format = DecimalFormat("#.0")
    val tempMin = format.format(forecast.tempMin)
    val tempMax = format.format(forecast.tempMax)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable {
                onClick(forecast)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {

        AsyncImage(
            model = forecast.imgUrl,
            modifier = Modifier.size(70.dp),
            error = painterResource(id = R.drawable.loading),
            contentDescription = "Imagem"
        )

        Spacer(modifier = Modifier.size(16.dp))

        Column {

            Text(
                text = forecast.weather,
                fontSize = 15.sp
            )

            Row {

                Text(
                    text = forecast.date,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = "Min: $tempMin℃",
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = "Max: $tempMax℃",
                    fontSize = 12.sp
                )

            }

        }

    }

}
