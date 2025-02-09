package nl.jkool.beerxmlviewer

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import org.json.XML
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer

class Brews (
    val data: Any?
) {
    @Composable
    fun brewsList(innerPadding: PaddingValues) {
        Surface(
            modifier = Modifier.padding(innerPadding).padding(10.dp, 10.dp, 10.dp, 0.dp)
        ) {
            if (data != null) {
                parseToComposable(data, "", topLayer = true)
            } else {
                Text("Something went wrong, failed to load content.")
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
    var reader: BufferedReader? = null
    try {
        val `in` = context.openFileInput("brews.json")
        reader = BufferedReader(InputStreamReader(`in`))
        val jsonObj2 = StringBuilder()
        for (line in reader.readLine()) {
            jsonObj2.append(line)
        }
        brews = jsonToBrewsObject(JSONObject(jsonObj2.toString()))
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
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
            val jsonObj = XML.toJSONObject(inputString)
            //Toast.makeText(context, "Successfully loaded brews from the xml file!", Toast.LENGTH_LONG).show()
            return jsonToBrewsObject(jsonObj) as Brews
        } catch (e: Exception) {
            //Toast.makeText(context, "Failed to load brews from the xml file.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            return Brews(null)
        }
    }
}