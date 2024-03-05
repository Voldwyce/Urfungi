package com.example.urfungi.Posts

import android.content.ContentValues
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import com.example.urfungi.Repo.Setas
import com.example.urfungi.addComment
import com.example.urfungi.deleteComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun UserPostsScreen(navController: NavController) {
    // Declaración de variables de estado para los posts y los tipos de setas
    var posts by remember { mutableStateOf(emptyList<Post>()) }
    var setaTypes by remember { mutableStateOf(emptyList<Setas>()) }

    // Declaración de variables de estado para el filtro y orden
    var selectedToxicity by remember { mutableStateOf("") }
    var selectedOrder by remember { mutableStateOf("Fecha") }

    // Obtener instancia de Firestore y el ID del usuario actual
    val db = FirebaseFirestore.getInstance()
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Función para cargar los posts del usuario actual
    fun loadPosts() {
        db.collection("posts")
            .whereEqualTo("usuario", userId)
            .get()
            .addOnSuccessListener { result ->
                posts = result.documents.mapNotNull { document ->
                    val post = document.toObject(Post::class.java)
                    post?.id = document.id
                    post
                }
            }
    }

    // Función para obtener los tipos de setas de Firestore
    suspend fun fetchMushroomData(): List<Setas> {
        val db = FirebaseFirestore.getInstance()
        return try {
            val documents = db.collection("setas").get().await()

            val mushroomList = documents.mapNotNull { document ->
                val id = document.id
                val nombre = document.getString("Nombre")
                val nombreCientifico = document.getString("NombreCientifico")
                val familia = document.getString("Familia")
                val temporada = document.getString("Temporada")
                val imagen = document.getString("Imagen")
                val comestible = document.getString("Comestible")
                val toxicidad = document.getString("Toxicidad")
                val descripcion = document.getString("Descripcion")
                val habitat = document.getString("Habitat")
                val dificultad = document.getString("Dificultad")
                val curiosidades = document.getString("Curiosidades")

                if (id != null && nombre != null && nombreCientifico != null && familia != null && temporada != null && imagen != null && comestible != null && toxicidad != null && descripcion != null && habitat != null && dificultad != null && curiosidades != null) {
                    Setas(
                        id,
                        nombre,
                        nombreCientifico,
                        familia,
                        temporada,
                        imagen,
                        comestible,
                        toxicidad,
                        descripcion,
                        habitat,
                        dificultad,
                        curiosidades
                    )
                } else null
            }

            Log.d(ContentValues.TAG, "Mushroom List: $mushroomList")

            mushroomList
        } catch (exception: Exception) {
            Log.w(ContentValues.TAG, "Error getting documents: ", exception)
            emptyList()
        }
    }

    // Cargar los posts y los tipos de setas cuando se inicializa el componente
    LaunchedEffect(Unit) {
        loadPosts()
        setaTypes = fetchMushroomData()
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filtro por toxicidad
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Toxicidad:")
            val toxicities = listOf("Segura", "Leve", "Peligrosa", "Muy peligrosa", "Mortal")
            toxicities.forEach { toxicity ->
                RadioButton(
                    selected = selectedToxicity == toxicity,
                    onClick = { selectedToxicity = toxicity },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filtro por orden
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Orden:")
            val orders = listOf("Fecha", "Likes", "Comentarios")
            orders.forEach { order ->
                RadioButton(
                    selected = selectedOrder == order,
                    onClick = { selectedOrder = order },
                )
            }
        }

        // Título de los posts
        Text(
            text = "Mis Posts",
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(start = 17.dp, bottom = 8.dp)
        )

        UserPostsContent(
            posts = posts,
            setaTypes = setaTypes,
            onPostDeleted = ::loadPosts,
            onPostUpdated = ::loadPosts,
            currentUser = currentUser,
            navController = navController,
            selectedToxicity = selectedToxicity,
            selectedOrder = selectedOrder
        )
    }
}
@Composable
fun UserPostsContent(
    posts: List<Post>,
    setaTypes: List<Setas>,
    onPostDeleted: () -> Unit,
    onPostUpdated: () -> Unit,
    currentUser: FirebaseUser?,
    navController: NavController,
    selectedToxicity: String,
    selectedOrder: String
) {
    // Lógica de filtrado y ordenamiento
    val filteredPosts = filterAndSortPosts(posts, setaTypes, selectedToxicity, selectedOrder)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(filteredPosts) { post ->
            UserPostItem(
                post = post,
                setaTypes = setaTypes,
                onPostDeleted = onPostDeleted,
                onPostUpdated = onPostUpdated,
                currentUser = FirebaseAuth.getInstance().currentUser,
                navController = navController
            )
        }
    }
}

