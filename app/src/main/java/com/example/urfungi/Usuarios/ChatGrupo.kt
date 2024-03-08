package com.example.urfungi.Usuarios

import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ChatGrupo (grupoId: String?, Admin: String?, nombre: String?, navController: NavController){
    var mensaje by remember { mutableStateOf("") }
    val userAuth = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val grupoId = grupoId
    val firestore = Firebase.firestore
    var mensajes by remember { mutableStateOf<List<Mensajes>>(emptyList()) }
    var nombreUsuario by remember { mutableStateOf("Nombre Desconocido") }
    var urlImagen by remember { mutableStateOf<String?>(null) }
    var esAdmin by remember { mutableStateOf(false) }
    var mevoy by remember { mutableStateOf(false) }


    LaunchedEffect(true) {
        // Lógica para recuperar mensajes

        if (userAuth == Admin){
            esAdmin = true
        } else{
            esAdmin = false
        }

        obtenerMensajesFlow(grupoId).collect { mensajesActualizados ->
            Log.d("Mensajes", "Flujo de mensajes actualizado: $mensajesActualizados")

            // Actualizar la lista de mensajes
            mensajes = mensajesActualizados

            // Obtener el usuario y la URL de la imagen dentro del bucle
            mensajes.firstOrNull()?.usuarioMensaje?.let { usuarioId ->
                obtenerUsuarioAmigoDesdeId(usuarioId)?.let { usuarioAmigo ->
                    nombreUsuario = usuarioAmigo.username ?: "Nombre Desconocido"
                    urlImagen = usuarioAmigo.foto
                }

            }

            // Añadir log para verificar si la lista de mensajes se actualiza correctamente
            Log.d("Mensajes", "Lista de mensajes actualizada: $mensajes")
        }

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        // Texto en la parte superior
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nombre ?: "Grupo", // Reemplaza "Grupo" con el nombre real del grupo
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 16.dp)
            )

            // Botón para irse del grupo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = {
                        mevoy = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Salir del grupo",
                        tint = Color.Red // Color rojo para el botón de salir
                    )
                }

                // Botón para ajustes
                if (esAdmin) {
                    IconButton(
                        onClick = {
                            Log.d("Botón", "Botón de ajustes presionado")
                            navController.navigate("editarGrupo/${grupoId}")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Ajustes",
                            tint = Color.White
                        )
                    }
                }
            }
        }
        // Espaciado entre el título y la lista de mensajes
        Spacer(modifier = Modifier.height(8.dp))

        // Mostrar mensajes
        // Actualizar la sección donde muestras los mensajes
        LazyColumn(
            modifier = Modifier.weight(1f),
            reverseLayout = true
        ) {
            itemsIndexed(mensajes) { _, mensaje ->
                val isCurrentUserMessage = mensaje.usuarioMensaje == userAuth

                // Obtener el usuario y la URL de la imagen dentro del bucle
                LaunchedEffect(mensaje.usuarioMensaje) {
                    mensaje.usuarioMensaje?.let { usuarioId ->
                        obtenerUsuarioAmigoDesdeId(usuarioId)?.let { usuarioAmigo ->
                            // Actualizar nombreUsuario y urlImagen solo para el mensaje actual
                            if (mensaje.id == mensajes.firstOrNull()?.id) {
                                nombreUsuario = usuarioAmigo.username ?: "Nombre Desconocido"
                                urlImagen = usuarioAmigo.foto.orEmpty()
                            }
                        } ?: run {
                            // Manejar el caso cuando obtenerUsuarioAmigoDesdeId devuelve null
                            if (mensaje.id == mensajes.firstOrNull()?.id) {
                                nombreUsuario = "Nombre Desconocido"
                                urlImagen = ""
                            }
                        }
                    }
                }


                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    // Verificar si el mensaje no es nulo ni está vacío antes de mostrarlo
                    if (!mensaje.mensaje.isNullOrBlank()) {
                        // Mostrar mensaje y fecha/hora
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isCurrentUserMessage) Alignment.End else Alignment.Start
                        ) {
                            // Si el mensaje es del usuario actual, mostrar "Tu" como nombre
                            if (isCurrentUserMessage) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tu",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            } else {
                                // Mostrar la foto de perfil y el nombre del amigo
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (urlImagen != null) {
                                        AndroidView(
                                            factory = { context ->
                                                ImageView(context).apply {
                                                    Glide.with(context)
                                                        .load(urlImagen)
                                                        .fitCenter()
                                                        .transform(CircleCrop())
                                                        .into(this)
                                                }
                                            }, modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, Color.Green, CircleShape)
                                        )
                                    } else {
                                        // Manejar el caso en que la URL de la imagen sea nula
                                        Spacer(modifier = Modifier.size(40.dp))
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = nombreUsuario,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Mostrar mensaje
                            Text(
                                text = mensaje.mensaje,
                                modifier = Modifier
                                    .background(if (isCurrentUserMessage) Color(0xFF64B5F6) else Color.Gray)
                                    .padding(8.dp)
                            )

                            // Fecha y hora
                            Text(
                                text = mensaje.Fecha ?: "Fecha desconocida",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = if (isCurrentUserMessage) TextAlign.End else TextAlign.Start,
                                modifier = Modifier.padding(8.dp)
                            )


                        }

                    }
                }
            }
        }

        // Espaciado entre la lista de mensajes y la zona de escritura
        Spacer(modifier = Modifier.height(8.dp))

        // Zona de escritura
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Campo de texto para escribir el mensaje
            TextField(
                value = mensaje,
                onValueChange = { mensaje = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Lógica para enviar el mensaje al presionar "Done" en el teclado
                        enviarMensaje(firestore, grupoId, mensaje, userAuth, visto = false)
                        mensaje = ""
                    }
                ),
                placeholder = { Text("Escribe tu mensaje...") },
            )

            // Botón para enviar el mensaje
            Button(
                onClick = {
                    // Lógica para enviar el mensaje al hacer clic en el botón
                    enviarMensaje(firestore, grupoId, mensaje, userAuth, visto = false)
                    mensaje = ""
                },
                modifier = Modifier
                    .height(56.dp)
                    .padding(start = 8.dp)
            ) {
                Text("Enviar")
            }
        }
    }
    if (mevoy) {
        showDialogConfirmationSalirGrupo(navController, grupoId, esAdmin){
            mevoy = false
        }
    }
}



