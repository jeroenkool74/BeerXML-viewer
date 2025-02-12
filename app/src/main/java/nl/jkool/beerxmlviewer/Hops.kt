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

class Hops (
    val data: Any?
) {
    @Composable
    fun hopsList(innerPadding: PaddingValues) {
        /*
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding).padding(10.dp, 10.dp, 10.dp, 0.dp)
        ) {
            items(data.sortedBy { it.getString("NAME") }) { hop ->
                mapCard(hop.toStringMap())
            }
        }
         */
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
                val out = context.openFileOutput("hops.json", Context.MODE_PRIVATE)
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
    var reader: BufferedReader? = null
    try {
        val `in` = context.openFileInput("hops.json")
        reader = BufferedReader(InputStreamReader(`in`))
        val jsonObj2 = StringBuilder()
        for (line in reader.readLine()) {
            jsonObj2.append(line)
        }
        hops = jsonToHopsObject(JSONObject(jsonObj2.toString()))
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
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
            val jsonObj = XML.toJSONObject(inputString)
            //Toast.makeText(context, "Successfully loaded hops from the xml file!", Toast.LENGTH_LONG).show()
            return jsonToHopsObject(jsonObj) as Hops
        } catch (e: Exception) {
            //Toast.makeText(context, "Failed to load hops from the xml file.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            return Hops(null)
        }
    }
}