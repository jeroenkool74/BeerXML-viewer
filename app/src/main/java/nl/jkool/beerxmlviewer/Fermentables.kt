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

class Fermentables (
    val data: Any?
) {
    @Composable
    fun fermentablesList(innerPadding: PaddingValues) {
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
                val out = context.openFileOutput("fermentables.json", Context.MODE_PRIVATE)
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
            return JSONObject().put("FERMENTABLES", JSONObject().put("FERMENTABLE", data))
        } else {
            return null
        }
    }
}

fun jsonToFermentablesObject(input: JSONObject?): Fermentables {
    if (input == null) {
        return  Fermentables(null)
    } else {
        val jsonObject = input.getJSONObject("FERMENTABLES").get("FERMENTABLE")
        return Fermentables(jsonObject)
    }
}

fun loadFermentables(context: Context): Fermentables {
    var fermentables = Fermentables(null)
    var reader: BufferedReader? = null
    try {
        val `in` = context.openFileInput("fermentables.json")
        reader = BufferedReader(InputStreamReader(`in`))
        val jsonObj2 = StringBuilder()
        for (line in reader.readLine()) {
            jsonObj2.append(line)
        }
        fermentables = jsonToFermentablesObject(JSONObject(jsonObj2.toString()))
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
    }
    return fermentables
}

fun xmlUriToFermentables(uri: Uri?, context: Context): Fermentables {
    if (uri == null) {
        return Fermentables(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = XML.toJSONObject(inputString)
            //Toast.makeText(context, "Successfully loaded fermentables from the xml file!", Toast.LENGTH_LONG).show()
            return jsonToFermentablesObject(jsonObj) as Fermentables
        } catch (e: Exception) {
            //Toast.makeText(context, "Failed to load fermentables from the xml file.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            return Fermentables(null)
        }
    }
}