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

class Mashs (
    val data: Any?
) {
    @Composable
    fun mashsList(innerPadding: PaddingValues, context: Context) {
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
                val out = context.openFileOutput("mashs.json", Context.MODE_PRIVATE)
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
            return JSONObject().put("MASHS", JSONObject().put("MASH", data))
        } else {
            return null
        }
    }
}

fun jsonToMashsObject(input: JSONObject?): Mashs {
    if (input == null) {
        return  Mashs(null)
    } else {
        val jsonObject = input.getJSONObject("MASHS").get("MASH")
        return Mashs(jsonObject)
    }
}

fun loadMashs(context: Context): Mashs {
    var mashs = Mashs(null)
    var reader: BufferedReader? = null
    try {
        val `in` = context.openFileInput("mashs.json")
        reader = BufferedReader(InputStreamReader(`in`))
        val jsonObj2 = StringBuilder()
        for (line in reader.readLine()) {
            jsonObj2.append(line)
        }
        mashs = jsonToMashsObject(JSONObject(jsonObj2.toString()))
    } catch (e: FileNotFoundException) {
        return mashs
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
    }
    return mashs
}

fun xmlUriToMashs(uri: Uri?, context: Context): Mashs {
    if (uri == null) {
        return Mashs(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = XML.toJSONObject(inputString)
            //Toast.makeText(context, "Successfully loaded mashs from the xml file!", Toast.LENGTH_LONG).show()
            return jsonToMashsObject(jsonObj) as Mashs
        } catch (e: Exception) {
            //Toast.makeText(context, "Failed to load mashs from the xml file.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            return Mashs(null)
        }
    }
}