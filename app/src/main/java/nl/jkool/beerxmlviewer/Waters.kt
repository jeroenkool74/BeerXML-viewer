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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer

class Waters (
    val data: Any?
) {
    @Composable
    fun WatersList(innerPadding: PaddingValues, context: Context) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(innerPadding).padding(10.dp, 10.dp, 10.dp, 0.dp)
        ) {
            if (data != null) {
                ParseToComposable(data, "", context, topLayer = true)
            } else {
                Text(stringResource(R.string.no_file_found))
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
    try {
        waters = jsonToWatersObject(JSONObject(readInternalFile(context, "waters.json")))
    } catch (e: FileNotFoundException) {
        return waters
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.something_went_wrong_period), Toast.LENGTH_LONG).show()
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
            val jsonObj = beerXmlToJSONObject(inputString)
            return jsonToWatersObject(jsonObj)
        } catch (e: Exception) {
            return Waters(null)
        }
    }
}
