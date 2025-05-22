package com.example.weatherforecast.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.weatherforecast.data.Weather
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun ForecastDayCard(
    date: String,
    weather: Weather?,
    temp_min: Double?,
    temp_max: Double?
) {
    val formattedDate = try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
        outputFormat.format(inputFormat.parse(date)) ?: date
    } catch (e: Exception) {
        date
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    weather?.icon?.let { iconCode ->
                        Image(
                            painter = rememberAsyncImagePainter(
                                "https://openweathermap.org/img/wn/$iconCode@2x.png"
                            ),
                            contentDescription = "Weather icon",
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Text(
                        text = weather?.description?.replaceFirstChar { it.uppercase() } ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "min temp: ${temp_min?.roundToInt()}°",
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = 24.sp
                    )
                    Text(
                        text = "max temp: ${temp_max?.roundToInt()}°",
                        style = MaterialTheme.typography.displaySmall,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