fun obtenerMensajesFlow(chatId: String?): Flow<List<Mensajes>> = callbackFlow {
    val firestore = Firebase.firestore
    val query = firestore.collection("Mensajes")
        .whereEqualTo("idchat", chatId)
        .orderBy("Fecha", Query.Direction.DESCENDING)

    val listener = query.addSnapshotListener { snapshots, error ->
        if (error != null) {
            // Manejar el error si es necesario
            Log.e("Mensajes", "Error al obtener mensajes: ${error.message}")
            trySend(emptyList()).isSuccess
            return@addSnapshotListener
        }

        if (snapshots != null) {
            val mensajes = snapshots.mapNotNull { document ->
                document.toObject(Mensajes::class.java)
            }
            Log.d("Mensajes", "Mensajes actualizados en tiempo real: $mensajes")
            trySend(mensajes).isSuccess
        }
    }

    awaitClose { listener.remove() }
}




private fun enviarMensaje(
    firestore: FirebaseFirestore,
    chatId: String?,
    mensaje: String,
    usuarioId: String,
    visto: Boolean

) {
    val mensajeData = hashMapOf(
        "id" to generateMessageId(),
        "idchat" to chatId,
        "Fecha" to obtenerFechaActual(),
        "mensaje" to mensaje,
        "usuarioMensaje" to usuarioId,
        "visto" to visto
    )

    firestore.collection("Mensajes")
        .add(mensajeData)
        .addOnSuccessListener {
            // Mensaje enviado exitosamente

        }
        .addOnFailureListener { e ->
            // Manejar el error si es necesario
            Log.d("Mensajes", "Error al guardar el mensaje: ${e.message}")
        }
}

private fun generateMessageId(): String {
    return System.currentTimeMillis().toString()
}

private fun obtenerFechaActual(): String {
    val fechaActual = Calendar.getInstance().time
    val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formato.format(fechaActual)
}

// Función para obtener la información del usuario desde la base de datos
private suspend fun obtenerUsuarioAmigoDesdeId(usuarioId: String): usuarios? {
    Log.d("Amigos", "Iterando sobre el usuarioAmigo: $usuarioId")
    val firestore = FirebaseFirestore.getInstance()
    val docRef = firestore.collection("usuarios").document(usuarioId)

    return try {
        val documentSnapshot: DocumentSnapshot = docRef.get().await()

        if (documentSnapshot.exists()) {
            // Convertir el documento a la clase usuarios
            val usuarioAmigo = documentSnapshot.toObject(usuarios::class.java)
            return usuarioAmigo
        } else {
            // El documento no existe
            null
        }
    } catch (e: Exception) {
        // Manejar errores de consulta
        null
    }
}

