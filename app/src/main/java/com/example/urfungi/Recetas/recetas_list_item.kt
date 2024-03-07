package com.example.urfungi.Recetas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.urfungi.Curiosidades.Seta
import com.example.urfungi.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.min


@Composable
fun RecetasSetasListScreen() {
    val recetas = remember { mutableStateOf(listOf<Recetas>()) }
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    val filteredRecetas = filterSetasRecetas(recetas.value, searchQuery.text)

    val db = FirebaseFirestore.getInstance()
    val ref = db.collection("recetas")

    LaunchedEffect(Unit) {
        ref.get().addOnSuccessListener { querySnapshot ->
            val tempRecetas = mutableListOf<Recetas>()
            for (document in querySnapshot.documents) {
                val receta = document.toObject(Recetas::class.java)
                if (receta != null) {
                    tempRecetas.add(receta)
                }
            }
            recetas.value = tempRecetas
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Recetas de Setas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 17.dp, bottom = 8.dp)
        )

        SetasSearchBar(searchQuery = searchQuery, onSearchQueryChange = { searchQuery = it })

        RecetasSetasListContent(recetas = filteredRecetas)
    }
}

@Composable
fun SetasSearchBar(searchQuery: TextFieldValue, onSearchQueryChange: (TextFieldValue) -> Unit) {
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
            placeholder = { Text(text = "Buscar recetas setas") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
    }
}

@Composable
fun RecetasSetasListContent(recetas: List<Recetas>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(recetas.size) { index ->
            RecetasSetasListItem(receta = recetas[index])
        }
    }
}

@Composable
fun RecetasSetasListItem(receta: Recetas) {
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
                model = receta.Imagen,
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
                    Spacer(modifier = Modifier.height(50.dp))
                    Text(
                        text = receta.NombreReceta,
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
                            text = receta.Descripcion,
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Ingredientes",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = receta.Ingredientes, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Preparacion",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = receta.Preparacion, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tiempo preparacion",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = receta.TiempoPreparacion, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

fun filterSetasRecetas(recetas: List<Recetas>, query: String): List<Recetas> {
    return recetas.filter {
        it.NombreReceta.contains(query, ignoreCase = true) /*||
                it.Descripcion.contains(query, ignoreCase = true) ||
                it.Ingredientes.contains(query, ignoreCase = true) ||
                it.Preparacion.contains(query, ignoreCase = true) */
    }
}
