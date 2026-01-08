package pl.stud.ur.edu.mj131446.spotly


import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.Locale

fun getAddressFromLocation(context: Context, lat: Double, lon: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(lat, lon, 1)
        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val street = address.thoroughfare ?: ""
            val number = address.subThoroughfare ?: ""
            val city = address.locality ?: ""
            listOf(street, number, city).filter { it.isNotEmpty() }.joinToString(", ")
        } else {
            "Brak informacji"
        }
    } catch (e: Exception) {
        "Brak informacji"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    place: Place,
    repo: PlaceRepository,
    onBack: () -> Unit,
    context: Context
) {
    var address by remember { mutableStateOf("Brak informacji") }

    LaunchedEffect(place.lat, place.lon) {
        if (place.lat != null && place.lon != null) {
            address = getAddressFromLocation(context, place.lat, place.lon)
        }
    }

    val localContext = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(place.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Wróć")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            place.imageFilename?.let { file ->
                val bmp = BitmapFactory.decodeFile(file)
                if (bmp != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp),
                        shape = MaterialTheme.shapes.medium,
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Image(bitmap = bmp.asImageBitmap(), contentDescription = "Zdjęcie miejsca", modifier = Modifier.fillMaxSize())
                    }
                }
            }

            InfoCard(title = "Notatka", content = place.note ?: "Brak informacji")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Kontakt", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(place.contact ?: "Brak informacji", style = MaterialTheme.typography.bodyMedium)
                        place.contact?.takeIf { it.isNotBlank() }?.let { number ->
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$number")
                                }
                                localContext.startActivity(intent)
                            }) {
                                Icon(Icons.Filled.Call, contentDescription = "Zadzwoń")
                            }
                        }
                    }
                }
            }

            // Lokalizacja
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Lokalizacja", style = MaterialTheme.typography.titleMedium)
                    Text(address, style = MaterialTheme.typography.bodyMedium)

                    if (place.lat != null && place.lon != null) {
                        Button(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=${place.lat},${place.lon}&mode=d")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                try {
                                    localContext.startActivity(mapIntent)
                                } catch (e: Exception) {
                                    val geoIntentUri = Uri.parse("geo:${place.lat},${place.lon}?q=${place.lat},${place.lon}(${place.title})")
                                    val fallbackIntent = Intent(Intent.ACTION_VIEW, geoIntentUri)
                                    try {
                                        localContext.startActivity(fallbackIntent)
                                    } catch (e2: Exception) {
                                        Toast.makeText(localContext, "Nie można otworzyć aplikacji map", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Pokaż trasę")
                        }
                    }
                }
            }

            // Ocena
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ocena: ", style = MaterialTheme.typography.bodyMedium)
                repeat(5) { index ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (index < place.rating) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Przyciski akcji
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        repo.deletePlace(place.id)
                        Toast.makeText(localContext, "Miejsce usunięte", Toast.LENGTH_SHORT).show()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Usuń", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(content, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
