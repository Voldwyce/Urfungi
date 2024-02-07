package com.example.urfungi

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import kotlin.properties.Delegates

@Composable
fun PostsScreen() {
    var posts by remember { mutableStateOf(listOf<Post>()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        posts = getPostsByActiveUser()
        loading = false
    }

    if (loading) {
        CircularProgressIndicator()
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(posts) { post ->
                PostItem(post)
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PostItem(post: Post) {
    val context = LocalContext.current
    var imageUrl by remember { mutableStateOf(post.foto) }

    Card(
        modifier = Modifier
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = post.titulo)
            Text(text = post.fecha)
            Text(text = post.idSeta, maxLines = Int.MAX_VALUE)
            Text(text = post.descripcion, maxLines = Int.MAX_VALUE)
            Text(text = getEmojiByToxicity(post.idSeta))

            AndroidView(factory = { ctx ->
                ImageView(ctx).apply {
                    Glide.with(ctx)
                        .load(imageUrl)
                        .into(this)
                }
            })
        }
    }
}

suspend fun getPostsByActiveUser(): List<Post> {
    val activeUserId = FirebaseAuth.getInstance().currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val posts = mutableListOf<Post>()
    try {
        val querySnapshot =
            db.collection("posts").whereEqualTo("usuario", activeUserId).get().await()
        for (document in querySnapshot.documents) {
            val post = document.toObject(Post::class.java)
            if (post != null) {
                posts.add(post)
                Log.d("PostsScreen", "Post recuperado: ${post.titulo}")
            }
        }
    } catch (e: Exception) {
        Log.d("PostsScreen", "Error al recuperar los posts: ${e.message}")
    }
    return posts
}

fun getEmojiByToxicity(toxicity: String): String {
    return when (toxicity) {
        "Segura" -> "Segura 😊"
        "Leve" -> "Leve 😐"
        "Peligrosa" -> "Peligrosa 😨"
        "Muy peligrosa" -> " Muy peligrosa 😱"
        "Mortal" -> "Mortal 💀"
        else -> ""
    }
}