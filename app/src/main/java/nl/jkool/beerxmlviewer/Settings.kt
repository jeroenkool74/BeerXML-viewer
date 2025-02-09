package nl.jkool.beerxmlviewer

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import nl.jkool.beerxmlviewer.ui.theme.BeerXMLViewerTheme
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer

fun storeSettings(context: Context, site: String, folder: String, username: String, password: String) {
    val inputSite = if (site == "") "ftp://" else site
    val inputFolder = if (folder == "") "/" else folder
    val jsObject = JSONObject()
        .put("site", inputSite)
        .put("folder", inputFolder)
        .put("username", username)
        .put("password", password)
    var writer: Writer? = null
    try {
        val out = context.openFileOutput("settings.json", Context.MODE_PRIVATE)
        writer = OutputStreamWriter(out)
        writer.write(jsObject.toString())
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
    } finally {
        writer?.close()
    }
}

fun getSettings(context: Context): Map<String, String> {
    var jsonObject = JSONObject()
    val reader: BufferedReader?
    try {
        val `in` = context.openFileInput("settings.json")
        reader = BufferedReader(InputStreamReader(`in`))
        val jsonObj2 = StringBuilder()
        for (line in reader.readLine()) {
            jsonObj2.append(line)
        }
        jsonObject = (JSONObject(jsonObj2.toString()))
    } catch (_: Exception) { }
    return jsonObject.toStringMap()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(activity: MainActivity, context: Context) {
    val settings = getSettings(context)
    var site by remember {
        mutableStateOf(
            settings.getOrDefault("site", "ftp://")
        )
    }
    var folder by remember {
        mutableStateOf(
            settings.getOrDefault("folder", "/")
        )
    }
    var username by remember {
        mutableStateOf(
            settings.getOrDefault("username", "")
        )
    }
    var password by remember {
        mutableStateOf(
            settings.getOrDefault("password", "")
        )
    }

    BeerXMLViewerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Menu",
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp).clickable {
                                activity.setContent {
                                    storeSettings(context, site, folder, username, password)
                                    Main(activity, context)
                                }
                            }
                        )
                    },
                    title = {
                        Text(text = "Settings")
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(12.dp)
            ) {
                Text("FTP site, must start with 'ftp://;")

                Row {
                    TextField(
                        value = site,
                        onValueChange = { site = it },
                        label = { Text("ftp site") }
                    )
                }

                Text("Folder, use '/' for root dir", modifier = Modifier.padding(top = 12.dp))
                TextField(
                    value = folder,
                    onValueChange = { folder = it },
                    label = { Text("folder") }
                )

                Text("Username", modifier = Modifier.padding(top = 12.dp))
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("username") }
                )

                Text("Password", modifier = Modifier.padding(top = 12.dp))
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("password") }
                )

                Box(
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Button(
                        onClick = {}
                    ) {
                        Text(
                            text = "Obtain XML from ftp",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}