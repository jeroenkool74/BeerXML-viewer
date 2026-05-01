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

class Miscs (
    val data: Any?
) {
    @Composable
    fun MiscsList(innerPadding: PaddingValues, context: Context) {
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
                val out = context.openFileOutput("miscs.json", Context.MODE_PRIVATE)
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
            return JSONObject().put("MISCS", JSONObject().put("MISC", data))
        } else {
            return null
        }
    }
}

fun jsonToMiscsObject(input: JSONObject?): Miscs {
    if (input == null) {
        return  Miscs(null)
    } else {
        val jsonObject = input.getJSONObject("MISCS").get("MISC")
        return Miscs(jsonObject)
    }
}

fun loadMiscs(context: Context): Miscs {
    var miscs = Miscs(null)
    try {
        miscs = jsonToMiscsObject(JSONObject(readInternalFile(context, "miscs.json")))
    } catch (e: FileNotFoundException) {
        return miscs
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.something_went_wrong_period), Toast.LENGTH_LONG).show()
    }
    return miscs
}

fun xmlUriToMiscs(uri: Uri?, context: Context): Miscs {
    if (uri == null) {
        return Miscs(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = beerXmlToJSONObject(inputString)
            return jsonToMiscsObject(jsonObj)
        } catch (e: Exception) {
            return Miscs(null)
        }
    }
}
