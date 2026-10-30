package com.genzvid.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.genzvid.app.data.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen() {
    var items by remember { mutableStateOf<List<JsonObject>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var refreshing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadFeed() {
        scope.launch {
            loading = true
            try {
                val params = buildJsonObject {
                    put("after_id", "")
                    put("page_size", 20)
                    put("kind_hint", "all")
                }
                val result = SupabaseClient.client.postgrest.rpc("gen_public_feed_page", params)
                val json = result.data as? JsonObject ?: JsonObject(emptyMap())
                items = json["items"]?.jsonArray?.mapNotNull { it as? JsonObject } ?: emptyList()
                error = null
            } catch (e: Exception) {
                error = e.message ?: "Failed to load feed. Using offline placeholder."
                items = listOf(
                    buildJsonObject {
                        put("data", buildJsonObject {
                            put("title", "Welcome to GEN Z VID Native")
                            put("description", "This is a native Android app connected to the existing Supabase backend.")
                            put("channel_id", "demo")
                        })
                    }
                )
            } finally {
                loading = false
                refreshing = false
            }
        }
    }

    LaunchedEffect(Unit) { loadFeed() }

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = { refreshing = true; loadFeed() }) {
        if (loading) {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null && items.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Error: $error")
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.Button(onClick = { loadFeed() }) { Text("Retry") }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(items) { item ->
                    val data = item["data"]?.jsonObject
                    Card(Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(data?.get("title")?.jsonPrimitive?.content ?: "Untitled", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text(data?.get("description")?.jsonPrimitive?.content ?: "", maxLines = 3)
                            Spacer(Modifier.height(4.dp))
                            Text("Channel: ${data?.get("channel_id")?.jsonPrimitive?.content ?: "unknown"}", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
