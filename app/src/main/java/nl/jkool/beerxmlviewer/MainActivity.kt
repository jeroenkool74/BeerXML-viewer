package nl.jkool.beerxmlviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import nl.jkool.beerxmlviewer.ui.theme.BeerXMLViewerTheme


val objectToCode: Map<String, Int> =
    mapOf(
        "Hop" to 1,
        "Fermentable" to 2,
        "Yeast" to 3,
        "Misc" to 4,
        "Water" to 5,
        "Equipment" to 6,
        "Style" to 7,
        "Mash" to 8,
        "Recipe" to 9,
        "Brew" to 10,
    )

val codeToObject: Map<Int, String> =
    objectToCode.map{ it.value to it.key }.toMap()

private fun objectNameRes(code: Int): Int =
    when (code) {
        objectToCode["Hop"]!! -> R.string.nav_hop
        objectToCode["Fermentable"]!! -> R.string.nav_fermentable
        objectToCode["Yeast"]!! -> R.string.nav_yeast
        objectToCode["Misc"]!! -> R.string.nav_misc
        objectToCode["Water"]!! -> R.string.nav_water
        objectToCode["Equipment"]!! -> R.string.nav_equipment
        objectToCode["Style"]!! -> R.string.nav_style
        objectToCode["Mash"]!! -> R.string.nav_mash
        objectToCode["Recipe"]!! -> R.string.nav_recipe
        objectToCode["Brew"]!! -> R.string.nav_brew
        else -> R.string.app_name
    }


@Suppress("DEPRECATION")
open class MainActivity : ComponentActivity() {

    fun openFile(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }

