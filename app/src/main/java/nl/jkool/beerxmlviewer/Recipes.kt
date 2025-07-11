package nl.jkool.beerxmlviewer

import android.app.AsyncNotedAppOp
import android.content.Context
import android.graphics.drawable.Icon
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.XML
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer

class Recipes (
    val data: Any?
) {
    @Composable
    fun recipesList(innerPadding: PaddingValues, context: Context, fullInfo: Boolean) {
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
                    if (sorted) {
                        Row(modifier = Modifier.padding(bottom = 16.dp, start = 10.dp).clickable { changeSorting() }) {
                            Icon(
                                painter = painterResource(R.drawable.sort_by_alpha_24px),
                                contentDescription = "Sort by name"
                            )
                            Text (
                                " Sort by name"
                            )
                        }
                    } else {
                        Row(modifier = Modifier.padding(bottom = 16.dp, start = 10.dp).clickable { changeSorting() }) {
                            Icon(
                                painter = painterResource(R.drawable.swap_vert_24px),
                                contentDescription = "Sort by beer style"
                            )
                            Text(
                                " Sort by beer style"
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
                                o.getJSONObject("STYLE").getString("NAME")
                            })
                        sorted && !fullInfo -> briefRecipeView(data, context, groupByString = { o: JSONObject ->
                            o.getJSONObject("STYLE").getString("NAME")
                        })
                        !sorted && fullInfo ->
                            ParseToComposable(
                                data,
                                "",
                                context,
                                topLayer = true
                            )
                        !sorted && !fullInfo -> briefRecipeView(data, context)
                    }
                } else {
                    Text("Something went wrong, failed to load content.")
                }
            }
        }
    }

    fun store(context: Context) {
        if (data != null) {
            var writer: Writer? = null
            try {
                val out = context.openFileOutput("recipes.json", Context.MODE_PRIVATE)
                writer = OutputStreamWriter(out)
                writer.write(toJSON().toString())
            } catch (e: Exception) {
                Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
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

fun jsonToRecipesObject(input: JSONObject?): Recipes {
    if (input == null) {
        return  Recipes(null)
    } else {
        val jsonObject = input.getJSONObject("RECIPES").get("RECIPE")
        return Recipes(jsonObject)
    }
}

fun loadRecipes(context: Context): Recipes {
    var recipes = Recipes(null)
    var reader: BufferedReader? = null
    try {
        val `in` = context.openFileInput("recipes.json")
        reader = BufferedReader(InputStreamReader(`in`))
        val jsonObj2 = StringBuilder()
        for (line in reader.readLine()) {
            jsonObj2.append(line)
        }
        recipes = jsonToRecipesObject(JSONObject(jsonObj2.toString()))
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
    }
    return recipes
}

fun xmlUriToRecipes(uri: Uri?, context: Context): Recipes {
    if (uri == null) {
        return Recipes(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = XML.toJSONObject(inputString)
            //Toast.makeText(context, "Successfully loaded recipes from the xml file!", Toast.LENGTH_LONG).show()
            return jsonToRecipesObject(jsonObj) as Recipes
        } catch (e: Exception) {
            //Toast.makeText(context, "Failed to load recipes from the xml file.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            return Recipes(null)
        }
    }
}