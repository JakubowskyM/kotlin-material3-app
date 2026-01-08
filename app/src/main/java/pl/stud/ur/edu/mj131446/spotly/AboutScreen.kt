package pl.stud.ur.edu.mj131446.spotly

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("O aplikacji") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Spotly",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Aplikacja do zarządzania miejscami została stworzona, które chcesz odwiedzić lub które odwiedziłeś.\n" +
                        "Możesz dodawać miejsca, przypisywać zdjęcia, notatki, lokalizację oraz oceny w formie gwiazdek.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Justify
            )
            Text(
                text = "Autor: Milosz\nWersja: 1.0",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}