        startActivityForResult(intent, requestCode)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setInitialContent()
    }

    protected open fun setInitialContent() {
        setContent {
            Main(this, applicationContext)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            importXmlFile(requestCode, data)
        }
    }

    private fun importXmlFile(requestCode: Int, resultData: Intent?) {
        val uri = resultData?.data ?: return
        Thread {
            val stored = when (requestCode) {
                objectToCode["Hop"] -> xmlUriToHops(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Fermentable"] -> xmlUriToFermentables(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Yeast"] -> xmlUriToYeasts(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Misc"] -> xmlUriToMiscs(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Water"] -> xmlUriToWaters(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Equipment"] -> xmlUriToEquipments(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Style"] -> xmlUriToStyles(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Mash"] -> xmlUriToMashs(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Recipe"] -> xmlUriToRecipes(uri, applicationContext).also { it.store(applicationContext) }.data != null
                objectToCode["Brew"] -> xmlUriToBrews(uri, applicationContext).also { it.store(applicationContext) }.data != null
                else -> null
            }
            runOnUiThread {
                when (stored) {
                    null -> Toast.makeText(applicationContext, getString(R.string.missing_request_code), Toast.LENGTH_LONG).show()
                    true -> {
                        setContent {
                            Main(this@MainActivity, applicationContext, requestCode)
                        }
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.stored_file, getString(objectNameRes(requestCode))),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    false -> Toast.makeText(
                        applicationContext,
                        getString(R.string.failed_to_load_file, getString(objectNameRes(requestCode))),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(
    activity: MainActivity,
    context: Context,
    initView: Int = 1,
    initialFtpDownloadStatus: FtpDownloadStatus = FtpDownloadStatus.Idle
) {
    BeerXMLViewerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var navState by rememberSaveable { mutableIntStateOf(initView) }
        var refreshKey by remember { mutableIntStateOf(0) }
        var ftpDownloadStatus by remember { mutableStateOf(initialFtpDownloadStatus) }
        val settings = getSettings(context)
        val fullInfo = settings.getOrDefault("fullInfo", "false") == "true"
        val ftpSettingsValid = hasValidFtpSettings(
            settings["site"],
            settings["path"],
            settings["username"],
            settings["password"]
        )
        val stateHistory = rememberSaveable { mutableListOf<Int>() }
        BackHandler(enabled = true, onBack = {
            when {
                (drawerState.isOpen) -> scope.launch { drawerState.close() }

                stateHistory.isNotEmpty() -> navState = stateHistory.removeAt(stateHistory.lastIndex)

                else -> activity.moveTaskToBack(true)
            }
        })
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_hop),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Hop"]),
                        onClick = {
                            val newState = objectToCode["Hop"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_fermentable),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Fermentable"]),
                        onClick = {
                            val newState = objectToCode["Fermentable"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_yeast),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Yeast"]),
                        onClick = {
                            val newState = objectToCode["Yeast"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_misc),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Misc"]),
                        onClick = {
                            val newState = objectToCode["Misc"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_water),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Water"]),
                        onClick = {
                            val newState = objectToCode["Water"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_equipment),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Equipment"]),
                        onClick = {
                            val newState = objectToCode["Equipment"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_style),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Style"]),
                        onClick = {
                            val newState = objectToCode["Style"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_mash),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Mash"]),
                        onClick = {
                            val newState = objectToCode["Mash"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_recipe),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Recipe"]),
                        onClick = {
                            val newState = objectToCode["Recipe"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = stringResource(R.string.nav_brew),
                            modifier = Modifier.padding(16.dp)
                        ) },
                        selected = (navState == objectToCode["Brew"]),
                        onClick = {
                            val newState = objectToCode["Brew"]!!
                            if (navState != newState) {
                                stateHistory.add(navState)
                            }
                            navState = newState
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = stringResource(R.string.content_description_menu),
                                modifier = Modifier.padding(start = 16.dp, end = 8.dp).clickable {
                                    scope.launch {
                                        drawerState.apply {
                                            if (isClosed) open() else close()
                                        }
                                    }
                                }
                            )
                        },
                        title = {
                            Text(text = stringResource(objectNameRes(navState)))
                        },
                        actions = {
                            Button(
                                enabled = ftpSettingsValid && !ftpDownloadStatus.isRunning,
                                modifier = Modifier.testTag("mainFtpDownloadButton"),
                                onClick = {
                                    quickObtainFile(
                                        activity,
                                        context,
                                        onStatusChanged = { ftpDownloadStatus = it },
                                        onFinished = { refreshKey++ }
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.download_24px),
                                    contentDescription = stringResource(R.string.content_description_download_from_ftp)
                                )
                                Text(stringResource(R.string.download_from_ftp))
                            }
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.content_description_settings),
                                modifier = Modifier.padding(start = 8.dp, end = 16.dp).clickable{
                                    activity.setContent {
                                        Settings(activity, context)
                                    }
                                }
                            )
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        activity.openFile(navState)
                    }) {
                        Row(modifier = Modifier.padding(10.dp)) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.folder_open_24dp_e8eaed_fill0_wght400_grad0_opsz24),
                                contentDescription = stringResource(R.string.content_description_open_xml_file)
                            )
                            Text(stringResource(R.string.open_xml_file), modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                },
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    FtpDownloadStatusBanner(
                        status = ftpDownloadStatus,
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 12.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        val contentPadding = PaddingValues(0.dp)
                        when (navState) {
                            objectToCode["Hop"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadHops(context)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, context.getString(R.string.failed_to_load_hops), Toast.LENGTH_LONG).show()
                                        Hops(null)
                                    }
                                }
                                view.HopsList(contentPadding, context)
                            }
                            objectToCode["Fermentable"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadFermentables(context)
                                    } catch (e: Exception) {
                                        Fermentables(null)
                                    }
                                }
                                view.FermentablesList(contentPadding, context)
                            }
                            objectToCode["Yeast"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadYeasts(context)
                                    } catch (e: Exception) {
                                        Yeasts(null)
                                    }
                                }
                                view.YeastsList(contentPadding, context)
                            }
                            objectToCode["Misc"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadMiscs(context)
                                    } catch (e: Exception) {
                                        Miscs(null)
                                    }
                                }
                                view.MiscsList(contentPadding, context)
                            }
                            objectToCode["Water"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadWaters(context)
                                    } catch (e: Exception) {
                                        Waters(null)
                                    }
                                }
                                view.WatersList(contentPadding, context)
                            }
                            objectToCode["Equipment"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadEquipments(context)
                                    } catch (e: Exception) {
                                        Equipments(null)
                                    }
                                }
                                view.EquipmentsList(contentPadding, context)
                            }
                            objectToCode["Style"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadStyles(context)
                                    } catch (e: Exception) {
                                        Styles(null)
                                    }
                                }
                                view.StylesList(contentPadding, context)
                            }
                            objectToCode["Mash"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadMashs(context)
                                    } catch (e: Exception) {
                                        Mashs(null)
                                    }
                                }
                                view.MashsList(contentPadding, context)
                            }
                            objectToCode["Recipe"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadRecipes(context)
                                    } catch (e: Exception) {
                                        Recipes(null)
                                    }
                                }
                                view.RecipesList(
                                    contentPadding,
                                    context,
                                    fullInfo
                                )
                            }
                            objectToCode["Brew"] -> {
                                val view = remember(refreshKey) {
                                    try {
                                        loadBrews(context)
                                    } catch (e: Exception) {
                                        Brews(null)
                                    }
                                }
                                view.BrewsList(contentPadding, context, fullInfo)
                            }
                            else -> Text(stringResource(R.string.something_went_wrong))
                        }
                    }
                }
            }
        }
    }
}
