package com.example.littlelemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.littlelemon.ui.theme.LittleLemonTheme
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState

class MainActivity : ComponentActivity() {
    private val httpClient = HttpClient(Android) {
        install(ContentNegotiation) {
            json(contentType = ContentType("text", "plain"))
        }
    }

    private val database by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database").build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LittleLemonTheme {
                // add databaseMenuItems code here
                val databaseMenuItems = database.menuItemDao()
                    .getAll()
                    .observeAsState(emptyList())
                    .value

                // add orderMenuItems variable here
                val orderMenuItems = remember {
                    mutableStateOf(false)
                }

                // add menuItems variable here
                val menuItems = if (orderMenuItems.value) {
                    databaseMenuItems.sortedBy {
                        it.title
                    }
                } else databaseMenuItems

                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "logo",
                        modifier = Modifier.padding(50.dp)
                    )

                    // add Button code here
                    Button(
                        onClick = { orderMenuItems.value = true },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 10.dp)
                    ) {
                        Text(text = "Tap to Order By Name")
                    }

                    // add searchPhrase variable here
                    val searchPhrase = remember {
                        mutableStateOf("")
                    }

                    // Add OutlinedTextField
                    OutlinedTextField(
                        value = "",
                        onValueChange = {
                            searchPhrase.value = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 50.dp),
                        label = { Text(text = "Search") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_search_24),
                                contentDescription = ""
                            )
                        }
                    )

                    // add is not empty check here
                    MenuItemsList(items = menuItems)

                }
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            if (database.menuItemDao().isEmpty()) {
                // add code here
                val menuList = fetchMenu()
                saveMenuToDatabase(menuList)
            }
        }
    }

    private suspend fun fetchMenu(): List<MenuItemNetwork> {
        val response: MenuNetwork =
            httpClient.get("https://raw.githubusercontent.com/Meta-Mobile-Developer-PC/Working-With-Data-API/main/littleLemonSimpleMenu.json")
                .body()
        return response.menu
    }

    private fun saveMenuToDatabase(menuItemsNetwork: List<MenuItemNetwork>) {
        val menuItemsRoom = menuItemsNetwork.map { it.toMenuItemRoom() }
        database.menuItemDao().insertAll(*menuItemsRoom.toTypedArray())
    }
}

@Composable
private fun MenuItemsList(items: List<MenuItemRoom>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 20.dp)
    ) {
        items(
            items = items,
            itemContent = { menuItem ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(menuItem.title)
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp),
                        textAlign = TextAlign.Right,
                        text = "%.2f".format(menuItem.price)
                    )
                }
            }
        )
    }
}
