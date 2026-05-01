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

class Styles (
    val data: Any?
) {
    @Composable
    fun StylesList(innerPadding: PaddingValues, context: Context) {
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
                val out = context.openFileOutput("styles.json", Context.MODE_PRIVATE)
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
            return JSONObject().put("STYLES", JSONObject().put("STYLE", data))
        } else {
            return null
        }
    }
}

fun jsonToStylesObject(input: JSONObject?): Styles {
    if (input == null) {
        return  Styles(null)
    } else {
        val jsonObject = input.getJSONObject("STYLES").get("STYLE")
        return Styles(jsonObject)
    }
}

fun loadStyles(context: Context): Styles {
    var styles = Styles(null)
    try {
        styles = jsonToStylesObject(JSONObject(readInternalFile(context, "styles.json")))
    } catch (e: FileNotFoundException) {
        return styles
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.something_went_wrong_period), Toast.LENGTH_LONG).show()
    }
    return styles
}

fun xmlUriToStyles(uri: Uri?, context: Context): Styles {
    if (uri == null) {
        return Styles(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = beerXmlToJSONObject(inputString)
            return jsonToStylesObject(jsonObj)
        } catch (e: Exception) {
            return Styles(null)
        }
    }
}
