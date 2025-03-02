package nl.jkool.beerxmlviewer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
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
fun NAMEtoName(input: String): String {
    return when (input) {
        "NAME" -> stringResource(R.string.NAME)
        "NOTES" -> stringResource(R.string.NOTES)
        "VERSION" -> stringResource(R.string.VERSION)
        "ALWAYS_ON_STOCK" -> stringResource(R.string.ALWAYS_ON_STOCK)
        "ALPHA" -> stringResource(R.string.ALWAYS_ON_STOCK)
        "TYPE" -> stringResource(R.string.TYPE)
        "FORM" -> stringResource(R.string.FORM)
        "USE" -> stringResource(R.string.USE)
        "BETA" -> stringResource(R.string.BETA)
        "HSI" -> stringResource(R.string.HSI)
        "ORIGIN" -> stringResource(R.string.ORIGIN)
        "SUBSTITUTES" -> stringResource(R.string.SUBSTITUTES)
        "HUMULENE" -> stringResource(R.string.HUMULENE)
        "CAROPHYLLENE" -> stringResource(R.string.CAROPHYLLENE)
        "COHUMULONE" -> stringResource(R.string.COHUMULONE)
        "MYRCENE" -> stringResource(R.string.MYRCENE)
        "TOTAL_OIL" -> stringResource(R.string.TOTAL_OIL)
        "YIELD" -> stringResource(R.string.YIELD)
        "COLOR" -> stringResource(R.string.COLOR)
        "ADD_AFTER_BOIL" -> stringResource(R.string.ADD_AFTER_BOIL)
        "SUPPLIER" -> stringResource(R.string.SUPPLIER)
        "COARSE_FINE_DIFF" -> stringResource(R.string.COARSE_FINE_DIFF)
        "MOISTURE" -> stringResource(R.string.MOISTURE)
        "DIASTATIC_POWER" -> stringResource(R.string.DIASTATIC_POWER)
        "MAX_IN_BATCH" -> stringResource(R.string.MAX_IN_BATCH)
        "RECOMMEND_MASH" -> stringResource(R.string.RECOMMEND_MASH)
        "DISPLAY_COLOR" -> stringResource(R.string.DISPLAY_COLOR)
        "GRAINTYPE" -> stringResource(R.string.GRAINTYPE)
        "ADDED" -> stringResource(R.string.ADDED)
        "ADJUST_TO_TOTAL_100" -> stringResource(R.string.ADJUST_TO_TOTAL_100)
        "DI_pH" -> stringResource(R.string.DI_pH)
        "ACID_TO_pH" -> stringResource(R.string.ACID_TO_pH)
        "COST" -> stringResource(R.string.COST)
        "DISPLAY_COST" -> stringResource(R.string.DISPLAY_COST)
        "AMOUNT_IS_WEIGHT" -> stringResource(R.string.AMOUNT_IS_WEIGHT)
        "LABORATORY" -> stringResource(R.string.LABORATORY)
        "MIN_TEMPERATURE" -> stringResource(R.string.MIN_TEMPERATURE)
        "MAX_TEMPERATURE" -> stringResource(R.string.MAX_TEMPERATURE)
        "FLOCCULATION" -> stringResource(R.string.FLOCCULATION)
        "ATTENUATION" -> stringResource(R.string.ATTENUATION)
        "BEST_FOR" -> stringResource(R.string.BEST_FOR)
        "TIMES_CULTURED" -> stringResource(R.string.TIMES_CULTURED)
        "MAX_REUSE" -> stringResource(R.string.MAX_REUSE)
        "ADD_TO_SECONDARY" -> stringResource(R.string.ADD_TO_SECONDARY)
        "DISP_MIN_TEMP" -> stringResource(R.string.DISP_MIN_TEMP)
        "DISP_MAX_TEMP" -> stringResource(R.string.DISP_MAX_TEMP)
        "STARTER_TYPE" -> stringResource(R.string.STARTER_TYPE)
        "STARTER_MADE" -> stringResource(R.string.STARTER_MADE)
        "OG_STARTER" ->  stringResource(R.string.OG_STARTER)
        "ZINC_ADDED" -> stringResource(R.string.ZINC_ADDED)
        "TEMP" -> stringResource(R.string.TEMP)
        "TIME" -> stringResource(R.string.TIME)
        "USE_FOR" -> stringResource(R.string.USE_FOR)
        "CALCIUM" -> stringResource(R.string.CALCIUM)
        "BICARBONATE" -> stringResource(R.string.BICARBONATE)
        "SULFATE" -> stringResource(R.string.SULFATE)
        "CHLORIDE" -> stringResource(R.string.CHLORIDE)
        "MAGNESIUM" -> stringResource(R.string.MAGNESIUM)
        "PH" -> stringResource(R.string.PH)
        "TOTAL_ALKALINITY" -> stringResource(R.string.TOTAL_ALKALINITY)
        "DEFAULT_WATER" -> stringResource(R.string.DEFAULT_WATER)
        "BOIL_SIZE" -> stringResource(R.string.BOIL_SIZE)
        "BATCH_SIZE" -> stringResource(R.string.BATCH_SIZE)
        "TUN_VOLUME" -> stringResource(R.string.TUN_VOLUME)
        "TUN_WEIGHT" -> stringResource(R.string.TUN_WEIGHT)
        "TUN_SPECIFIC_HEAT" -> stringResource(R.string.TUN_SPECIFIC_HEAT)
        "TRUB_CHILLER_LOSS" -> stringResource(R.string.TRUB_CHILLER_LOSS)
        "EVAP_RATE" -> stringResource(R.string.EVAP_RATE)
        "BOIL_TIME" -> stringResource(R.string.BOIL_TIME)
        "CALC_BOIL_VOLUME" -> stringResource(R.string.CALC_BOIL_VOLUME)
        "LAUTER_DEADSPACE" -> stringResource(R.string.LAUTER_DEADSPACE)
        "HOP_UTILIZATION" -> stringResource(R.string.HOP_UTILIZATION)
        "DISPLAY_BOIL_SIZE" -> stringResource(R.string.DISPLAY_BOIL_SIZE)
        "DISPLAY_BATCH_SIZE" -> stringResource(R.string.DISPLAY_BATCH_SIZE)
        "DISPLAY_TUN_WEIGHT" -> stringResource(R.string.DISPLAY_TUN_WEIGHT)
        "DISPLAY_TRUB_CHILLER_LOSS" -> stringResource(R.string.DISPLAY_TRUB_CHILLER_LOSS)
        "LAUTER_VOLUME" -> stringResource(R.string.LAUTER_VOLUME)
        "KETTLE_HEIGHT" -> stringResource(R.string.KETTLE_HEIGHT)
        "LAUTER_HEIGHT" -> stringResource(R.string.LAUTER_HEIGHT)
        "MASH_VOLUME" -> stringResource(R.string.MASH_VOLUME)
        "EFFICIENCY" -> stringResource(R.string.EFFICIENCY)
        "ATTENUATION_FACTOR_YEAST" -> stringResource(R.string.ATTENUATION_FACTOR_YEAST)
        "ATTENUATION_FACTOR_WATER_TO_GRAIN_RATIO" -> stringResource(R.string.ATTENUATION_FACTOR_WATER_TO_GRAIN_RATIO)
        "ATTENUATION_FACTOR_TOTAL_MASH_TIME" -> stringResource(R.string.ATTENUATION_FACTOR_TOTAL_MASH_TIME)
        "ATTENUATION_FACTOR_PERC_SIMPLE_SUGAR" -> stringResource(R.string.ATTENUATION_FACTOR_PERC_SIMPLE_SUGAR)
        "STYLE_LETTER" -> stringResource(R.string.STYLE_LETTER)
        "OG_MIN" -> stringResource(R.string.OG_MIN)
        "OG_MAX" -> stringResource(R.string.OG_MAX)
        "FG_MIN" -> stringResource(R.string.FG_MIN)
        "FG_MAX" -> stringResource(R.string.FG_MAX)
        "IBU_MIN" -> stringResource(R.string.IBU_MIN)
        "IBU_MAX" -> stringResource(R.string.IBU_MAX)
        "COLOR_MIN" -> stringResource(R.string.COLOR_MIN)
        "COLOR_MAX" -> stringResource(R.string.COLOR_MAX)
        "CARB_MIN" -> stringResource(R.string.CARB_MIN)
        "CARB_MAX" -> stringResource(R.string.CARB_MAX)
        "ABV_MIN" -> stringResource(R.string.ABV_MIN)
        "ABV_MAX" -> stringResource(R.string.ABV_MAX)
        "DISPLAY_OG_MIN" -> stringResource(R.string.DISPLAY_OG_MIN)
        "DISPLAY_OG_MAX" -> stringResource(R.string.DISPLAY_OG_MAX)
        "DISPLAY_FG_MIN" -> stringResource(R.string.DISPLAY_FG_MIN)
        "DISPLAY_FG_MAX" -> stringResource(R.string.DISPLAY_FG_MAX)
        "DISPLAY_IBU_MIN" -> stringResource(R.string.DISPLAY_IBU_MIN)
        "DISPLAY_IBU_MAX" -> stringResource(R.string.DISPLAY_IBU_MAX)
        "DISPLAY_COLOR_MIN" -> stringResource(R.string.DISPLAY_COLOR_MIN)
        "DISPLAY_COLOR_MAX" -> stringResource(R.string.DISPLAY_COLOR_MAX)
        "DISPLAY_CARB_MIN" -> stringResource(R.string.DISPLAY_CARB_MIN)
        "DISPLAY_CARB_MAX" -> stringResource(R.string.DISPLAY_CARB_MAX)
        "DISPLAY_ABV_MIN" -> stringResource(R.string.DISPLAY_ABV_MIN)
        "DISPLAY_ABV_MAX" -> stringResource(R.string.DISPLAY_ABV_MAX)
        "OG_RANGE" -> stringResource(R.string.OG_RANGE)
        "FG_RANGE" -> stringResource(R.string.FG_RANGE)
        "IBU_RANGE" -> stringResource(R.string.IBU_RANGE)
        "CARB_RANGE" -> stringResource(R.string.CARB_RANGE)
        "COLOR_RANGE" -> stringResource(R.string.COLOR_RANGE)
        "ABV_RANGE" -> stringResource(R.string.ABV_RANGE)
        "GRAIN_TEMP" -> stringResource(R.string.GRAIN_TEMP)
        "TUN_TEMP" -> stringResource(R.string.TUN_TEMP)
        "SPARGE_TEMP" -> stringResource(R.string.SPARGE_TEMP)
        "SG_LAST_RUNNINGS" -> stringResource(R.string.SG_LAST_RUNNINGS)
        "PH_LAST_RUNNINGS" -> stringResource(R.string.PH_LAST_RUNNINGS)
        "TUN_SPECIFIC_HEATH" -> stringResource(R.string.TUN_SPECIFIC_HEATH)
        "EQUIP_ADJUST" -> stringResource(R.string.EQUIP_ADJUST)
        "INFUSE_AMOUNT" -> stringResource(R.string.INFUSE_AMOUNT)
        "STEP_TEMP" -> stringResource(R.string.STEP_TEMP)
        "STEP_TIME" -> stringResource(R.string.STEP_TIME)
        "RAMP_TIME" -> stringResource(R.string.RAMP_TIME)
        "END_TEMP" -> stringResource(R.string.END_TEMP)
        "WATER_GRAIN_RATIO" -> stringResource(R.string.WATER_GRAIN_RATIO)
        "DECOCTION_AMT" -> stringResource(R.string.DECOCTION_AMT)
        "DISPLAY_INFUSE_AMT" -> stringResource(R.string.DISPLAY_INFUSE_AMT)
        "DISPLAY_STEP_TEMP" -> stringResource(R.string.DISPLAY_STEP_TEMP)
        "HARVEST_DATE" -> stringResource(R.string.HARVEST_DATE)
        "PERCENTAGE" -> stringResource(R.string.PERCENTAGE)
        "PROTEIN" -> stringResource(R.string.PROTEIN)
        "STARTER_VOLUME" -> stringResource(R.string.STARTER_VOLUME)
        "AMOUNT_ZINC" -> stringResource(R.string.AMOUNT_ZINC)
        "AMOUNT_EXTRACT" -> stringResource(R.string.AMOUNT_EXTRACT)
        "COST_EXTRACT" -> stringResource(R.string.COST_EXTRACT)
        "PRODUCT_ID" -> stringResource(R.string.PRODUCT_ID)
        "AMOUNT" -> stringResource(R.string.AMOUNT)
        "DISPLAY_AMOUNT" -> stringResource(R.string.DISPLAY_AMOUNT)
        "DISPLAY_TIME" -> stringResource(R.string.DISPLAY_TIME)
        "SODIUM" -> stringResource(R.string.SODIUM)
        "DISPLAY_TUN_VOLUME" -> stringResource(R.string.DISPLAY_TUN_VOLUME)
        "DISPLAY_LAUTERDEADSPACE" -> stringResource(R.string.DISPLAY_LAUTERDEADSPACE)
        "ATTENUATION_FACTOR_CONSTANT" -> stringResource(R.string.ATTENUATION_FACTOR_CONSTANT)
        "MASH_STEPS" -> stringResource(R.string.MASH_STEPS)
        else -> input//input.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
    }
}

