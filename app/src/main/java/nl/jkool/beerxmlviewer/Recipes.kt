package nl.jkool.beerxmlviewer

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    fun recipesList(innerPadding: PaddingValues) {
        Surface(
            modifier = Modifier.padding(innerPadding).padding(10.dp, 10.dp, 10.dp, 0.dp)
        ) {
            if (data != null) {
                ParseToComposable(data, "", topLayer = true)
            } else {
                Text("Something went wrong, failed to load content.")
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