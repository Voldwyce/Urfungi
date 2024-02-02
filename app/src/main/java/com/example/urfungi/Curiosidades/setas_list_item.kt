package com.example.urfungi.Curiosidades

import androidx.compose.foundation.Image
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
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import com.example.urfungi.R
import kotlin.math.min

val setas = listOf(
    Seta(
        "TEEMO", R.drawable.teemo,
        "Teemo no se inmuta ante los obstáculos más peligrosos y amenazadores mientras explora el mundo con un entusiasmo infinito y un espíritu lleno de alegría. Es un yordle con una moralidad inquebrantable que se enorgullece de seguir el código de los exploradores de Bandle, a veces con tanto ímpetu que no se percata de las consecuencias de sus acciones. Aunque algunos dicen que la existencia de los exploradores es cuestionable, algo es seguro: no hay que meterse con la convicción de Teemo.",
        "El chikito",
        "Grieta del Invocador"
    ),
    Seta(
        "NISCALO ", R.drawable.niscalo,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
    ),

    )

@Composable
fun SetasListScreen() {
    var searchQuery by remember { mutableStateOf(TextFieldValue()) }
    val filteredSetas = filterSetas(setas, searchQuery.text)

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
fun SetasListContent(setas: List<Seta>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(setas.size) { index ->
            SetasListItem(seta = setas[index])
        }
    }
}

@Composable
fun SetasListItem(seta: Seta) {
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
            Image(
                painter = painterResource(id = seta.imagenResId),
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
                    Text(text = seta.nombre, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    if (!isExpanded) {
                        Text(text = seta.descripcion.substring(0, min(14, seta.descripcion.length)))
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Descripción",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = seta.descripcion,
                            textAlign = TextAlign.Justify,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nombre cientifico",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(text = seta.nombrecientifico, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Estación", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(text = seta.estacion, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

fun filterSetas(setas: List<Seta>, query: String): List<Seta> {
    return setas.filter {
        it.nombre.contains(query, ignoreCase = true) ||
                it.descripcion.contains(query, ignoreCase = true) ||
                it.nombrecientifico.contains(query, ignoreCase = true) ||
                it.estacion.contains(query, ignoreCase = true)
    }
}


