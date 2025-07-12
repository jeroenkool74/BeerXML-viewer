package nl.jkool.beerxmlviewer

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text2.BasicSecureTextField
import androidx.compose.foundation.text2.input.TextFieldState
import androidx.compose.foundation.text2.input.TextObfuscationMode
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.sauronsoftware.ftp4j.FTPClient
import it.sauronsoftware.ftp4j.FTPException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nl.jkool.beerxmlviewer.ui.theme.BeerXMLViewerTheme
import org.json.JSONObject
import org.json.XML
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun storeSettings(context: Context, site: String, path: String, username: String, password: String, fullInfo: Boolean) {
    val inputPath = if (path == "") "/" else path
    val fullInfoStr = when (fullInfo) {
        true -> "true"
        false -> "false"
    }
    val jsObject = JSONObject()
        .put("site", site)
        .put("path", inputPath)
        .put("username", username)
        .put("password", password)
        .put("fullInfo", fullInfoStr)
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

fun quickObtainFile(activity: MainActivity, context: Context){
    val settings = getSettings(context)
    val siteNull = settings.get("site")
    val pathNull = settings.get("path")
    val usernameNull = settings.get("username")
    val passwordNull = settings.get("password")
    if (
        siteNull == null ||
        pathNull == null ||
        usernameNull == null ||
        passwordNull == null
        ) {
        Toast.makeText(context, "Unable to sync, some required setting is not set.", Toast.LENGTH_LONG).show()
        return
    } else {
        val site = siteNull.toString()
        val path = pathNull.toString()
        val username = usernameNull.toString()
        val password = passwordNull.toString()
        Thread {
            obtainFile(
                activity,
                context,
                site,
                path,
                username,
                password
            )
        }.start()
    }
}

fun obtainFile(activity: MainActivity, context: Context, site: String, path: String, username: String, password: String) {
    val mFTPClient = FTPClient()
    activity.runOnUiThread {
        val message = "Start obtaining data from FTP server, this can take a while…"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    try {
        mFTPClient.connect(site, 21)
        mFTPClient.login(username, password)
        mFTPClient.type = FTPClient.TYPE_BINARY
        mFTPClient.isPassive = true
        mFTPClient.noop()
        mFTPClient.changeDirectory(path)

        try {
            mFTPClient.download("hops.xml", File(context.applicationInfo.dataDir + "/hops.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/hops.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val hops = jsonToHopsObject(jsonObj)
            hops.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain hops.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("brews.xml", File(context.applicationInfo.dataDir + "/brews.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/brews.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val brews = jsonToBrewsObject(jsonObj)
            brews.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain brews.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("equipments.xml", File(context.applicationInfo.dataDir + "/equipments.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/equipments.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val equipments = jsonToEquipmentsObject(jsonObj)
            equipments.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain equipments.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("fermentables.xml", File(context.applicationInfo.dataDir + "/fermentables.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/fermentables.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val fermentables = jsonToFermentablesObject(jsonObj)
            fermentables.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain fermentables.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("mashs.xml", File(context.applicationInfo.dataDir + "/mashs.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/mashs.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val mashs = jsonToMashsObject(jsonObj)
            mashs.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain mashs.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("miscs.xml", File(context.applicationInfo.dataDir + "/miscs.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/miscs.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val miscs = jsonToMiscsObject(jsonObj)
            miscs.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain miscs.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("recipes.xml", File(context.applicationInfo.dataDir + "/recipes.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/recipes.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val recipes = jsonToRecipesObject(jsonObj)
            recipes.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain recipes.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("styles.xml", File(context.applicationInfo.dataDir + "/styles.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/styles.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val styles = jsonToStylesObject(jsonObj)
            styles.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain styles.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("waters.xml", File(context.applicationInfo.dataDir + "/waters.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/waters.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val waters = jsonToWatersObject(jsonObj)
            waters.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain waters.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }

        try {
            mFTPClient.download("yeasts.xml", File(context.applicationInfo.dataDir + "/yeasts.xml"))
            val hopsFile =
                File(context.applicationInfo.dataDir + "/yeasts.xml").inputStream()
                    ?.bufferedReader()
            val inputString = hopsFile.use { it?.readText() }!!
            val jsonObj = XML.toJSONObject(inputString)
            val yeasts = jsonToYeastsObject(jsonObj)
            yeasts.store(context)
        } catch (e: Exception) {
            activity.runOnUiThread {
                Toast.makeText(context, "Failed to obtain yeasts.xml", Toast.LENGTH_LONG).show()
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            }
        }
        mFTPClient.disconnect(true)
        activity.runOnUiThread {
            Toast.makeText(context, "Finished downloading files from FTP server.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "You may have to restart the app.", Toast.LENGTH_LONG).show()
        }
    } catch (e: FTPException) {
        activity.runOnUiThread {
            Toast.makeText(context, "${e.message}", Toast.LENGTH_LONG).show()
        }
    } catch (e: UnknownHostException) {
        activity.runOnUiThread {
            Toast.makeText(context, "Could not connect to FTP server.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "Are you sure the web address is correct?", Toast.LENGTH_LONG).show()
        }
    } catch (e: SocketTimeoutException) {
        activity.runOnUiThread {
            Toast.makeText(context, "Connection timeout, server does not respond.", Toast.LENGTH_LONG).show()
        }
    }
    catch (e: Exception) {
        activity.runOnUiThread {
            Toast.makeText(context, "${e}", Toast.LENGTH_LONG).show()
        }
    }
}

fun getFullInfoSetting(context: Context) = getSettings(context).getOrDefault("fullInfo", "false") == "true"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(activity: MainActivity, context: Context) {
    val settings = getSettings(context)
    var site by remember {
        mutableStateOf(
            settings.getOrDefault("site", "")
        )
    }
    var path by remember {
        mutableStateOf(
            settings.getOrDefault("path", "/")
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
    var fullInfo by remember {
        mutableStateOf(
            settings.getOrDefault("fullInfo", "false") == "true"
        )
    }
    BackHandler(enabled = true, onBack = {
        activity.setContent {
            storeSettings(context, site, path, username, password, fullInfo)
            Main(activity, context)
        }
    })

    BeerXMLViewerTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Menu",
                            modifier = Modifier
                                .padding(start = 16.dp, end = 8.dp)
                                .clickable {
                                    activity.setContent {
                                        storeSettings(context, site, path, username, password, fullInfo)
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
                Text("FTP site, without the 'ftp://' prefix")

                Row {
                    TextField(
                        value = site,
                        onValueChange = { site = it },
                        label = { Text("ftp site") }
                    )
                }

                Text("Path, use '/' for root dir", modifier = Modifier.padding(top = 12.dp))
                TextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text("path") }
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
                    label = { Text("password") },
                    visualTransformation = PasswordVisualTransformation()
                )

                Box(
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Button(
                        onClick = {
                            Thread {
                                obtainFile(activity, context, site, path, username, password)
                            }.start()
                        }
                    ) {
                        Text(
                            text = "Obtain XML from ftp",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                Row(modifier = Modifier.padding(top = 32.dp)) {
                    Checkbox(
                        checked = fullInfo,
                        onCheckedChange = { state -> fullInfo = state }
                    )
                    Text(stringResource(R.string.fullInfo))
                }
            }
        }
    }
}