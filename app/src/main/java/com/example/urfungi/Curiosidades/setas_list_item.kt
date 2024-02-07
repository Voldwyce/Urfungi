package com.example.urfungi.Curiosidades

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.urfungi.R
import com.example.urfungi.Setas
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.min

@Composable
fun SetasListScreen() {
    // Estado para almacenar la consulta de búsqueda
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    // Estado para almacenar la lista de setas obtenida de Firebase
    var setas by remember { mutableStateOf(emptyList<Setas>()) }

    // Instancia de Firebase Firestore
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        // Obtención de datos de Firebase y actualización del estado
        db.collection("setas")
            .get()
            .addOnSuccessListener { result ->
                setas = result.documents.mapNotNull { document ->
                    document.toObject(Setas::class.java)
                }
            }
    }

    // Filtrado de setas según la consulta de búsqueda
    val filteredSetas = filteredSetas(setas, searchQuery.text)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Curiosidades de las Setas",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 17.dp, bottom = 8.dp)
        )

        // Barra de búsqueda de setas
        SetasSearchBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })

        // Contenido de la lista de setas
        SetasListContent(setas = filteredSetas)
    }
}

@Composable
fun SetasSearchBar(searchQuery: TextFieldValue, onSearchQueryChange: (TextFieldValue) -> Unit) {
    // Barra de búsqueda de setas
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
            onValueChange = { onSearchQueryChange(it) },
            placeholder = { Text(text = "Buscar setas") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun SetasListContent(setas: List<Setas>) {
    // Estado para la posición de desplazamiento de la lista
    val scrollState = rememberScrollState()

    // Lista de setas en un LazyRow
    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .scrollable(scrollState, Orientation.Horizontal)
    ) {
        items(setas.size) { index ->
            SetasListItem(seta = setas[index])
        }
    }
}

@Composable
fun SetasListItem(seta: Setas) {
    // Estado para controlar si el elemento de la lista está expandido
    var isExpanded by remember { mutableStateOf(false) }

    // Elemento de lista de setas
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 30.dp)
            .clickable { isExpanded = !isExpanded } // Hacer clic para expandir/reducir
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Cargar imagen desde Firebase Storage
            val imageUrl = "gs://urfungui.appspot.com/" + seta.Imagen
            val painter = rememberImagePainter(data = imageUrl)
            Image(
                painter = painter,
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

            // Contenido de la tarjeta de setas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Spacer(modifier = Modifier.height(25.dp))
                    Text(text = seta.Nombre, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    if (!isExpanded) {
                        Text(text = seta.Descripcion.substring(0, min(14, seta.Descripcion.length)))
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Descripción",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = seta.Descripcion,
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nombre cientifico",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = seta.NombreCientifico, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Estación", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = seta.Habitat, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// Función para filtrar la lista de setas según la consulta de búsqueda
fun filteredSetas(setas: List<Setas>, query: String): List<Setas> {
    return setas.filter {
        it.Nombre.contains(query, ignoreCase = true) ||
                it.Descripcion.contains(query, ignoreCase = true) ||
                it.NombreCientifico.contains(query, ignoreCase = true) ||
                it.Habitat.contains(query, ignoreCase = true)
    }
}



