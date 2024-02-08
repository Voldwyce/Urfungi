@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.urfungi

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun UserPostsScreen() {
    var posts by remember { mutableStateOf(emptyList<Post>()) }
    var setaTypes by remember { mutableStateOf(emptyList<String>()) }

    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun loadPosts() {
        db.collection("posts")
            .whereEqualTo("usuario", userId)
            .get()
            .addOnSuccessListener { result ->
                posts = result.documents.mapNotNull { document ->
                    val post = document.toObject(Post::class.java)
                    post?.id = document.id // Asignar el ID del documento a la propiedad 'id' del post
                    post
                }
            }
    }

    fun loadSetaTypes() {
        db.collection("Setas")
            .get()
            .addOnSuccessListener { result ->
                setaTypes = result.documents.mapNotNull { document ->
                    document.toObject(Setas::class.java).toString()
                }
                // Log the seta types
                Log.d("UserPostsScreen", "Seta Types: $setaTypes")
            }
    }

    LaunchedEffect(Unit) {
        loadPosts()
        loadSetaTypes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Mis Posts",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 17.dp, bottom = 8.dp)
        )

        UserPostsContent(posts = posts, setaTypes = setaTypes, onPostDeleted = ::loadPosts, onPostUpdated = ::loadPosts)
    }
}

@Composable
fun UserPostsContent(posts: List<Post>, setaTypes: List<String>, onPostDeleted: () -> Unit, onPostUpdated: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(posts) { post ->
            UserPostItem(post = post, setaTypes = setaTypes, onPostDeleted = onPostDeleted, onPostUpdated = onPostUpdated)
        }
    }
}
@Composable
fun UserPostItem(post: Post, setaTypes: List<String>, onPostDeleted: () -> Unit, onPostUpdated: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(post.titulo) }
    var editedDescription by remember { mutableStateOf(post.descripcion) }
    var editedSetaType by remember { mutableStateOf(post.idSeta) }
    var showSetaTypeDialog by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 30.dp)
            .clickable { showDialog = true } // Mostrar el diálogo al hacer clic
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(5.dp))
            Image(
                painter = rememberImagePainter(post.foto),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .width(200.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp
                        )
                    )
            )

            Spacer(modifier = Modifier.height(16.dp)) // Agregar espacio entre la imagen y el texto

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = post.titulo, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    Text(text = post.fecha, color = Color.LightGray)
                }
            }
        }
    }

    // Mostrar el diálogo al hacer clic en la tarjeta
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = post.titulo) },
            text = {
                Column {
                    // Mostrar la imagen
                    Image(
                        painter = rememberImagePainter(post.foto),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomEnd = 0.dp,
                                    bottomStart = 0.dp
                                )
                            )
                    )

                    Spacer(modifier = Modifier.height(27.dp)) // Agregar espacio entre la imagen y el texto

                    Text(text = post.descripcion)

                    Spacer(modifier = Modifier.height(25.dp)) // Agregar espacio entre la imagen y el texto

                    Text(text = "Fecha: ", fontWeight = FontWeight.Bold) // Fecha en negrita
                    Text(text = post.fecha)
                }
            },
            confirmButton = {
                Row {
                    IconButton(onClick = {
                        // Cerrar el diálogo de post
                        showDialog = false
                        // Mostrar el diálogo de edición
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = {
                        showDialog = false
                        showDeleteConfirmationDialog = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                    Button(
                        onClick = { showDialog = false }
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }

    // Mostrar el diálogo de confirmación de eliminación
    if (showDeleteConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que quieres eliminar este post?") },
            confirmButton = {
                Button(
                    onClick = {
                        // Eliminar el post
                        db.collection("posts").document(post.id)
                            .delete()
                            .addOnSuccessListener {
                                Log.d("Post", "DocumentSnapshot successfully deleted!")
                                // Recargar los posts
                                onPostDeleted()
                            }
                            .addOnFailureListener { e -> Log.w("Post", "Error deleting document", e) }

                        // Cerrar el diálogo de confirmación de eliminación
                        showDeleteConfirmationDialog = false
                    }
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmationDialog = false }
                ) {
                    Text("No")
                }
            }
        )
    }

    // Mostrar el diálogo de edición
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Editar post") },
            text = {
                Column {
                    TextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text("Título") }
                    )
                    TextField(
                        value = editedDescription,
                        onValueChange = { editedDescription = it },
                        label = { Text("Descripción") }
                    )
                    Button(onClick = { showSetaTypeDialog = true }) {
                        Text(text = "Seleccionar tipo de seta")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Actualizar el post
                        db.collection("posts").document(post.id)
                            .update(
                                mapOf(
                                    "titulo" to editedTitle,
                                    "descripcion" to editedDescription,
                                    "idSeta" to editedSetaType
                                )
                            )
                            .addOnSuccessListener {
                                Log.d("Post", "DocumentSnapshot successfully updated!")
                                // Recargar los posts
                                onPostUpdated()
                            }
                            .addOnFailureListener { e -> Log.w("Post", "Error updating document", e) }

                        // Cerrar el diálogo de edición
                        showEditDialog = false
                    }
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showEditDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showSetaTypeDialog) {
        AlertDialog(
            onDismissRequest = { showSetaTypeDialog = false },
            title = { Text("Seleccionar tipo de seta") },
            text = {
                if (setaTypes.isEmpty()) {
                    Text("No hay tipos de seta disponibles")
                } else {
                    LazyColumn {
                        items(setaTypes) { setaType ->
                            Text(
                                text = setaType,
                                modifier = Modifier.clickable {
                                    editedSetaType = setaType
                                    showSetaTypeDialog = false
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSetaTypeDialog = false }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

}