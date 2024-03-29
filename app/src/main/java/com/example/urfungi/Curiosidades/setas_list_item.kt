package com.example.urfungi.Curiosidades

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.urfungi.Repo.Setas
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun SetasListScreen() {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    var setas by remember { mutableStateOf(emptyList<Setas>()) }

    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(Unit) {
        db.collection("setas")
            .get()
            .addOnSuccessListener { result ->
                setas = result.documents.mapNotNull { document ->
                    document.toObject(Setas::class.java)
                }.sortedByDescending { it.Toxicidad } // Ordenar las setas por toxicidad
            }
    }

    val filteredSetas = filteredSetas(setas, searchQuery.text)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Curiosidades de las Setas",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 17.dp, bottom = 8.dp)
        )

        SetasSearchBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })

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
    val groupedSetas = setas.groupBy { it.Toxicidad } // Agrupar las setas por su toxicidad

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        groupedSetas.forEach { (toxicidad, setasGroup) ->
            item {
                Text(
                    text = "Toxicidad: $toxicidad", // Mostrar el nivel de toxicidad como título
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(13.dp)
                )
            }
            item {
                LazyRow {
                    items(setasGroup) { seta ->
                        SetasListItem(seta = seta)
                    }
                }
            }
        }
    }
}


@Composable
fun SetasListItem(seta: Setas) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp) // Ajuste el margen del card
            .width(250.dp) // Aumentar la altura del Card
            .clickable { showDialog = true }, // Mostrar el diálogo al hacer clic
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = seta.Imagen,
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

            // Contenido restante del Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Spacer(modifier = Modifier.height(15.dp))
                    Text(text = seta.Nombre, fontWeight = FontWeight.Bold, color = Color.LightGray)
                }
            }
        }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = seta.Nombre) },
            text = {
                Column {
                    Spacer(modifier = Modifier.height(32.dp))
                    AsyncImage(
                        model = seta.Imagen,
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

                    Spacer(modifier = Modifier.height(50.dp)) // Agregar espacio entre la imagen y el texto

                    Text(text = seta.Descripcion)

                    Spacer(modifier = Modifier.height(20.dp)) // Agregar espacio entre la imagen y el texto

                    Text(
                        text = "Nombre cientifico: ",
                        fontWeight = FontWeight.Bold
                    ) // Nombre científico en negrita
                    Text(text = seta.NombreCientifico)

                    Spacer(modifier = Modifier.height(20.dp)) // Agregar espacio entre la imagen y el texto

                    Text(text = "Estación: ", fontWeight = FontWeight.Bold) // Hábitat en negrita
                    Text(text = seta.Habitat)

                    Spacer(modifier = Modifier.height(20.dp)) // Agregar espacio entre la imagen y el texto

                    Text(text = "Toxicidad: ", fontWeight = FontWeight.Bold) // Toxicidad en negrita
                    Text(text = seta.Toxicidad.toString())

                    Spacer(modifier = Modifier.height(20.dp)) // Agregar espacio entre la imagen y el texto

                    Text(text = "Dificultad: ", fontWeight = FontWeight.Bold) // Dificultad en negrita
                    Text(text = seta.Dificultad.toString())

                    Spacer(modifier = Modifier.height(20.dp)) // Agregar espacio entre la imagen y el texto

                    Text(text = "Temporada: ", fontWeight = FontWeight.Bold) // Temporada en negrita
                    Text(text = seta.Temporada)

                    Spacer(modifier = Modifier.height(20.dp)) // Agregar espacio entre la imagen y el texto

                    Text(text = "Familia: ", fontWeight = FontWeight.Bold) // Familia en negrita
                    Text(text = seta.Familia)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cerrar")
                }
            }
        )
    }
}


// Función para filtrar la lista de setas según la consulta de búsqueda
fun filteredSetas(setas: List<Setas>, query: String): List<Setas> {
    return setas.filter {
        it.Nombre.contains(query, ignoreCase = true) ||
                it.Descripcion.contains(query, ignoreCase = true) ||
                it.NombreCientifico.contains(query, ignoreCase = true) ||
                it.Habitat.contains(query, ignoreCase = true) ||
                it.Toxicidad.contains(query, ignoreCase = true)
    }
}



