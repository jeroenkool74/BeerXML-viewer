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

class Waters (
    val data: Any?
) {
    @Composable
    fun watersList(innerPadding: PaddingValues) {
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
                val out = context.openFileOutput("waters.json", Context.MODE_PRIVATE)
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
            return JSONObject().put("WATERS", JSONObject().put("WATER", data))
        } else {
            return null
        }
    }
}

fun jsonToWatersObject(input: JSONObject?): Waters {
    if (input == null) {
        return  Waters(null)
    } else {
        val jsonObject = input.getJSONObject("WATERS").get("WATER")
        return Waters(jsonObject)
    }
}

fun loadWaters(context: Context): Waters {
    var waters = Waters(null)
    var reader: BufferedReader? = null
    try {
        val `in` = context.openFileInput("waters.json")
        reader = BufferedReader(InputStreamReader(`in`))
        val jsonObj2 = StringBuilder()
        for (line in reader.readLine()) {
            jsonObj2.append(line)
        }
        waters = jsonToWatersObject(JSONObject(jsonObj2.toString()))
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
    }
    return waters
}

fun xmlUriToWaters(uri: Uri?, context: Context): Waters {
    if (uri == null) {
        return Waters(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = XML.toJSONObject(inputString)
            //Toast.makeText(context, "Successfully loaded waters from the xml file!", Toast.LENGTH_LONG).show()
            return jsonToWatersObject(jsonObj) as Waters
        } catch (e: Exception) {
            //Toast.makeText(context, "Failed to load waters from the xml file.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            return Waters(null)
        }
    }
}