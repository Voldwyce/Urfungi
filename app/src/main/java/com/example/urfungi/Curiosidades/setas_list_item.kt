package com.example.urfungi.Curiosidades

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.urfungi.R
import kotlin.math.min

val setas = listOf(
    Seta(
        "CHAMPIÑONES", R.drawable.champ,
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sit amet eros cursus, viverra nibh in, tincidunt justo. Ut pretium a nibh eu feugiat. Quisque lorem ante, accumsan eu tincidunt eget, imperdiet eget felis. Quisque a orci gravida, pellentesque lorem at, consequat quam. Integer non est est. Vestibulum aliquam nulla et massa viverra, non maximus magna consectetur. Nunc non erat id sem condimentum lacinia. Suspendisse id dictum dui, vel hendrerit dolor. Mauris vitae tortor purus. Nulla sit amet ullamcorper risus.",
        "Agaricus bisporus",
        "Otoño"
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
fun SetasListItem(seta: Seta, onItemClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 30.dp)
            .clickable { onItemClick(); isExpanded = !isExpanded }
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
                    Text(text = seta.nombre, fontWeight = FontWeight.Bold, color = Color.Cyan)
                    if (!isExpanded) {
                        Text(text = seta.descripcion.substring(0, min(14, seta.descripcion.length)))
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Descripción",
                            fontWeight = FontWeight.Bold,
                            color = Color.Cyan
                        )
                        Text(text = seta.descripcion, textAlign = TextAlign.Justify)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nombre cientifico",
                            fontWeight = FontWeight.Bold,
                            color = Color.Cyan
                        )
                        Text(text = seta.nombrecientifico)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Estación", fontWeight = FontWeight.Bold, color = Color.Cyan)
                        Text(text = seta.estacion)
                    }
                }
            }
        }
    }
}



