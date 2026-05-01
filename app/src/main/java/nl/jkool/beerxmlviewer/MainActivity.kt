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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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


@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {

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
                    null -> Toast.makeText(applicationContext, "Missing requestCode", Toast.LENGTH_LONG).show()
                    true -> {
                        setContent {
                            Main(this@MainActivity, applicationContext, requestCode)
                        }
                        Toast.makeText(applicationContext, "Stored ${codeToObject[requestCode] ?: "file"}", Toast.LENGTH_LONG).show()
                    }
                    false -> Toast.makeText(applicationContext, "Failed to load ${codeToObject[requestCode] ?: "file"}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(activity: MainActivity, context: Context, initView: Int = 1) {
    BeerXMLViewerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var navState by rememberSaveable { mutableIntStateOf(initView) }
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
                        text = "BeerXML viewer",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                    NavigationDrawerItem(
                        label = { Text (
                            text = "Hop",
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
                            text = "Fermentable",
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
                            text = "Yeast",
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
                            text = "Misc",
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
                            text = "Water",
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
                            text = "Equipment",
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
                            text = "Style",
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
                            text = "Mash",
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
                            text = "Recipe",
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
                            text = "Brews",
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
                                contentDescription = "Menu",
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
                            val pageTitle =
                                codeToObject[navState] ?: "BeerXML viewer"
                            Text(text = pageTitle)
                        },
                        actions = {
                            Button(
                                enabled = ftpSettingsValid,
                                modifier = Modifier.testTag("mainFtpDownloadButton"),
                                onClick = { quickObtainFile(activity, context) }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.download_24px),
                                    contentDescription = "Download from FTP"
                                )
                                Text("Download from FTP")
                            }
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
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
                                contentDescription = "add"
                            )
                            Text("Open XML file", modifier = Modifier.padding(start = 10.dp))
                        }
                    }
                },
            ) { innerPadding ->
                when (navState) {
                    objectToCode["Hop"] -> {
                        val view =
                            try {
                                loadHops(context)
                            } catch (e: Exception){
                                Toast.makeText(context, "Failed to load hops", Toast.LENGTH_LONG).show()
                                Hops(null)
                            }
                        view.HopsList(innerPadding, context)
                    }
                    objectToCode["Fermentable"] -> {
                        val view =
                            try {
                                loadFermentables(context)
                            } catch (e: Exception){
                                Fermentables(null)
                            }
                        view.FermentablesList(innerPadding, context)
                    }
                    objectToCode["Yeast"] -> {
                        val view =
                            try {
                                loadYeasts(context)
                            } catch (e: Exception){
                                Yeasts(null)
                            }
                        view.YeastsList(innerPadding, context)
                    }
                    objectToCode["Misc"] -> {
                        val view =
                            try {
                                loadMiscs(context)
                            } catch (e: Exception){
                                Miscs(null)
                            }
                        view.MiscsList(innerPadding, context)
                    }
                    objectToCode["Water"] -> {
                        val view =
                            try {
                                loadWaters(context)
                            } catch (e: Exception){
                                Waters(null)
                            }
                        view.WatersList(innerPadding, context)
                    }
                    objectToCode["Equipment"] -> {
                        val view =
                            try {
                                loadEquipments(context)
                            } catch (e: Exception){
                                Equipments(null)
                            }
                        view.EquipmentsList(innerPadding, context)
                    }
                    objectToCode["Style"] -> {
                        val view =
                            try {
                                loadStyles(context)
                            } catch (e: Exception){
                                Styles(null)
                            }
                        view.StylesList(innerPadding, context)
                    }
                    objectToCode["Mash"] -> {
                        val view =
                            try {
                                loadMashs(context)
                            } catch (e: Exception){
                                Mashs(null)
                            }
                        view.MashsList(innerPadding, context)
                    }
                    objectToCode["Recipe"] -> {
                        val view =
                            try {
                                loadRecipes(context)
                            } catch (e: Exception){
                                Recipes(null)
                            }
                        view.RecipesList(
                            innerPadding,
                            context,
                            fullInfo
                        )
                    }
                    objectToCode["Brew"] -> {
                        val view =
                            try {
                                loadBrews(context)
                            } catch (e: Exception){
                                Brews(null)
                            }
                        view.BrewsList(innerPadding, context, fullInfo)
                    }
                    else -> Text("Something went wrong", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
