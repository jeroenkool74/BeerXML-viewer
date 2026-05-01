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

class Hops (
    val data: Any?
) {
    @Composable
    fun HopsList(innerPadding: PaddingValues, context: Context) {
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
                val out = context.openFileOutput("hops.json", Context.MODE_PRIVATE)
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
            return JSONObject().put("HOPS", JSONObject().put("HOP", data))
        } else {
            return null
        }
    }
}

fun jsonToHopsObject(input: JSONObject?): Hops {
    if (input == null) {
        return  Hops(null)
    } else {
        val jsonObject = input.getJSONObject("HOPS").get("HOP")
        return Hops(jsonObject)
    }
}

fun loadHops(context: Context): Hops {
    var hops = Hops(null)
    try {
        hops = jsonToHopsObject(JSONObject(readInternalFile(context, "hops.json")))
    } catch (e: FileNotFoundException) {
        return hops
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.something_went_wrong_period), Toast.LENGTH_LONG).show()
    }
    return hops
}

fun xmlUriToHops(uri: Uri?, context: Context): Hops {
    if (uri == null) {
        return Hops(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = beerXmlToJSONObject(inputString)
            return jsonToHopsObject(jsonObj)
        } catch (e: Exception) {
            return Hops(null)
        }
    }
}