@Composable
fun UserPostItem(
    post: Post,
    setaTypes: List<Setas>,
    onPostDeleted: () -> Unit,
    onPostUpdated: () -> Unit,
    currentUser: FirebaseUser?,
    navController: NavController
) {
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(post.titulo) }
    var editedDescription by remember { mutableStateOf(post.descripcion) }
    var editedSetaType by remember { mutableStateOf(post.idSeta) }
    var showSetaTypeDialog by remember { mutableStateOf(false) }
    var newComment by remember { mutableStateOf("") }
    var likes by remember { mutableStateOf(post.likes.size) }
    var commentCount by remember { mutableStateOf(post.comentarios.size) }
    var isLiked by remember { mutableStateOf(post.likes.contains(currentUser?.uid)) }
    var editedPrivacy by remember { mutableStateOf(post.privacidad) }
    var showPrivacyDialog by remember { mutableStateOf(false) }


    LaunchedEffect(post.id) {
        val postRef = FirebaseFirestore.getInstance().collection("posts").document(post.id)
        val listenerRegistration = postRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w("UserPostItem", "Listen failed.", error)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                likes = (snapshot.get("likes") as? List<*>)?.size ?: 0
                isLiked = (snapshot.get("likes") as? List<*>)?.contains(currentUser?.uid) ?: false
                commentCount = (snapshot.get("comentarios") as? List<*>)?.size ?: 0
                post.comentarios = (snapshot.get("comentarios") as? List<String>) ?: emptyList()
            }
        }
    }

    var setaName = setaTypes.find { it.id == post.idSeta }?.Nombre ?: "Desconocido"

    val db = FirebaseFirestore.getInstance()
    var showCommentsDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp, 15.dp, 15.dp)
            .clickable { showDialog = true } // Mostrar el diálogo al hacer clic
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            AsyncImage(
                model = post.foto,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .clip(
                        RoundedCornerShape(
                            topStart = 12.dp,
                            topEnd = 12.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp
                        )
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = post.titulo, fontWeight = FontWeight.Bold, color = Color.LightGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = post.fecha, color = Color.LightGray)
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var isLiked = post.likes.contains(currentUser?.uid)

                    IconButton(onClick = {
                        val userId = currentUser?.uid
                        if (userId != null) {
                            val postRef = db.collection("posts").document(post.id)
                            postRef.get().addOnSuccessListener { documentSnapshot ->
                                val currentPost = documentSnapshot.toObject(Post::class.java)
                                if (currentPost != null) {
                                    val likes = currentPost.likes.toMutableList()
                                    if (likes.contains(userId)) {
                                        likes.remove(userId)
                                        isLiked = false
                                    } else {
                                        likes.add(userId)
                                        isLiked = true
                                    }
                                    postRef.update("likes", likes)
                                }
                            }
                        }
                    }, modifier = Modifier.size(24.dp)) {
                        if (isLiked) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Likes",
                                tint = Color.Red
                            )
                        } else {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = "Likes",
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = likes.toString(), color = Color.Gray)
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showCommentsDialog = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Comentarios",
                                tint = Color.Gray
                            )
                        }
                        Text(
                            text = commentCount.toString(),
                            color = Color.Gray
                        ) // Mostrar el número de comentarios
                    }
                }
            }
        }

        if (showCommentsDialog) {
            AlertDialog(
                onDismissRequest = { showCommentsDialog = false },
                title = { Text("Comentarios") },
                text = {
                    Column {
                        LazyColumn {
                            items(post.comentarios) { comment ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        val commentParts = comment.split(" at ")
                                        val usernameAndComment = commentParts[0].split(": ")
                                        val username = usernameAndComment[0]
                                        val commentText = usernameAndComment[1]
                                        val timestamp = commentParts[1]

                                        Text(text = username, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = commentText)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = timestamp, fontSize = 12.sp)

                                        Log.d("DEBUG", "Username del comentario: $username")
                                        Log.d(
                                            "DEBUG",
                                            "DisplayName del usuario actual: ${currentUser?.displayName}"
                                        )
                                        IconButton(onClick = {
                                            deleteComment(
                                                post.id,
                                                comment
                                            )
                                            commentCount--
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Comment Button",
                                                tint = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextField(
                                value = newComment,
                                onValueChange = { newComment = it },
                                label = { Text("Escribe un comentario") },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (currentUser != null && newComment.isNotBlank()) {
                                        val fullComment = "${currentUser.displayName}: $newComment"
                                        addComment(post.id, fullComment)
                                        commentCount++ // Incrementar el contador de comentarios
                                        newComment = ""
                                    }
                                },
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send Comment Button",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showCommentsDialog = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }

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

                        Spacer(modifier = Modifier.height(27.dp))

                        Text(text = post.descripcion)

                        Spacer(modifier = Modifier.height(25.dp))

                        Text(text = "Fecha: ", fontWeight = FontWeight.Bold) // Fecha en negrita
                        Text(text = post.fecha)
                    }
                },
                confirmButton = {
                    Row {
                        IconButton(
                            onClick = {
                                val cordenadas = post.cordenadas.split(",")
                                val latitud = cordenadas[0].trim().toDouble()
                                val longitud = cordenadas[1].trim().toDouble()
                                Log.d("UserPostItem", "Latitud: $latitud, Longitud: $longitud")
                                navController.navigate("mapScreen/${latitud}/${longitud}")                            },
                            modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = "Map Button",
                                tint = Color.Gray
                            )
                        }
                        IconButton(onClick = {
                            showDialog = false
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
                                    onPostDeleted()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        "Post",
                                        "Error deleting document",
                                        e
                                    )
                                }

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
                            val displayText = if (setaName == "Desconocido") {
                                "Seleccionar tipo de seta"
                            } else {
                                setaName
                            }
                            Text(text = displayText)
                        }
                        Button(onClick = { showPrivacyDialog = true }) {
                            val displayText = if (editedPrivacy == "") {
                                "Seleccionar privacidad"
                            } else {
                                editedPrivacy
                            }
                            Text(text = displayText)
                        }

                        if (showPrivacyDialog) {
                            AlertDialog(
                                onDismissRequest = { showPrivacyDialog = false },
                                title = { Text("Seleccionar privacidad") },
                                text = {
                                    Column {
                                        TextButton(onClick = {
                                            editedPrivacy = "Público"
                                            showPrivacyDialog = false
                                        }) {
                                            Text(text = "Público")
                                        }
                                        TextButton(onClick = {
                                            editedPrivacy = "Privado"
                                            showPrivacyDialog = false
                                        }) {
                                            Text(text = "Privado")
                                        }
                                        TextButton(onClick = {
                                            editedPrivacy = "Solo amigos"
                                            showPrivacyDialog = false
                                        }) {
                                            Text(text = "Solo amigos")
                                        }
                                    }
                                },
                                confirmButton = { }
                            )
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
                                        "idSeta" to editedSetaType,
                                        "privacidad" to editedPrivacy
                                    )
                                )
                                .addOnSuccessListener {
                                    Log.d("Post", "DocumentSnapshot successfully updated!")
                                    // Recargar los posts
                                    onPostUpdated()
                                }
                                .addOnFailureListener { e ->
                                    Log.w(
                                        "Post",
                                        "Error updating document",
                                        e
                                    )
                                }

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
                title = { Text("Selecciona una Seta") },
                text = {
                    LazyColumn {
                        items(setaTypes) { setaType ->
                            TextButton(onClick = {
                                editedSetaType = setaType.id // Actualiza el valor de editedSetaType
                                setaName = setaType.Nombre // Actualiza el valor de setaName
                                showSetaTypeDialog = false

                                // Imprimir todos los datos de la seta seleccionada
                                Log.d(ContentValues.TAG, "Selected Mushroom: $setaType")
                            }) {
                                Text(text = setaType.Nombre)
                            }
                        }
                    }
                },
                confirmButton = { }
            )
        }
    }
}

fun filterAndSortPosts(
    posts: List<Post>,
    setaTypes: List<Setas>,
    toxicity: String,
    order: String
): List<Post> {
    var filteredPosts = posts

    // Filtrar por toxicidad si se ha seleccionado una
    if (toxicity.isNotEmpty()) {
        filteredPosts = filteredPosts.filter { post ->
            val setaType = setaTypes.find { it.id == post.idSeta }
            setaType?.Toxicidad == toxicity
        }
    }

    // Ordenar según el criterio seleccionado
    filteredPosts = when (order) {
        "Fecha" -> filteredPosts.sortedByDescending { it.fecha }
        "Likes" -> filteredPosts.sortedByDescending { it.likes.size }
        "Comentarios" -> filteredPosts.sortedByDescending { it.comentarios.size }
        else -> filteredPosts
    }

    return filteredPosts
}

fun navigateToMap(navController: NavController, lat: Double, lon: Double) {
    navController.navigate("mapScreen/$lat/$lon")
}