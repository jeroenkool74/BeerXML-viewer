package nl.jkool.beerxmlviewer

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import org.json.XML
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer

class Fermentables (
    val data: Any?
) {
    @Composable
    fun FermentablesList(innerPadding: PaddingValues, context: Context) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(innerPadding).padding(10.dp, 10.dp, 10.dp, 0.dp)
        ) {
            if (data != null) {
                ParseToComposable(data, "", context, topLayer = true)
            } else {
                Text("No file found. Open a BeerXML file with the button at bottom, or download via FTP in the settings.")
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
    try {
        fermentables = jsonToFermentablesObject(JSONObject(readInternalFile(context, "fermentables.json")))
    } catch (e: FileNotFoundException) {
        return fermentables
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
            return jsonToFermentablesObject(jsonObj)
        } catch (e: Exception) {
            return Fermentables(null)
        }
    }
}
