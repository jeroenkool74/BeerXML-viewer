package nl.jkool.beerxmlviewer

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import nl.jkool.beerxmlviewer.ui.theme.BeerXMLViewerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(activity: MainActivity, context: Context) {
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
            Text("todo", modifier = Modifier.padding(innerPadding))
        }
    }
}