@Composable
fun EditarGrupoScreen(
    navController: NavController,
    grupoId: String?

) {
    var amigosDisponibles by remember { mutableStateOf<List<usuarios>>(emptyList()) }
    var amigosSeleccionados by remember { mutableStateOf<List<usuarios>>(emptyList()) }
    var usuario by remember { mutableStateOf<usuarios?>(null) }
    var amigos by remember { mutableStateOf<List<usuarios>>(emptyList()) }
    var nombreGrupo by remember { mutableStateOf(TextFieldValue("")) }
    var grupo by remember { mutableStateOf<Chat?>(null) }
    var showDialog by remember { mutableStateOf(false) }


    LaunchedEffect(true) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val db = Firebase.firestore

            // Obtener información del usuario logeado
            val userReference: DocumentReference = db.collection("usuarios").document(userId)

            userReference.get().addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    usuario = documentSnapshot.toObject(usuarios::class.java)

                    // Obtener la lista de amigos del usuario
                    val listaAmigos = usuario?.amigos ?: emptyList()

                    Log.d("Amigos", "Listar listaAmigos: $listaAmigos")

                    if (listaAmigos.isNotEmpty()) {
                        GlobalScope.launch {
                            // Obtener información de cada amigo y almacenarla en la lista 'amigos'
                            amigos = listaAmigos.mapNotNull { amigoId ->
                                obtenerUsuarioAmigoDesdeId(amigoId)
                            }
                            Log.d("Amigos", "Listar amigos: $amigos")


                            amigosDisponibles = amigos.toList()

                            Log.d("Amigos", "Listar amigosDisponibles: $amigosDisponibles")

                            // Obtener información del grupo
                            val groupReference = grupoId?.let { db.collection("Chat").document(it) }

                            if (groupReference != null) {
                                groupReference.get().addOnSuccessListener { documentSnapshot ->
                                    if (documentSnapshot.exists()) {
                                        grupo = documentSnapshot.toObject(Chat::class.java)

                                        // Obtener información de los usuarios en el grupo
                                        val idsUsuariosEnGrupo = grupo?.integrantes ?: emptyList()

                                        // Utilizar coroutines async para obtener información de usuarios en paralelo
                                        GlobalScope.launch {
                                            // Obtener la lista de amigos seleccionados
                                            amigosSeleccionados =
                                                obtenerUsuariosEnGrupo(idsUsuariosEnGrupo)

                                            // Filtrar al usuario logeado de la lista de amigos seleccionados
                                            val idUsuarioLogeado = usuario?.id ?: ""
                                            amigosSeleccionados =
                                                amigosSeleccionados.filter { it.id != idUsuarioLogeado }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = nombreGrupo.text,
            onValueChange = { nombreGrupo = nombreGrupo.copy(text = it) },
            label = { Text(grupo?.nombreGrupo ?: "Grupo") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )


        // Lista de amigos disponibles
        Text("Selecciona amigos para el grupo:")
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(amigosDisponibles) { amigo ->
                AmigoCardEditarGrupo(
                    amigo = amigo,
                    onAddClick = {
                        amigosSeleccionados = amigosSeleccionados + it
                    },
                    navController = navController
                )
            }
        }

        // Lista de amigos seleccionados
        Text("Usuarios seleccionados:")
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(amigosSeleccionados.distinctBy { it.id }) { amigo ->
                AmigoCardEditarGrupoSelect(
                    amigo = amigo,
                    onAddClick = {
                        amigosSeleccionados = amigosSeleccionados.filter { selectedAmigo ->
                            selectedAmigo != amigo
                        }
                    },
                    navController = navController
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón para crear el grupo con amigos seleccionados
            Button(
                onClick = {
                    // Verificar que haya al menos 1 amigos seleccionados para crear un grupo
                    if (amigosSeleccionados.size >= 1 && nombreGrupo.text.isNotEmpty()) {
                        // Verificar si el usuario logeado está presente en la lista de amigos seleccionados
                        val idUsuarioLogeado = usuario?.id ?: ""
                        val amigosSeleccionadosSinUsuarioLogeado =
                            amigosSeleccionados.filter { it.id != idUsuarioLogeado }

                        // Obtener IDs de amigos seleccionados, incluyendo al usuario logeado
                        val idsAmigosSeleccionados =
                            amigosSeleccionadosSinUsuarioLogeado.map { it.id ?: "" }
                                .plus(idUsuarioLogeado)

                        val db = Firebase.firestore
                        val grupoRef = db.collection("Chat").document(grupoId ?: "")
                        grupoRef.update(
                            mapOf(
                                "nombreGrupo" to nombreGrupo.text,
                                "integrantes" to idsAmigosSeleccionados
                            )
                        ).addOnSuccessListener {
                            // Grupo editado exitosamente
                            navController.navigate("mensajes")
                        }.addOnFailureListener { e ->
                            // Manejar el error si es necesario
                            Log.e("EditarGrupo", "Error al editar el grupo: ${e.message}")
                        }
                    } else {
                        // Mostrar un mensaje indicando que se necesitan al menos 2 usuarios para editar el grupo
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text("Editar Grupo")
            }

            // Botón para eliminar el grupo
            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text("Eliminar Grupo")
            }
        }
        if (showDialog) {
            showDialogConfirmation(navController, grupoId) {
                showDialog = false
            }
        }
    }
}

@Composable
fun AmigoCardEditarGrupo(
    amigo: usuarios,
    onAddClick: (usuarios) -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de perfil del amigo a la izquierda
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        val amigoFoto = amigo.foto
                        val imageUrl = "$amigoFoto"
                        Glide.with(context)
                            .load(imageUrl)
                            .fitCenter()
                            .transform(CircleCrop())
                            .into(this)
                    }
                }, modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Green, CircleShape)
            )

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(12.dp))

            // Columna para el Usuario y Nombre del amigo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Nombre de usuario más pequeño del amigo
                Text(
                    text = amigo.username ?: "Username",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )

                // Nombre completo del amigo
                Text(
                    text = amigo.nombre ?: "Nombre del Amigo",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(16.dp))

            // Botón de Agregar o Quitar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas
                    .background(Color.Green)
                    .clickable { onAddClick(amigo) }, // Acción al hacer clic
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Agregar",
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

        }
    }
}

