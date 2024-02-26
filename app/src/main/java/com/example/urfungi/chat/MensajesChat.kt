    package com.example.urfungi.chat

    import android.util.Log
    import android.widget.ImageView
    import androidx.compose.foundation.Canvas
    import androidx.compose.foundation.background
    import androidx.compose.foundation.border
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.itemsIndexed
    import androidx.compose.foundation.shape.CircleShape
    import androidx.compose.foundation.text.KeyboardActions
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material3.*
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.text.input.ImeAction
    import androidx.compose.ui.text.input.KeyboardType
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.compose.ui.viewinterop.AndroidView
    import com.bumptech.glide.Glide
    import com.bumptech.glide.load.resource.bitmap.CircleCrop
    import com.example.urfungi.R
    import com.example.urfungi.usuarios
    import com.google.firebase.Firebase
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.DocumentSnapshot
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.Query
    import com.google.firebase.firestore.firestore
    import kotlinx.coroutines.channels.awaitClose
    import kotlinx.coroutines.flow.*
    import kotlinx.coroutines.tasks.await
    import java.text.SimpleDateFormat
    import java.util.*

    @Composable
     fun MensajesChat(usuarioId: String, username: String, imagen: String) {
        var mensaje by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val userAuth = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        val chatId = generateChatId(userAuth, usuarioId)
        val firestore = Firebase.firestore
        var mensajes by remember { mutableStateOf<List<Mensajes>>(emptyList()) }
        var usuario by remember { mutableStateOf<usuarios?>(null) }

        LaunchedEffect(true) {
            existeSalaChatFlow(chatId).collect { chatExistente ->
                Log.d("Mensajes", "Chat existente: $chatExistente")

                obtenerUsuarioDesdeId(userAuth)?.let {
                    usuario = it
                }

                if (!chatExistente) {
                    // Código para crear la sala de chat si no existe
                    val chatData = hashMapOf(
                        "id" to chatId,
                        "integrantes" to listOf(userAuth, usuarioId),
                        "grupo" to false,
                        "nombreGrupo" to null,
                        "usuariosEnChat" to listOf(userAuth) // Inicialmente, solo el usuario actual está en el chat

                    )

                    firestore.collection("Chat")
                        .document(chatId)
                        .set(chatData)
                        .addOnSuccessListener {
                            // Sala de chat creada exitosamente
                            Log.d("Mensajes", "Sala de chat creada exitosamente")


                        }
                        .addOnFailureListener { e ->
                            // Mostrar mensaje de error al guardar en Firestore
                            errorMessage = "Error al guardar datos en Firestore: ${e.message}"
                            Log.e("Mensajes", "Error al guardar datos en Firestore: ${e.message}")
                        }
                }

                Log.d("Mensajes", "Llego aqui,llego aqui,llego aqui,llego aqui")

                marcarMensajesComoVistosAlEntrar(firestore, chatId, usuarioId)


                // Lógica para recuperar mensajes
                obtenerMensajesFlow(chatId).collect { mensajesActualizados ->
                    Log.d("Mensajes", "Flujo de mensajes actualizado: $mensajesActualizados")

                    // Actualizar la lista de mensajes
                    mensajes = mensajesActualizados
                    // Añadir log para verificar si la lista de mensajes se actualiza correctamente
                    Log.d("Mensajes", "Lista de mensajes actualizada: $mensajes")
                    obtenerUsuarioDesdeId(userAuth)



                }
                Log.d("Mensajes", "Toy fuera")



            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(8.dp)
        ) {
            // Texto en la parte superior
            Text(
                text = "Chat con ${username}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 16.dp, top = 12.dp)
            )

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
                                        AndroidView(
                                            factory = { context ->
                                                ImageView(context).apply {
                                                    val imageUrl = imagen
                                                    Glide.with(context)
                                                        .load(imageUrl)
                                                        .fitCenter()
                                                        .transform(CircleCrop())
                                                        .into(this)
                                                }
                                            }, modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .border(2.dp, Color.Green, CircleShape)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = username,
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

// Agregar el indicador de estado solo si el mensaje no ha sido visto y el usuario actual no es el remitente
                                if (mensaje.usuarioMensaje == userAuth) {
                                    EstadoIndicadorAmigo(mensaje, userAuth)
                                }

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
                            enviarMensaje(firestore, chatId, mensaje, userAuth, visto = false)
                            mensaje = ""
                        }
                    ),
                    placeholder = { Text("Escribe tu mensaje...") },
                )

                // Botón para enviar el mensaje
                Button(
                    onClick = {
                        // Lógica para enviar el mensaje al hacer clic en el botón
                        enviarMensaje(firestore, chatId, mensaje, userAuth, visto = false)
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
    }



    fun existeSalaChatFlow(chatId: String): Flow<Boolean> = callbackFlow {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("Chat").document(chatId)

        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Manejar el error si es necesario
                Log.e("Mensajes", "Error al obtener sala: ${error.message}")
                trySend(false).isSuccess
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                trySend(true).isSuccess
            } else {
                trySend(false).isSuccess
            }
        }

        awaitClose { listener.remove() }
    }

    fun obtenerMensajesFlow(chatId: String): Flow<List<Mensajes>> = callbackFlow {
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
        chatId: String,
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
                marcarMensajesComoVistosAlEntrar(firestore, chatId, usuarioId)

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

    // Función para generar el ID de la sala de chat
    private fun generateChatId(userId1: String, userId2: String): String {
        // Ordenar alfabéticamente las IDs de los usuarios y luego concatenarlas
        val sortedIds = listOf(userId1, userId2).sorted()
        return "chat_${sortedIds[0]}_${sortedIds[1]}"
    }

    // Función para obtener la información del usuario desde la base de datos
    private suspend fun obtenerUsuarioDesdeId(usuarioId: String): usuarios? {
        val firestore = FirebaseFirestore.getInstance()
        val docRef = firestore.collection("Usuarios").document(usuarioId)

        return try {
            val documentSnapshot: DocumentSnapshot = docRef.get().await()

            if (documentSnapshot.exists()) {
                // Convertir el documento a la clase usuarios
                val usuario = documentSnapshot.toObject(usuarios::class.java)
                return usuario
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
    fun EstadoIndicadorAmigo(mensaje: Mensajes, usuarioActual: String) {
        if (mensaje.usuarioMensaje == usuarioActual) {
            Canvas(
                modifier = Modifier
                    .size(12.dp)
                    .padding(start = 4.dp)
            ) {
                drawCircle(
                    color = if (mensaje.visto) Color.Blue else Color.Black
                )
            }
        }
    }

    private fun marcarMensajesComoVistosAlEntrar(firestore: FirebaseFirestore, chatId: String, usuarioId: String) {
        // Marcar mensajes no propios como vistos
        val mensajesRef = firestore.collection("Mensajes")
        mensajesRef.whereEqualTo("idchat", chatId)
            .whereEqualTo("usuarioMensaje", usuarioId)
            .whereEqualTo("visto", false)
            .get()
            .addOnSuccessListener { snapshots ->
                for (snapshot in snapshots) {
                    val messageId = snapshot.id
                    mensajesRef.document(messageId).update("visto", true)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Mensajes", "Error al marcar mensajes como vistos al entrar: ${e.message}")
            }
    }


