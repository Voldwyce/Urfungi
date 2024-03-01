package com.example.urfungi.Repo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun Repositorio(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { navController.navigate("recetas_list_item") },
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp, 60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
            ),
        ) {
            Text(
                "Recetas",
                fontSize = 20.sp
            )
        }
        Button(
            onClick = { navController.navigate("setas_list_item") },
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp, 60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
            ),
        ) {
            Text(
                "Setas",
                fontSize = 20.sp
            )
        }
        Button(
            onClick = { navController.navigate("restaurantes_list_item") },
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp, 60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
            ),
        ) {
            Text(
                "Restaurantes",
                fontSize = 20.sp
            )
        }
        Button(
            onClick = { navController.navigate("weather_screen") },
            modifier = Modifier
                .padding(16.dp)
                .size(200.dp, 60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
            ),
        ) {
            Text(
                "Clima",
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { navController.navigate("creditos") },
            modifier = Modifier
                .padding(16.dp)
                .size(150.dp, 50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black.copy(alpha = 0.5f), contentColor = Color.White
            ),
        ) {
            Text(
                "Créditos",
                fontSize = 20.sp
            )
        }
    }
}
