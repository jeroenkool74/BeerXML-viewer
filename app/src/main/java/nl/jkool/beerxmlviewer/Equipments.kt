package nl.jkool.beerxmlviewer

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import org.json.XML
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.Writer

class Equipments (
    val data: Any?
) {
    @Composable
    fun equipmentsList(innerPadding: PaddingValues) {
        Surface(
            modifier = Modifier.padding(innerPadding).padding(10.dp, 10.dp, 10.dp, 0.dp)
        ) {
            if (data != null) {
                parseToComposable(data, "", topLayer = true)
            } else {
                Text("Something went wrong, failed to load content.")
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
    var reader: BufferedReader? = null
    try {
        val `in` = context.openFileInput("equipments.json")
        reader = BufferedReader(InputStreamReader(`in`))
        val jsonObj2 = StringBuilder()
        for (line in reader.readLine()) {
            jsonObj2.append(line)
        }
        equipments = jsonToEquipmentsObject(JSONObject(jsonObj2.toString()))
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
            return jsonToEquipmentsObject(jsonObj) as Equipments
        } catch (e: Exception) {
            //Toast.makeText(context, "Failed to load equipments from the xml file.", Toast.LENGTH_LONG).show()
            Toast.makeText(context, "$e", Toast.LENGTH_LONG).show()
            return Equipments(null)
        }
    }
}