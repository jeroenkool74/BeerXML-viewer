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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringArrayResource
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
        Toast.makeText(context, context.getString(R.string.something_went_wrong_period), Toast.LENGTH_LONG).show()
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

sealed class FtpDownloadStatus {
    data object Idle : FtpDownloadStatus()
    data class Running(
        val message: String,
        val completedFiles: Int = 0,
        val totalFiles: Int = FTP_DOWNLOAD_FILES.size,
        val currentFile: String? = null
    ) : FtpDownloadStatus()
    data class Success(val message: String) : FtpDownloadStatus()
    data class Warning(val message: String) : FtpDownloadStatus()
    data class Error(val message: String) : FtpDownloadStatus()
}

val FtpDownloadStatus.isRunning: Boolean
    get() = this is FtpDownloadStatus.Running

private data class FtpDownloadFile(
    val fileName: String,
    val parseAndStore: (String, Context) -> Unit
)

private val FTP_DOWNLOAD_FILES = listOf(
    FtpDownloadFile("hops.xml") { inputString, context ->
        jsonToHopsObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("brews.xml") { inputString, context ->
        jsonToBrewsObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("equipments.xml") { inputString, context ->
        jsonToEquipmentsObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("fermentables.xml") { inputString, context ->
        jsonToFermentablesObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("mashs.xml") { inputString, context ->
        jsonToMashsObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("miscs.xml") { inputString, context ->
        jsonToMiscsObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("recipes.xml") { inputString, context ->
        jsonToRecipesObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("styles.xml") { inputString, context ->
        jsonToStylesObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("waters.xml") { inputString, context ->
        jsonToWatersObject(beerXmlToJSONObject(inputString)).store(context)
    },
    FtpDownloadFile("yeasts.xml") { inputString, context ->
        jsonToYeastsObject(beerXmlToJSONObject(inputString)).store(context)
    }
)

fun quickObtainFile(
    activity: MainActivity,
    context: Context,
    onStatusChanged: (FtpDownloadStatus) -> Unit = {},
    onFinished: (FtpDownloadStatus) -> Unit = {}
) {
    val settings = getSettings(context)
    startFtpDownload(
        activity,
        context,
        settings["site"],
        settings["path"],
        settings["username"],
        settings["password"],
        onStatusChanged,
        onFinished
    )
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
    val host = hostAndPort.substringBefore(":").trim()
    val defaultPort = if (security == FTPClient.SECURITY_FTPS) 990 else 21
    val portText = hostAndPort.substringAfter(":", "").trim()
    val port = if (portText == "") {
        defaultPort
    } else {
        portText.toIntOrNull() ?: throw IllegalArgumentException()
    }

    return FtpServer(
        host = host,
        port = port,
        security = security,
        isPlainFtp = security == FTPClient.SECURITY_FTP
    )
}

internal fun ftpSettingsValidationError(
    site: String?,
    path: String?,
    username: String?,
    password: String?
): String? {
    if (site.isNullOrBlank()) return "FTP site is required."
    if (path.isNullOrBlank()) return "FTP path is required."
    if (username.isNullOrBlank()) return "FTP username is required."
    if (password.isNullOrBlank()) return "FTP password is required."

    val server = try {
        parseFtpServer(site)
    } catch (_: IllegalArgumentException) {
        return "FTP site is invalid."
    }
    if (server.host.isBlank() || server.host.any { it.isWhitespace() }) return "FTP site is invalid."
    if (server.port !in 1..65535) return "FTP port is invalid."

    return null
}

private fun ftpSettingsValidationErrorRes(
    site: String?,
    path: String?,
    username: String?,
    password: String?
): Int? {
    if (site.isNullOrBlank()) return R.string.ftp_site_required
    if (path.isNullOrBlank()) return R.string.ftp_path_required
    if (username.isNullOrBlank()) return R.string.ftp_username_required
    if (password.isNullOrBlank()) return R.string.ftp_password_required

    val server = try {
        parseFtpServer(site)
    } catch (_: IllegalArgumentException) {
        return R.string.ftp_site_invalid
    }
    if (server.host.isBlank() || server.host.any { it.isWhitespace() }) return R.string.ftp_site_invalid
    if (server.port !in 1..65535) return R.string.ftp_port_invalid

    return null
}

private fun normalizedFtpSite(site: String?): String = site?.let(::stripUrl).orEmpty()

private fun normalizedFtpPath(path: String?): String = path?.ifBlank { "/" }.orEmpty()

internal fun hasValidFtpSettings(
    site: String?,
    path: String?,
    username: String?,
    password: String?
): Boolean = ftpSettingsValidationError(
    normalizedFtpSite(site),
    normalizedFtpPath(path),
    username.orEmpty(),
    password.orEmpty()
) == null

private fun startFtpDownload(
    activity: MainActivity,
    context: Context,
    site: String?,
    path: String?,
    username: String?,
    password: String?,
    onStatusChanged: (FtpDownloadStatus) -> Unit,
    onFinished: (FtpDownloadStatus) -> Unit
) {
    val normalizedSite = normalizedFtpSite(site)
    val normalizedPath = normalizedFtpPath(path)
    val normalizedUsername = username.orEmpty()
    val normalizedPassword = password.orEmpty()
    val validationError = ftpSettingsValidationErrorRes(
        normalizedSite,
        normalizedPath,
        normalizedUsername,
        normalizedPassword
    )

    if (validationError != null) {
        val status = FtpDownloadStatus.Error(
            context.getString(R.string.ftp_unable_to_sync, context.getString(validationError))
        )
        onStatusChanged(status)
        onFinished(status)
        return
    }

    Thread {
        obtainFile(
            activity,
            context,
            normalizedSite,
            normalizedPath,
            normalizedUsername,
            normalizedPassword,
            onStatusChanged,
            onFinished
        )
    }.start()
}

private fun downloadFtpText(ftpClient: FTPClient, context: Context, fileName: String): String {
    val file = File(context.applicationInfo.dataDir, fileName)
    ftpClient.download(fileName, file)
    return file.inputStream().bufferedReader().use { it.readText() }
}

fun obtainFile(
    activity: MainActivity,
    context: Context,
    site: String,
    path: String,
    username: String,
    password: String,
    onStatusChanged: (FtpDownloadStatus) -> Unit = {},
    onFinished: (FtpDownloadStatus) -> Unit = {}
) {
    val ftpClient = FTPClient()
    fun report(status: FtpDownloadStatus, finished: Boolean = false) {
        activity.runOnUiThread {
            onStatusChanged(status)
            if (finished) {
                onFinished(status)
            }
        }
    }

    report(FtpDownloadStatus.Running(context.getString(R.string.ftp_connecting)))
    try {
        val server = parseFtpServer(site)
        val connectionMessage = if (server.isPlainFtp) {
            context.getString(R.string.ftp_connecting_plain_warning)
        } else {
            context.getString(R.string.ftp_connecting)
        }
        report(FtpDownloadStatus.Running(connectionMessage))

        ftpClient.security = server.security
        ftpClient.connect(server.host, server.port)
        ftpClient.login(username, password)
        ftpClient.type = FTPClient.TYPE_BINARY
        ftpClient.isPassive = true
        ftpClient.noop()
        ftpClient.changeDirectory(path)

        val failedFiles = mutableListOf<String>()
        FTP_DOWNLOAD_FILES.forEachIndexed { index, ftpFile ->
            report(
                FtpDownloadStatus.Running(
                    message = context.getString(R.string.ftp_downloading_file, ftpFile.fileName),
                    completedFiles = index,
                    currentFile = ftpFile.fileName
                )
            )
            try {
                val inputString = downloadFtpText(ftpClient, context, ftpFile.fileName)
                ftpFile.parseAndStore(inputString, context)
            } catch (_: Exception) {
                failedFiles.add(ftpFile.fileName)
            }
            report(
                FtpDownloadStatus.Running(
                    message = context.getString(R.string.ftp_downloaded_files, index + 1, FTP_DOWNLOAD_FILES.size),
                    completedFiles = index + 1,
                    currentFile = ftpFile.fileName
                )
            )
        }

        val finalStatus = if (failedFiles.isEmpty()) {
            FtpDownloadStatus.Success(context.getString(R.string.ftp_download_complete_message))
        } else {
            FtpDownloadStatus.Warning(
                context.getString(
                    R.string.ftp_download_partial_message,
                    FTP_DOWNLOAD_FILES.size - failedFiles.size,
                    FTP_DOWNLOAD_FILES.size,
                    failedFiles.joinToString()
                )
            )
        }
        report(finalStatus, finished = true)
    } catch (e: FTPException) {
        report(FtpDownloadStatus.Error(e.message ?: context.getString(R.string.ftp_could_not_connect)), finished = true)
    } catch (_: UnknownHostException) {
        report(FtpDownloadStatus.Error(context.getString(R.string.ftp_unknown_host)), finished = true)
    } catch (_: SocketTimeoutException) {
        report(FtpDownloadStatus.Error(context.getString(R.string.ftp_timeout)), finished = true)
    } catch (e: Exception) {
        report(FtpDownloadStatus.Error(e.message ?: context.getString(R.string.ftp_unable_to_sync_server)), finished = true)
    } finally {
        try {
            ftpClient.disconnect(true)
        } catch (_: Exception) {
        }
    }
}

fun getFullInfoSetting(context: Context) = getSettings(context).getOrDefault("fullInfo", "false") == "true"

@Composable
fun FtpDownloadStatusBanner(status: FtpDownloadStatus, modifier: Modifier = Modifier) {
    if (status == FtpDownloadStatus.Idle) return

    val containerColor = when (status) {
        is FtpDownloadStatus.Error -> MaterialTheme.colorScheme.errorContainer
        is FtpDownloadStatus.Warning -> MaterialTheme.colorScheme.secondaryContainer
        is FtpDownloadStatus.Success -> MaterialTheme.colorScheme.primaryContainer
        is FtpDownloadStatus.Running -> MaterialTheme.colorScheme.surfaceVariant
        FtpDownloadStatus.Idle -> MaterialTheme.colorScheme.surface
    }
    val title = when (status) {
        is FtpDownloadStatus.Error -> stringResource(R.string.ftp_download_failed_title)
        is FtpDownloadStatus.Warning -> stringResource(R.string.ftp_download_warning_title)
        is FtpDownloadStatus.Success -> stringResource(R.string.ftp_download_complete_title)
        is FtpDownloadStatus.Running -> stringResource(R.string.ftp_downloading_title)
        FtpDownloadStatus.Idle -> ""
    }
    val message = when (status) {
        is FtpDownloadStatus.Error -> status.message
        is FtpDownloadStatus.Warning -> status.message
        is FtpDownloadStatus.Success -> status.message
        is FtpDownloadStatus.Running -> status.message
        FtpDownloadStatus.Idle -> ""
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("ftpDownloadStatusBanner")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
            if (status is FtpDownloadStatus.Running) {
                Spacer(modifier = Modifier.height(8.dp))
                val progress = (status.completedFiles.toFloat() / status.totalFiles.coerceAtLeast(1)).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ftpDownloadProgress")
                )
                if (status.currentFile != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.ftp_files_complete, status.completedFiles, status.totalFiles),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun FtpDownloadHelpCard(modifier: Modifier = Modifier) {
    val expectedFiles = stringArrayResource(R.array.ftp_expected_files)
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.ftp_help_title),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.ftp_help_body),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.ftp_expected_files_title),
                style = MaterialTheme.typography.titleSmall
            )
            expectedFiles.forEach { fileName ->
                Text(
                    text = "- $fileName",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PrivacySummaryCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.privacy_title),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.privacy_summary),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

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
    var ftpDownloadStatus by remember { mutableStateOf<FtpDownloadStatus>(FtpDownloadStatus.Idle) }
    val ftpSettingsValid = hasValidFtpSettings(site, path, username, password)
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
                            contentDescription = stringResource(R.string.content_description_menu),
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
                        Text(text = stringResource(R.string.settings_title))
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
                FtpDownloadStatusBanner(
                    status = ftpDownloadStatus,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                FtpDownloadHelpCard(modifier = Modifier.padding(bottom = 12.dp))

                PrivacySummaryCard(modifier = Modifier.padding(bottom = 12.dp))

                Text(stringResource(R.string.ftp_site_description))

                Row {
                    TextField(
                        value = site,
                        onValueChange = { site = it },
                        label = { Text(stringResource(R.string.ftp_site_label)) },
                        modifier = Modifier.testTag("ftpSiteField")
                    )
                }

                Text(stringResource(R.string.ftp_path_description), modifier = Modifier.padding(top = 12.dp))
                TextField(
                    value = path,
                    onValueChange = { path = it },
                    label = { Text(stringResource(R.string.ftp_path_label)) },
                    modifier = Modifier.testTag("ftpPathField")
                )

                Text(stringResource(R.string.ftp_username_title), modifier = Modifier.padding(top = 12.dp))
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.ftp_username_label)) },
                    modifier = Modifier.testTag("ftpUsernameField")
                )

                Text(stringResource(R.string.ftp_password_title), modifier = Modifier.padding(top = 12.dp))
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
                    label = { Text(stringResource(R.string.ftp_password_label)) },
                    modifier = Modifier.testTag("ftpPasswordField"),
                    visualTransformation = LastCharRevealTransformation(revealUntil = revealUntil),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    )
                )

                Box(
                    modifier = Modifier.padding(top = 20.dp)
                ) {
                    Button(
                        enabled = ftpSettingsValid && !ftpDownloadStatus.isRunning,
                        modifier = Modifier.testTag("settingsFtpDownloadButton"),
                        onClick = {
                            storeSettings(context, site, path, username, password, fullInfo)
                            startFtpDownload(
                                activity,
                                context,
                                site,
                                path,
                                username,
                                password,
                                onStatusChanged = { ftpDownloadStatus = it },
                                onFinished = { status ->
                                    if (status is FtpDownloadStatus.Success || status is FtpDownloadStatus.Warning) {
                                        activity.setContent {
                                            Main(
                                                activity,
                                                context,
                                                initialFtpDownloadStatus = status
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    ) {
                        Text(
                            text = stringResource(R.string.obtain_xml_from_ftp),
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
