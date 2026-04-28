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

class Equipments (
    val data: Any?
) {
    @Composable
    fun EquipmentsList(innerPadding: PaddingValues, context: Context) {
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
                val out = context.openFileOutput("equipments.json", Context.MODE_PRIVATE)
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
            return JSONObject().put("EQUIPMENTS", JSONObject().put("EQUIPMENT", data))
        } else {
            return null
        }
    }
}

fun jsonToEquipmentsObject(input: JSONObject?): Equipments {
    if (input == null) {
        return  Equipments(null)
    } else {
        val jsonObject = input.getJSONObject("EQUIPMENTS").get("EQUIPMENT")
        return Equipments(jsonObject)
    }
}

fun loadEquipments(context: Context): Equipments {
    var equipments = Equipments(null)
    try {
        equipments = jsonToEquipmentsObject(JSONObject(readInternalFile(context, "equipments.json")))
    } catch (e: FileNotFoundException) {
        return equipments
    } catch (e: Exception) {
        Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
    }
    return equipments
}

fun xmlUriToEquipments(uri: Uri?, context: Context): Equipments {
    if (uri == null) {
        return Equipments(null)
    } else {
        try {
            val bufferedReader = context.contentResolver.openInputStream(uri)?.bufferedReader()
            val inputString = bufferedReader.use { it?.readText() }
            val jsonObj = XML.toJSONObject(inputString)
            //Toast.makeText(context, "Successfully loaded equipments from the xml file!", Toast.LENGTH_LONG).show()
            return jsonToEquipmentsObject(jsonObj)
        } catch (e: Exception) {
            return Equipments(null)
        }
    }
}