@Composable
fun AmigoCardEditarGrupoSelect(
    amigo: usuarios,
    onAddClick: (usuarios) -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(10.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de perfil del amigo a la izquierda
            AndroidView(
                factory = { context ->
                    ImageView(context).apply {
                        val amigoFoto = amigo.foto
                        val imageUrl = "$amigoFoto"
                        Glide.with(context)
                            .load(imageUrl)
                            .fitCenter()
                            .transform(CircleCrop())
                            .into(this)
                    }
                }, modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Green, CircleShape)
            )

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(12.dp))

            // Columna para el Usuario y Nombre del amigo
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Nombre de usuario más pequeño del amigo
                Text(
                    text = amigo.username ?: "Username",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .padding(top = 8.dp)
                )

                // Nombre completo del amigo
                Text(
                    text = amigo.nombre ?: "Nombre del Amigo",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }

            // Espaciador horizontal
            Spacer(modifier = Modifier.width(16.dp))

            // Botón de Agregar o Quitar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)) // Esquinas redondeadas
                    .background(Color.Red)
                    .clickable { onAddClick(amigo) }, // Acción al hacer clic
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Quitar",
                    tint = Color.White,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

        }
    }
}

private suspend fun obtenerUsuariosEnGrupo(idsUsuarios: List<String>): List<usuarios> {
    return coroutineScope {
        idsUsuarios.map { usuarioId ->
            async { obtenerUsuarioAmigoDesdeId(usuarioId) }
        }.awaitAll().filterNotNull()
    }
}


@Composable
fun showDialogConfirmation(navController: NavController, grupoId: String?,    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {            onDismiss()
        },
        title = { Text("Eliminar Grupo") },
        text = { Text("¿Seguro que quieres eliminar este grupo?") },
        confirmButton = {
            Button(
                onClick = {
                    // Confirmación: Eliminar el grupo
                    eliminarGrupo(navController,grupoId)
                },
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()

                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

fun eliminarGrupo(navController: NavController,grupoId: String?) {
    val db = Firebase.firestore
    val grupoRef = db.collection("Chat").document(grupoId ?: "")
    grupoRef.delete().addOnSuccessListener {
        // Grupo eliminado exitosamente
        navController.navigate("mensajes")
    }.addOnFailureListener { e ->
        // Manejar el error si es necesario
        Log.e("EliminarGrupo", "Error al eliminar el grupo: ${e.message}")
    }
}

@Composable
fun showDialogConfirmationSalirGrupo(navController: NavController, grupoId: String?,esAdmin: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss()},
        title = { Text("Salir del Grupo") },
        text = { Text("¿Seguro que quieres salir de este grupo?") },
        confirmButton = {
            Button(
                onClick = {
                    // Confirmación: Salir del grupo
                    salirDelGrupo(navController, grupoId, esAdmin)
                }
            ) {
                Text("Sí")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()                }
            ) {
                Text("Cancelar")
            }
        }
    )
}

fun salirDelGrupo(navController: NavController, grupoId: String?, esAdmin: Boolean) {

    if (esAdmin) {
        Toast.makeText(
            navController.context,
            "Eres un administrador y no puedes salir del grupo",
            Toast.LENGTH_SHORT
        ).show()
        return
    }

    val db = Firebase.firestore
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val grupoRef = db.collection("Chat").document(grupoId ?: "")

    db.runBatch { batch ->
        // Eliminar al usuario actual de la lista de integrantes del grupo
        batch.update(grupoRef, "integrantes", FieldValue.arrayRemove(currentUserUid))
    }.addOnSuccessListener {
        // Usuario eliminado exitosamente del grupo
        navController.navigate("mensajes")
    }.addOnFailureListener { e ->
        // Manejar el error si es necesario
        Log.e("SalirDelGrupo", "Error al salir del grupo: ${e.message}")
    }
}
