package com.example.urfungi.Restaurantes

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.urfungi.Destino
import com.example.urfungi.Recetas.Recetas
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RestaurantesSetasListScreen(navController: NavController) {
    val restaurantes = remember { mutableStateOf(listOf<Restaurantes>()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    val filteredRestaurantes = filterRestaurantes(restaurantes.value, searchQuery.text)

    val db = FirebaseFirestore.getInstance()
    val ref = db.collection("restaurantes")

    LaunchedEffect(Unit) {
        ref.get().addOnSuccessListener { querySnapshot ->
            val tempRestaurantes = mutableListOf<Restaurantes>()
            for (document in querySnapshot.documents) {
                val restaurantes = document.toObject(Restaurantes::class.java)
                if (restaurantes != null) {
                    tempRestaurantes.add(restaurantes)
                }
            }
            restaurantes.value = tempRestaurantes
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Restaurantes de Setas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 17.dp, bottom = 8.dp)
        )

        RestaurantesSearchBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })

        RestaurantesSetasListContent(restaurantes = filteredRestaurantes, navController)
    }
}

@Composable
fun RestaurantesSearchBar(
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .padding(0.dp, 0.dp, 15.dp)
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )
    ) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text(text = "Buscar restaurantes setas") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun RestaurantesSetasListContent(restaurantes: List<Restaurantes>, navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(restaurantes.size) { index ->
            RestaurantesSetasListItem(restaurantes = restaurantes[index], navController)
        }
    }
}

@Composable
fun RestaurantesSetasListItem(restaurantes: Restaurantes, navController: NavController) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 30.dp)
            .clickable { isExpanded = !isExpanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = restaurantes.Imagen,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .aspectRatio(1f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp
                        )
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Spacer(modifier = Modifier.height(25.dp))
                    Text(
                        text = restaurantes.NombreRestaurante,
                        fontWeight = FontWeight.Bold,
                        color = Color.LightGray
                    )
                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Descripción",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = restaurantes.Descripcion,
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                /*navController.navigate("${Destino.Destino2.ruta}/${restaurantes.longitude}/${restaurantes.latitude}")*/
                                navController.navigate(Destino.Destino2.ruta)
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(text = "Ubicación")
                        }
                    }
                }
            }
        }
    }
}

fun filterRestaurantes(restaurantes: List<Restaurantes>, query: String): List<Restaurantes> {
    return restaurantes.filter {
        it.NombreRestaurante.contains(query, ignoreCase = true)
    }
}

/*fun agregarRestaurantesAleatorios() {
    val db = FirebaseFirestore.getInstance()
    val ref = db.collection("restaurantes")

    val restaurantes = listOf(
        mapOf(
            "NombreRestaurante" to "Le Grand Café Rouge",
            "Descripcion" to "Descripción del Restaurante A",
            "latitude" to 41.4118938, // Latitud de una ubicación aleatoria
            "longitude" to 2.2190616, // Longitud de una ubicación aleatoria
            "Imagen" to "" // URL de la imagen del Restaurante A
        ),
        mapOf(
            "NombreRestaurante" to "Restaurante A",
            "Descripcion" to "Descripción del Restaurante A",
            "latitude" to 40.7128, // Latitud de una ubicación aleatoria
            "longitude" to -74.0060, // Longitud de una ubicación aleatoria
            "Imagen" to "" // URL de la imagen del Restaurante A
        ),
        mapOf(
            "NombreRestaurante" to "Restaurante A",
            "Descripcion" to "Descripción del Restaurante A",
            "latitude" to 40.7128, // Latitud de una ubicación aleatoria
            "longitude" to -74.0060, // Longitud de una ubicación aleatoria
            "Imagen" to "" // URL de la imagen del Restaurante A
        ),
        mapOf(
            "NombreRestaurante" to "Restaurante A",
            "Descripcion" to "Descripción del Restaurante A",
            "latitude" to 40.7128, // Latitud de una ubicación aleatoria
            "longitude" to -74.0060, // Longitud de una ubicación aleatoria
            "Imagen" to "" // URL de la imagen del Restaurante A
        ),
        mapOf(
            "NombreRestaurante" to "Restaurante A",
            "Descripcion" to "Descripción del Restaurante A",
            "latitude" to 40.7128, // Latitud de una ubicación aleatoria
            "longitude" to -74.0060, // Longitud de una ubicación aleatoria
            "Imagen" to "" // URL de la imagen del Restaurante A
        ),
    )

    for (restaurante in restaurantes) {
        ref.add(restaurante)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Restaurante agregado con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al agregar restaurante", e)
            }
    }
}*/
