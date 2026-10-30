package com.genzvid.app.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.genzvid.app.data.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun CreateScreen() {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedUri = uri
        status = if (uri != null) "Media selected" else "No media selected"
    }

    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        status = if (granted) "Camera permission granted" else "Camera permission denied"
    }

    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        status = if (granted) "Microphone permission granted" else "Microphone permission denied"
    }

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Post", style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            photoPicker.launch(androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
        }) { Text("Pick Photo / Video") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                status = "Camera ready"
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }) { Text("Request Camera") }
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                status = "Microphone ready"
            } else {
                micPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }) { Text("Request Microphone") }
        Spacer(Modifier.height(16.dp))
        if (status != null) Text(status!!)
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    val user = SupabaseClient.client.auth.currentUserOrNull()
                    status = if (user == null) "Please sign in first" else "Post prepared (connect to gen_commit for full publish)"
                }
            },
            enabled = title.isNotBlank() || description.isNotBlank()
        ) { Text("Publish (Native)") }
    }
}
