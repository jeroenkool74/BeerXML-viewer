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

class Yeasts (
    val data: Any?
) {
    @Composable
    fun YeastsList(innerPadding: PaddingValues, context: Context) {
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
                val out = context.openFileOutput("yeasts.json", Context.MODE_PRIVATE)
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
            return JSONObject().put("YEASTS", JSONObject().put("YEAST", data))
        } else {
            return null
        }
    }
}

fun jsonToYeastsObject(input: JSONObject?): Yeasts {
    if (input == null) {
        return  Yeasts(null)
    } else {
        val jsonObject = input.getJSONObject("YEASTS").get("YEAST")
        return Yeasts(jsonObject)
    }
}

fun loadYeasts(context: Context): Yeasts {
    var yeasts = Yeasts(null)
    try {
        yeasts = jsonToYeastsObject(JSONObject(readInternalFile(context, "yeasts.json")))
    } catch (e: FileNotFoundException) {
        return yeasts
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
    }
    return yeasts
}

fun xmlUriToYeasts(uri: Uri?, context: Context): Yeasts {
    if (uri == null) {
        return Yeasts(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = XML.toJSONObject(inputString)
            //Toast.makeText(context, "Successfully loaded yeasts from the xml file!", Toast.LENGTH_LONG).show()
            return jsonToYeastsObject(jsonObj)
        } catch (e: Exception) {
            return Yeasts(null)
        }
    }
}
