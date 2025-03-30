package nl.jkool.beerxmlviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import nl.jkool.beerxmlviewer.ui.theme.BeerXMLViewerTheme
import org.json.JSONObject


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
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (resultCode == RESULT_OK
        ) {
            when (requestCode) {
                objectToCode["Hop"] -> {
                    resultData?.data?.also { uri ->
                        val hops = xmlUriToHops(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                        Toast.makeText(applicationContext, "Stored hops", Toast.LENGTH_LONG).show()
                    }
                }
                objectToCode["Fermentable"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToFermentables(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                objectToCode["Yeast"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToYeasts(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                objectToCode["Misc"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToMiscs(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                objectToCode["Water"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToWaters(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                objectToCode["Equipment"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToEquipments(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                objectToCode["Style"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToStyles(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                objectToCode["Mash"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToMashs(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                objectToCode["Recipe"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToRecipes(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                objectToCode["Brew"] -> {
                    resultData?.data?.also { uri ->
                        xmlUriToBrews(uri, applicationContext).store(applicationContext)
                        setContent {
                            Main(this, applicationContext, requestCode)
                        }
                    }
                }
                else -> Toast.makeText(applicationContext, "Missing requestCode", Toast.LENGTH_LONG).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Main(activity: MainActivity, context: Context, initView: Int = 1) {
    BeerXMLViewerTheme {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var navState by rememberSaveable { mutableStateOf(1) }
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
                            navState = objectToCode["Hop"]!!
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
                            navState = objectToCode["Fermentable"]!!
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
                            navState = objectToCode["Yeast"]!!
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
                            navState = objectToCode["Misc"]!!
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
                            navState = objectToCode["Water"]!!
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
                            navState = objectToCode["Equipment"]!!
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
                            navState = objectToCode["Style"]!!
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
                            navState = objectToCode["Mash"]!!
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
                            navState = objectToCode["Recipe"]!!
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
                            navState = objectToCode["Brew"]!!
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
                            Button(onClick = { quickObtainFile(activity, context) }){
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
                        runBlocking {
                            activity.openFile(navState)
                        }
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
                        view.hopsList(innerPadding, context)
                    }
                    objectToCode["Fermentable"] -> {
                        val view =
                            try {
                                loadFermentables(context)
                            } catch (e: Exception){
                                Fermentables(null)
                            }
                        view.fermentablesList(innerPadding, context)
                    }
                    objectToCode["Yeast"] -> {
                        val view =
                            try {
                                loadYeasts(context)
                            } catch (e: Exception){
                                Yeasts(null)
                            }
                        view.yeastsList(innerPadding, context)
                    }
                    objectToCode["Misc"] -> {
                        val view =
                            try {
                                loadMiscs(context)
                            } catch (e: Exception){
                                Miscs(null)
                            }
                        view.miscsList(innerPadding, context)
                    }
                    objectToCode["Water"] -> {
                        val view =
                            try {
                                loadWaters(context)
                            } catch (e: Exception){
                                Waters(null)
                            }
                        view.watersList(innerPadding, context)
                    }
                    objectToCode["Equipment"] -> {
                        val view =
                            try {
                                loadEquipments(context)
                            } catch (e: Exception){
                                Equipments(null)
                            }
                        view.equipmentsList(innerPadding, context)
                    }
                    objectToCode["Style"] -> {
                        val view =
                            try {
                                loadStyles(context)
                            } catch (e: Exception){
                                Styles(null)
                            }
                        view.stylesList(innerPadding, context)
                    }
                    objectToCode["Mash"] -> {
                        val view =
                            try {
                                loadMashs(context)
                            } catch (e: Exception){
                                Mashs(null)
                            }
                        view.mashsList(innerPadding, context)
                    }
                    objectToCode["Recipe"] -> {
                        val view =
                            try {
                                loadRecipes(context)
                            } catch (e: Exception){
                                Recipes(null)
                            }
                        view.recipesList(
                            innerPadding,
                            context
                        )
                    }
                    objectToCode["Brew"] -> {
                        val view =
                            try {
                                loadBrews(context)
                            } catch (e: Exception){
                                Brews(null)
                            }
                        view.brewsList(innerPadding, context)
                    }
                    else -> Text("Something went wrong", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BeerXMLViewerTheme {
        Greeting("Android")
    }
}
