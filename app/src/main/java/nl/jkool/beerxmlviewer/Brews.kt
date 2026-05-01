package nl.jkool.beerxmlviewer

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer

class Brews (
    val data: Any?
) {
    @Composable
    fun BrewsList(innerPadding: PaddingValues, context: Context, fullInfo: Boolean) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(innerPadding).padding(10.dp, 10.dp, 10.dp, 0.dp)
        ) {
            var sorted by remember { mutableStateOf( true ) }
            fun changeSorting() {
                if (sorted) {
                    sorted = false
                } else {
                    sorted = true
                }
            }
            Column {
                if (data != null) {
                    val unknownStyle = stringResource(R.string.unknown_style)
                    if (sorted) {
                        Row(modifier = Modifier.padding(bottom = 16.dp, start = 10.dp).clickable { changeSorting() }) {
                            Icon(
                                painter = painterResource(R.drawable.sort_by_alpha_24px),
                                contentDescription = stringResource(R.string.sort_by_name)
                            )
                            Text (
                                " ${stringResource(R.string.sort_by_name)}"
                            )
                        }
                    } else {
                        Row(modifier = Modifier.padding(bottom = 16.dp, start = 10.dp).clickable { changeSorting() }) {
                            Icon(
                                painter = painterResource(R.drawable.swap_vert_24px),
                                contentDescription = stringResource(R.string.sort_by_beer_style)
                            )
                            Text(
                                " ${stringResource(R.string.sort_by_beer_style)}"
                            )
                        }
                    }
                    when {
                        sorted && fullInfo -> ParseToComposable(
                            data,
                            "",
                            context,
                            topLayer = true,
                            groupByString = { o: JSONObject ->
                                o.styleName(unknownStyle)
                            })
                        sorted && !fullInfo -> BriefRecipeView(data, context, groupByString = { o: JSONObject ->
                            o.styleName(unknownStyle)
                        })
                        !sorted && fullInfo ->
                            ParseToComposable(
                                data,
                                "",
                                context,
                                topLayer = true
                            )
                        !sorted && !fullInfo -> BriefRecipeView(data, context)
                    }
                } else {
                    Text(stringResource(R.string.no_file_found))
                }
            }
        }
    }

    fun store(context: Context) {
        if (data != null) {
            var writer: Writer? = null
            try {
                val out = context.openFileOutput("brews.json", Context.MODE_PRIVATE)
                writer = OutputStreamWriter(out)
                writer.write(toJSON().toString())
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.something_went_wrong_period), Toast.LENGTH_LONG).show()
            } finally {
                if (writer != null) {
                    writer.close()
                }
            }
        }
    }

    fun toJSON(): JSONObject? {
        if (data != null){
            return JSONObject().put("RECIPES", JSONObject().put("RECIPE", data))
        } else {
            return null
        }
    }
}

fun jsonToBrewsObject(input: JSONObject?): Brews {
    if (input == null) {
        return  Brews(null)
    } else {
        val jsonObject = input.getJSONObject("RECIPES").get("RECIPE")
        return Brews(jsonObject)
    }
}

fun loadBrews(context: Context): Brews {
    var brews = Brews(null)
    try {
        brews = jsonToBrewsObject(JSONObject(readInternalFile(context, "brews.json")))
    } catch (e: FileNotFoundException) {
        return brews
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.something_went_wrong_period), Toast.LENGTH_LONG).show()
    }
    return brews
}

fun xmlUriToBrews(uri: Uri?, context: Context): Brews {
    if (uri == null) {
        return Brews(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = beerXmlToJSONObject(inputString)
            return jsonToBrewsObject(jsonObj)
        } catch (e: Exception) {
            return Brews(null)
        }
    }
}