fun NAMEtoUnit(input: String): String {
    return when (input) {
        "ALPHA",
        "BETA",
        "HSI",
        "HUMULENE",
        "CAROPHYLLENE",
        "COHUMULONE",
        "MYRCENE",
        "TOTAL_OIL",
        "COARSE_FINE_DIFF" -> "%"
        "DIASTATIC_POWER" -> "Linter"
        else -> ""
    }
}

@Composable
fun ParseToComposable(anObject: Any, parent: String, depth: Int = 0, topLayer: Boolean = false){
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
                        ParseToComposable(item, parent, depth)
                    }
                }
            }
            else {
                Column(
                    //verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in 0 until anObject.length()) {
                        ParseToComposable(anObject.get(i), parent, depth)
                    }
                }
            }
        }
        is JSONObject -> {
            if (topLayer) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    ParseToComposable(anObject, parent, depth)
                }
            } else {
                val name = try {
                    anObject.getString("NAME")
                } catch (e: Exception) {
                    parent
                }
                if (anObject.isOfLength(1)) {
                    Column(modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 10.dp)) {
                        Text("${NAMEtoName(parent)}:")
                        for (key in anObject.keys()) {
                            if (key != "NAME") {
                                Box(modifier = Modifier.padding(start = 10.dp)) {
                                    ParseToComposable(
                                        anObject.get(key.toString()),
                                        key.toString(),
                                        depth
                                    )
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
                                            ParseToComposable(
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
        }
        else -> {
            parseText(parent, anObject.toString())
        }
    }
}


@Composable
fun parseText(key: String, value: String) {
    Text(
        "${NAMEtoName(key)}: ${value}${NAMEtoUnit(key)}",
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
