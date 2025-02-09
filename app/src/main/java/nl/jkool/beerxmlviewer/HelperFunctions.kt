package nl.jkool.beerxmlviewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject


fun JSONObject.toStringMap(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (key in keys()) {
        map.put(
            key.toString(),
            this.get(key.toString()).toString()
        )
    }
    return map.toMap()
}

fun JSONObject.isEmpty(): Boolean {
    for (i in this.keys()) {
        return false
    }
    return true
}

fun JSONObject.isOfLength(n: Int): Boolean {
    var count = 0
    for (i in this.keys()) {
        count += 1
        if (count > n) {
            return false
        }
    }
    return count == n
}

enum class ComposableOptions {
    ARRAY,
    OBJECT,
    STRING
}

fun choseComposableOption(jsonObject: JSONObject): ComposableOptions {
    if (jsonObject.isOfLength(1)) {
        try {
            for (key in jsonObject.keys()) {
                jsonObject.getJSONArray(key.toString())
                return ComposableOptions.ARRAY
            }
        } catch (e: Exception){
            return ComposableOptions.STRING
        }
    }
    return ComposableOptions.OBJECT
}

fun depthToColorId(depth: Int): Int =
    when (depth) {
        0 -> R.color.depth0
        1 -> R.color.depth1
        2 -> R.color.depth2
        3 -> R.color.depth3
        4 -> R.color.depth4
        else -> R.color.depth5
    }

@Composable
fun parseToComposable(anObject: Any, parent: String, depth: Int = 0, topLayer: Boolean = false){
    when (anObject) {
        is JSONArray -> {
            if (topLayer){
                val list = mutableListOf<JSONObject>()
                for (i in 0 until anObject.length()) {
                    list.add(anObject.getJSONObject(i))
                }
                LazyColumn(
                    //verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(list.sortedBy { it.getString("NAME") }) { item ->
                        parseToComposable(item, parent, depth)
                    }
                }
            }
            else {
                Column(
                    //verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in 0 until anObject.length()) {
                        parseToComposable(anObject.get(i), parent, depth)
                    }
                }
            }
        }
        is JSONObject -> {
            val name = try {
                    anObject.getString("NAME")
                } catch (e: Exception) {
                    parent
                }
            if (anObject.isOfLength(1)) {
                Column(modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 10.dp)) {
                    Text("${parent}:")
                    for (key in anObject.keys()) {
                        if (key != "NAME") {
                            Box(modifier = Modifier.padding(start = 10.dp)) {
                                parseToComposable(anObject.get(key.toString()), key.toString(), depth)
                            }
                        }
                    }

                }
            } else {
                Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                    var isExpanded by remember { mutableStateOf(false) }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colorResource(depthToColorId(depth)),
                        modifier = Modifier.clickable { isExpanded = !isExpanded }
                    ) {
                        Column(modifier = Modifier.padding(all = 10.dp)) {
                            if (isExpanded) {
                                Text(
                                    "$name ▶",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(all = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    "$name ▼",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(all = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            if (isExpanded) {
                                for (key in anObject.keys()) {
                                    if (key != "NAME") {
                                        parseToComposable(
                                            anObject.get(key.toString()),
                                            key.toString(),
                                            depth + 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else -> {
            parseText(parent, anObject.toString())
        }
    }
}


@Composable
fun parseText(key: String, value: String) {
    Text(
        "${key}: $value",
        modifier = Modifier.padding(all = 4.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun mapCard(map: Map<String, String>) {

    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = MaterialTheme.shapes.medium,
        color = colorResource(R.color.depth0),
        modifier = Modifier.clickable { isExpanded = !isExpanded }
    ) {
        Column(modifier = Modifier.padding(all = 10.dp)) {
            val name = map.get("NAME")
            if (isExpanded) { Text("$name ▶",
                fontSize = 18.sp,
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.bodyMedium)
            }
            else { Text("$name ▼",
                fontSize =18.sp,
                modifier = Modifier.padding(all = 4.dp),
                style = MaterialTheme.typography.bodyMedium)
            }
            if (isExpanded) {
                for ((key, value) in map) {
                    if (key != "NAME") {
                        parseText(key, value)
                    }
                }
            }
        }
    }
}
