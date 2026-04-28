package nl.jkool.beerxmlviewer

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import it.sauronsoftware.ftp4j.FTPClient
import it.sauronsoftware.ftp4j.FTPException
import nl.jkool.beerxmlviewer.ui.theme.BeerXMLViewerTheme
import org.json.JSONObject
import org.json.XML
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private const val SETTINGS_PREFS_NAME = "encrypted_settings"
private const val LEGACY_SETTINGS_FILE_NAME = "settings.json"
private val SETTINGS_KEYS = listOf("site", "path", "username", "password", "fullInfo")

private fun settingsPreferences(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        SETTINGS_PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

private fun writeSettings(
    context: Context,
    site: String,
    path: String,
    username: String,
    password: String,
    fullInfo: Boolean
) {
    settingsPreferences(context).edit {
        putString("site", stripUrl(site))
        putString("path", if (path == "") "/" else path)
        putString("username", username)
        putString("password", password)
        putString("fullInfo", fullInfo.toString())
    }
}

fun storeSettings(context: Context, site: String, path: String, username: String, password: String, fullInfo: Boolean) {
    try {
        writeSettings(context, site, path, username, password, fullInfo)
        context.deleteFile(LEGACY_SETTINGS_FILE_NAME)
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
    }
}

private fun String.removePrefixIgnoreCase(prefix: String): String =
    if (startsWith(prefix, ignoreCase = true)) drop(prefix.length) else this

fun stripUrl(url: String): String =
    url.trim().removePrefixIgnoreCase("ftp://")

private fun legacySettings(context: Context): Map<String, String> =
    try {
        JSONObject(readInternalFile(context, LEGACY_SETTINGS_FILE_NAME)).toStringMap()
    } catch (_: Exception) {
        emptyMap()
    }

fun getSettings(context: Context): Map<String, String> {
    val prefs = try {
        settingsPreferences(context)
    } catch (_: Exception) {
        return legacySettings(context)
    }

    val settings = SETTINGS_KEYS.mapNotNull { key ->
        prefs.getString(key, null)?.let { value -> key to value }
    }.toMap()
    if (settings.isNotEmpty()) {
        return settings
    }

    val legacySettings = legacySettings(context)
    if (legacySettings.isNotEmpty()) {
        writeSettings(
            context,
            legacySettings.getOrDefault("site", ""),
            legacySettings.getOrDefault("path", "/"),
            legacySettings.getOrDefault("username", ""),
            legacySettings.getOrDefault("password", ""),
            legacySettings.getOrDefault("fullInfo", "false") == "true"
        )
        context.deleteFile(LEGACY_SETTINGS_FILE_NAME)
    }
    return legacySettings
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
        val site = siteNull
        val path = pathNull
        val username = usernameNull
        val password = passwordNull
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

private data class FtpServer(
    val host: String,
    val port: Int,
    val security: Int,
    val isPlainFtp: Boolean
)

private fun parseFtpServer(site: String): FtpServer {
    val trimmedSite = site.trim()
    val lowerSite = trimmedSite.lowercase()
    val security = when {
        lowerSite.startsWith("ftps://") -> FTPClient.SECURITY_FTPS
        lowerSite.startsWith("ftpes://") -> FTPClient.SECURITY_FTPES
        else -> FTPClient.SECURITY_FTP
    }
    val hostAndPort = trimmedSite.substringAfter("://", trimmedSite).substringBefore("/")
    val host = hostAndPort.substringBefore(":")
    val port = hostAndPort.substringAfter(":", "").toIntOrNull()
        ?: if (security == FTPClient.SECURITY_FTPS) 990 else 21

    return FtpServer(
        host = host,
        port = port,
        security = security,
        isPlainFtp = security == FTPClient.SECURITY_FTP
    )
}

private fun downloadFtpText(ftpClient: FTPClient, context: Context, fileName: String): String {
    val file = File(context.applicationInfo.dataDir, fileName)
    ftpClient.download(fileName, file)
    return file.inputStream().bufferedReader().use { it.readText() }
}

fun obtainFile(activity: MainActivity, context: Context, site: String, path: String, username: String, password: String) {
    val mFTPClient = FTPClient()
    activity.runOnUiThread {
        val message = "Start obtaining data from FTP server, this can take a while…"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    try {
        val server = parseFtpServer(site)
        if (server.isPlainFtp) {
            activity.runOnUiThread {
                Toast.makeText(context, "Plain FTP is not encrypted. Use ftpes:// or ftps:// if your server supports it.", Toast.LENGTH_LONG).show()
            }
        }
        mFTPClient.security = server.security
        mFTPClient.connect(server.host, server.port)
        mFTPClient.login(username, password)
        mFTPClient.type = FTPClient.TYPE_BINARY
        mFTPClient.isPassive = true
        mFTPClient.noop()
        mFTPClient.changeDirectory(path)

        try {
            val inputString = downloadFtpText(mFTPClient, context, "hops.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "brews.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "equipments.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "fermentables.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "mashs.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "miscs.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "recipes.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "styles.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "waters.xml")
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
            val inputString = downloadFtpText(mFTPClient, context, "yeasts.xml")
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
                Text("FTP site. Use ftpes:// or ftps:// for encrypted connections.")

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
//                BasicSecureTextField(
//                    value = password,
//                    onValueChange = { password = it },
//                    textObfuscationMode = TextObfuscationMode.RevealLastTyped,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(6.dp)
//                        .border(1.dp, Color.LightGray, RoundedCornerShape(6.dp))
//                        .padding(6.dp),
//                )
                var revealUntil by remember { mutableLongStateOf(0L) }
                TextField(
                    value = password,
                    onValueChange = {
                        if (it.length > password.length) {
                            revealUntil = System.currentTimeMillis() + 1000L // show last char for 1s
                        } else {
                            revealUntil = 0L
                        }
                        password = it
                    },
                    label = { Text("password") },
                    visualTransformation = LastCharRevealTransformation(revealUntil = revealUntil),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    )
                )

                Box(
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Button(
                        onClick = {
                            Thread {
                                obtainFile(activity, context, stripUrl(site), path, username, password)
                            }.start()
                        }
                    ) {
                        Text(
                            text = "Obtain XML from FTP/FTPS",
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

class LastCharRevealTransformation(
    private val maskChar: Char = '•',
    private val revealUntil: Long = 0L // Unix time in millis
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val now = System.currentTimeMillis()
        val transformed = if (text.isEmpty()) {
            ""
        } else if (now <= revealUntil) {
            text.text.dropLast(1).map { maskChar }.joinToString("") + text.text.last()
        } else {
            text.text.map { maskChar }.joinToString("")
        }
        return TransformedText(AnnotatedString(transformed), OffsetMapping.Identity)
    }
}
