package pl.stud.ur.edu.mj131446.spotly

import androidx.compose.foundation.text.KeyboardOptions
import android.Manifest
import android.content.Context
import android.location.Location
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlaceScreen(
    repo: PlaceRepository,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var contactError by remember { mutableStateOf(false) }
    var imageBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var lat by remember { mutableStateOf<Double?>(null) }
    var lon by remember { mutableStateOf<Double?>(null) }
    var rating by remember { mutableStateOf(0) }
    var locationLoading by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(ctx) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                getCurrentLocation(fusedLocationClient, ctx) { latitude, longitude ->
                    lat = latitude
                    lon = longitude
                    locationLoading = false
                }
            } else {
                locationLoading = false
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bmp -> imageBitmap = bmp }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> if (granted) cameraLauncher.launch(null) }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dodaj miejsce") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Anuluj")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nazwa miejsca") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Notatka") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = contact,
                onValueChange = {
                    if (it.all { c -> c.isDigit() } && it.length <= 9) {
                        contact = it
                        contactError = false
                    }
                },
                label = { Text("Kontakt (9-cyfrowy numer)") },
                isError = contactError,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            if (contactError) {
                Text("Numer telefonu musi mieć dokładnie 9 cyfr",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = {
                        locationLoading = true
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    },
                    enabled = !locationLoading
                ) {
                    if (locationLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Pobierz lokalizację")
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    lat?.let { latitude ->
                        Text(text = "Szer: %.5f°".format(latitude), style = MaterialTheme.typography.bodySmall)
                    }
                    lon?.let { longitude ->
                        Text(text = "Dł: %.5f°".format(longitude), style = MaterialTheme.typography.bodySmall)
                    }
                    if (lat == null && lon == null && !locationLoading) {
                        Text("Brak lokalizacji", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text("Zrób zdjęcie")
            }

            imageBitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "Zdjęcie miejsca",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ocena miejsca", style = MaterialTheme.typography.bodyMedium)
                Row {
                    repeat(5) { index ->
                        IconButton(onClick = { rating = index + 1 }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "${index + 1} gwiazdek",
                                tint = if (index < rating) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (contact.isNotBlank() && contact.length != 9) {
                        contactError = true
                        return@Button
                    }

                    val filename = imageBitmap?.let { bmp ->
                        val fname = "place_${UUID.randomUUID()}.png"
                        repo.saveBitmapToInternalStorage(bmp, fname)
                    }

                    val place = Place(
                        title = if (title.isBlank()) "Bez nazwy" else title,
                        note = note.ifBlank { null },
                        contact = contact.ifBlank { null },
                        lat = lat,
                        lon = lon,
                        imageFilename = filename,
                        rating = rating
                    )
                    repo.addPlace(place)
                    vibrateFeedback(ctx)
                    onSaved()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Zapisz miejsce")
            }
        }
    }
}

@Suppress("MissingPermission")
private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationReceived: (Double, Double) -> Unit
) {
    try {
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationReceived(it.latitude, it.longitude)
            }
        }.addOnFailureListener { exception ->
            // Opcjonalnie: można dodać obsługę błędów
            exception.printStackTrace()
        }
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}

private fun vibrateFeedback(context: Context) {
    val v = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    v?.let {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            it.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            it.vibrate(200)
        }
    }
}