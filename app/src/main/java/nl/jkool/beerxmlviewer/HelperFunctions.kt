package nl.jkool.beerxmlviewer

import android.content.Context
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
fun translate(input: String, context: Context): String {
    return when (input) {
        "NAME" -> stringResource(R.string.NAME)
        "NOTES" -> stringResource(R.string.NOTES)
        "VERSION" -> stringResource(R.string.VERSION)
        "ALWAYS_ON_STOCK" -> stringResource(R.string.ALWAYS_ON_STOCK)
        "ALPHA" -> stringResource(R.string.ALPHA    )
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
        "HOPS" -> stringResource(R.string.HOPS)
        "FERMENTABLES" -> stringResource(R.string.FERMENTABLES)
        "MISCS" -> stringResource(R.string.MISCS)
        "YEASTS" -> stringResource(R.string.YEASTS)
        "WATERS" -> stringResource(R.string.WATERS)
        "Boil" -> stringResource(R.string.Boil)
        "DryHop" -> stringResource(R.string.DryHop)
        "Mash" -> stringResource(R.string.Mash)
        "FirstWort" -> stringResource(R.string.FirstWort)
        "Aroma" -> stringResource(R.string.Aroma)
        "Bittering" -> stringResource(R.string.Bittering)
        "Both" -> stringResource(R.string.Both)
        "Pallet" -> stringResource(R.string.Pallet)
        "Plug" -> stringResource(R.string.Plug)
        "Leaf" -> stringResource(R.string.Leaf)
        "Grain" -> stringResource(R.string.Grain)
        "Sugar" -> stringResource(R.string.Sugar)
        "Extract" -> stringResource(R.string.Extract)
        "DryExtract" -> stringResource(R.string.DryExtract)
        "Adjunct" -> stringResource(R.string.Adjunct)
        "Ale" -> stringResource(R.string.Ale)
        "Lager" -> stringResource(R.string.Lager)
        "Wheat" -> stringResource(R.string.Wheat)
        "Wine" -> stringResource(R.string.Wine)
        "Champagne" -> stringResource(R.string.Champagne)
        "Liquid" -> stringResource(R.string.Liquid)
        "Dry" -> stringResource(R.string.Dry)
        "Slant" -> stringResource(R.string.Slant)
        "Culture" -> stringResource(R.string.Culture)
        "Low" -> stringResource(R.string.Low)
        "Medium" -> stringResource(R.string.Medium)
        "High" -> stringResource(R.string.High)
        "VeryHigh" -> stringResource(R.string.VeryHigh)
        "Spice" -> stringResource(R.string.Spice)
        "Fining" -> stringResource(R.string.Fining)
        "WaterAgent" -> stringResource(R.string.WaterAgent)
        "Herb" -> stringResource(R.string.Herb)
        "Flavor" -> stringResource(R.string.Flavor)
        "Other" -> stringResource(R.string.Other)
        "Primary" -> stringResource(R.string.Primary)
        "Secondary" -> stringResource(R.string.Secondary)
        "Bottling" -> stringResource(R.string.Bottling)
        "Mead" -> stringResource(R.string.Mead)
        "Mixed" -> stringResource(R.string.Mixed)
        "Cider" -> stringResource(R.string.Cider)
        "Infusion" -> stringResource(R.string.Infusion)
        "Temperature" -> stringResource(R.string.Temperature)
        "Decoction" -> stringResource(R.string.Decoction)
        "PartialMash" -> stringResource(R.string.PartialMash)
        "AllGrain" -> stringResource(R.string.AllGrain)
        "Rager" -> stringResource(R.string.Rager)
        "Tinseth" -> stringResource(R.string.Tinseth)
        "Garetz" -> stringResource(R.string.Garetz)
        "False" -> stringResource(R.string.False)
        "True" -> stringResource(R.string.True)
        "ACID_TO_pH_5.7" -> stringResource(R.string.ACID_TO_pH_5_7)
        "TIME_AERATED" -> stringResource(R.string.TIME_AERATED)
        "FREE_FIELD" -> stringResource(R.string.FREE_FIELD)
        "FREE_FIELD_NAME" -> stringResource(R.string.FREE_FIELD_NAME)
        "KETTLE_VOLUME" -> stringResource(R.string.KETTLE_VOLUME)
        "TUN_MATERIAL" -> stringResource(R.string.TUN_MATERIAL)
        "TUN_HEIGHT" -> stringResource(R.string.TUN_HEIGHT)
        "AUTONR" -> stringResource(R.string.AUTONR)
        "OG" -> stringResource(R.string.OG)
        "FG" -> stringResource(R.string.FG)
        "FERMENTATION_STAGES" -> stringResource(R.string.FERMENTATION_STAGES)
        "FORCED_CARBONATION" -> stringResource(R.string.FORCED_CARBONATION)
        "EST_OG" -> stringResource(R.string.EST_OG)
        "EST_FG" -> stringResource(R.string.EST_FG)
        "EST_COLOR" -> stringResource(R.string.EST_COLOR)
        "IBU" -> stringResource(R.string.IBU)
        "IBU_METHOD" -> stringResource(R.string.IBU_METHOD)
        "COLOR_METHOD" -> stringResource(R.string.COLOR_METHOD)
        "ACID_SPARGE_PERC" -> stringResource(R.string.ACID_SPARGE_PERC)
        "SPARGE_ACID_TYPE" -> stringResource(R.string.SPARGE_ACID_TYPE)
        "TARGET_PH" -> stringResource(R.string.TARGET_PH)
        "SG_END_MASH" -> stringResource(R.string.SG_END_MASH)
        "SPARGE_WATER_COMP" -> stringResource(R.string.SPARGE_WATER_COMP)
        "OG_BEFORE_BOIL" -> stringResource(R.string.OG_BEFORE_BOIL)
        "OG_FERMENTER" -> stringResource(R.string.OG_FERMENTER)
        "COOLING_METHOD" -> stringResource(R.string.COOLING_METHOD)
        "AERATION_TYPE" -> stringResource(R.string.AERATION_TYPE)
        "SG_END_PRIMARY" -> stringResource(R.string.SG_END_PRIMARY)
        "FORCED_CARB_KEGS" -> stringResource(R.string.FORCED_CARB_KEGS)
        "INVENTORY_REDUCED" -> stringResource(R.string.INVENTORY_REDUCED)
        "LOCKED" -> stringResource(R.string.LOCKED)
        "PRIMING_SUGAR_BOTTLES" -> stringResource(R.string.PRIMING_SUGAR_BOTTLES)
        "PRIMING_SUGAR_KEGS" -> stringResource(R.string.PRIMING_SUGAR_KEGS)
        "BATCH_DIVIDED" -> stringResource(R.string.BATCH_DIVIDED)
        "BATCH_DIVISION" -> stringResource(R.string.BATCH_DIVISION)
        "DIVIDED_FROM" -> stringResource(R.string.DIVIDED_FROM)
        "CALC_ACID" -> stringResource(R.string.CALC_ACID)
        "CULTURE_DATE" -> stringResource(R.string.CULTURE_DATE)
        "AMOUNT_YEAST" -> stringResource(R.string.AMOUNT_YEAST)
        "INVENTORY" -> stringResource(R.string.INVENTORY)
        "PRIMARY_TEMP" -> stringResource(R.string.PRIMARY_TEMP)
        "SECONDARY_TEMP" -> stringResource(R.string.SECONDARY_TEMP)
        "TERTIARY_TEMP" -> stringResource(R.string.TERTIARY_TEMP)
        "EST_ABV" -> stringResource(R.string.EST_ABV)
        "DATE" -> stringResource(R.string.DATE)
        "ABV" -> stringResource(R.string.ABV)
        "ACTUAL_EFFICIENCY" -> stringResource(R.string.ACTUAL_EFFICIENCY)
        "PARENT" -> stringResource(R.string.PARENT)
        "NR_RECIPE" -> stringResource(R.string.NR_RECIPE)
        "LACTIC_SPARGE" -> stringResource(R.string.LACTIC_SPARGE)
        "VOLUME_BEFORE_BOIL" -> stringResource(R.string.VOLUME_BEFORE_BOIL)
        "COOLING_TIME" -> stringResource(R.string.COOLING_TIME)
        "VOLUME_AFTER_BOIL" -> stringResource(R.string.VOLUME_AFTER_BOIL)
        "COOLING_TO" -> stringResource(R.string.COOLING_TO)
        "START_TEMP_PRIMARY" -> stringResource(R.string.START_TEMP_PRIMARY)
        "MAX_TEMP_PRIMARY" -> stringResource(R.string.MAX_TEMP_PRIMARY)
        "END_TEMP_PRIMARY" -> stringResource(R.string.END_TEMP_PRIMARY)
        "TIME_STARTED" -> stringResource(R.string.TIME_STARTED)
        "TIME_ENDED" -> stringResource(R.string.TIME_ENDED)
        "TRUE" -> stringResource(R.string.True)
        "FALSE" -> stringResource(R.string.False)
        "Style" -> stringResource(R.string.Style)
        "Volume" -> stringResource(R.string.Volume)
        "Amount" -> stringResource(R.string.AMOUNT)
        "SPARGE_WATER" -> stringResource(R.string.SPARGE_WATER)
        "WATER_FROM" -> stringResource(R.string.WATER_FROM)
        "Fill until" -> stringResource(R.string.FILL_UNTIL)
        else -> {
            //Toast.makeText(context, input, Toast.LENGTH_LONG).show()
            input
        }//input.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")
    }
}

@Composable
fun NAMEtoUnit(input: String): String {
    return when (input) {
        "ACTUAL_EFFICIENCY",
        "ABV_MIN",
        "ABV_MAX",
        "EST_ABV",
        "ABV",
        "ALPHA",
        "BETA",
        "HSI",
        "HUMULENE",
        "CAROPHYLLENE",
        "COHUMULONE",
        "MYRCENE",
        "TOTAL_OIL",
        "COARSE_FINE_DIFF",
        "MOISTURE",
        "PROTEIN",
        "ATTENUATION",
        "MAX_IN_BATCH",
        "HOP_UTILIZATION",
        "EFFICIENCY",
         "YIELD" -> "%"
        "DIASTATIC_POWER" -> "Linter"
        "AMOUNT" -> "kg/l"
        "RAMP_TIME",
        "STEP_TIME",
        "BOIL_TIME",
        "TIME" -> " " + stringResource(R.string.minutes)
        "CARBONATION_TEMP",
        "AGE_TEMP",
        "TERTIARY_TEMP",
        "SECONDARY_TEMP",
        "PRIMARY_TEMP",
        "MAX_TEMPERATURE",
        "TEMP_STEP",
        "END_TEMP",
        "GRAIN_TEMP",
        "TUN_TEMP",
        "SPARGE_TEMP",
        "MIN_TEMPERATURE" -> "°C"
        "BOIL_SIZE",
        "TUN_VOLUME",
        "LAUTER_DEADSPACE",
        "TOP_UP_KETTLE",
        "INFUSE_AMOUNT",
        "BATCH_SIZE" -> "l"
        "TUN_WEIGHT" -> "kg"
        "EVAP_RATE" -> "%/" + stringResource(R.string.hour)
        "POTENTIAL",
        "OG",
        "FG",
        "OG_MIN",
        "OG_MAX",
        "FG_MIN",
        "FG_MAX" -> " SG"
        "IBU",
        "IBU_MIN",
        "IBU_MAX" -> " IBU"
        "COLOR_MIN",
        "COLOR_MAX" -> " SRM"
        "CARBONATION",
        "CARB_MIN",
        "CARB_MAX" -> stringResource(R.string.volumes_of_co2)
        "AGE",
        "TERTIARY_AGE",
        "SECONDARY_AGE",
        "PRIMARY_AGE" -> stringResource(R.string.days)
        else -> ""
    }
}

@Composable
fun briefRecipeCard(anObject: JSONObject, context: Context){
    Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
        var isExpanded by remember { mutableStateOf(false) }
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = colorResource(depthToColorId(0)),
            modifier = Modifier.clickable { isExpanded = !isExpanded }
        ) {
            Column(modifier = Modifier.padding(all = 10.dp)) {
                if (isExpanded) {
                    Text(
                        "${anObject.getString("NAME")} ▶",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(all = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Text(
                        "${anObject.getString("NAME")} ▼",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(all = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (isExpanded) {
                    parseText("Style", anObject.getJSONObject("STYLE").getString("NAME"), context)
                    parseText("Volume", anObject.getString("DISPLAY_BATCH_SIZE"), context)
                    parseText("EST_OG", anObject.getString("EST_OG"), context)
                    parseText("EST_COLOR", anObject.getString("EST_COLOR"), context)
                    parseText("EFFICIENCY", anObject.getString("EFFICIENCY"), context)
                    parseText("BOIL_TIME", anObject.getString("BOIL_TIME"), context)

                    val fermentablesList= mutableListOf<JSONObject>()
                    val fermentablesObject = anObject.getJSONObject("FERMENTABLES").get("FERMENTABLE")
                    when (fermentablesObject) {
                        is JSONObject ->
                            fermentablesList.add(fermentablesObject)
                        is JSONArray ->
                            for (i in 0 until fermentablesObject.length()) {
                                fermentablesList.add(
                                    fermentablesObject.getJSONObject(i)
                                )
                            }
                    }

                    Text("${translate("WATERS", context)}:")
                    val watersList= mutableListOf<JSONObject>()
                    val watersObject = anObject.getJSONObject("WATERS").get("WATER")
                    when (watersObject) {
                        is JSONObject ->
                            watersList.add(watersObject)
                        is JSONArray ->
                            for (i in 0 until watersObject.length()) {
                                watersList.add(
                                    watersObject.getJSONObject(i)
                                )
                            }
                    }
                    for (water in watersList) {
                        Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = colorResource(depthToColorId(1)),
                            ) {
                                Column(modifier = Modifier.padding(all = 10.dp)) {
                                    val values = eitherGetString(water, "NAME", "Can not get water name").flatMap { name ->
                                        eitherGetString(water, "DISPLAY_AMOUNT", "Can not get amount").map { amount ->
                                            listOf(name, amount)
                                        }
                                    }
                                    when (values) {
                                        is Either.Left -> Text(values.value)
                                        is Either.Right -> {
                                            val (name, amount) = values.value
                                            Text(
                                                "${translate("WATER_FROM", context)} $name",
                                                modifier = Modifier.padding(all = 4.dp),
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            parseText("Amount", amount, context)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    val boilSize = anObject.getString("BOIL_SIZE").split(" ")[0].toFloat()
                    val startAmount = watersList.map { it.getString("AMOUNT").toFloat() }.sum()
                    val trubChillerLoss = try { anObject.getJSONObject("EQUIPMENT").getString("TRUB_CHILLER_LOSS").toFloat() } catch (e: Exception) { 0.toFloat() }
                    val grainAbsorption = fermentablesList.filter { it.getString("TYPE").lowercase() in listOf("grain", "adjunct")}.map { it.getString("AMOUNT").toFloat() }.sum()
                    val spargeWaterAmount = "%.1f".format((grainAbsorption + trubChillerLoss + boilSize) - startAmount)
                    Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = colorResource(depthToColorId(1)),
                        ) {
                            Column(modifier = Modifier.padding(all = 10.dp)) {
                                Text(
                                    "${translate("SPARGE_WATER", context)}",
                                    modifier = Modifier.padding(all = 4.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                parseText("Amount", "$spargeWaterAmount L", context)
                                parseText("Fill until", "${"%.1f".format(1.04 * boilSize)} L", context)
                            }
                        }
                    }

                    Text("${translate("FERMENTABLES", context)}:")
                    for (fermentable in fermentablesList) {
                        Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = colorResource(depthToColorId(1)),
                            ) {
                                Column(modifier = Modifier.padding(all = 10.dp)) {
                                    val values = eitherGetString(fermentable, "NAME", "Can not get name").flatMap { name ->
                                        eitherGetString(fermentable, "DISPLAY_AMOUNT", "Can not get amount").flatMap { amount ->
                                            eitherGetString(fermentable, "SUPPLIER", "Can not get supplier").map { supplier ->
                                                listOf(name, amount, supplier)
                                            }
                                        }
                                    }
                                    when (values) {
                                        is Either.Left -> Text(values.value)
                                        is Either.Right -> {
                                            val (name, amount, supplier) = values.value
                                            parseText("NAME", name, context)
                                            parseText("Amount", amount, context)
                                            parseText("SUPPLIER", supplier, context)
                                            parseText("COLOR", try {fermentable.getString("DISPLAY_COLOR")} catch (e: Exception) { "${0} EBC" }, context)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text("${translate("MASH_STEPS", context)}:")
                    val mashList= mutableListOf<JSONObject>()
                    val mashObject = anObject.getJSONObject("MASH").getJSONObject("MASH_STEPS").get("MASH_STEP")
                    when (mashObject) {
                        is JSONObject ->
                            mashList.add(mashObject)
                        is JSONArray ->
                            for (i in 0 until mashObject.length()) {
                                mashList.add(
                                    mashObject.getJSONObject(i)
                                )
                            }
                    }
                    for (mashStep in mashList.sortedBy { it.getString("STEP_TIME") }) {
                        Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = colorResource(depthToColorId(1)),
                            ) {
                                Column(modifier = Modifier.padding(all = 10.dp)) {
                                    val values = eitherGetString(mashStep, "END_TEMP", "Can not get temp").flatMap { temp ->
                                        eitherGetString(mashStep, "STEP_TIME", "Can not get time").map { time ->
                                            listOf(temp, time)
                                        }
                                    }
                                    val name = eitherGetString(mashStep, "NAME", "Can not get name")
                                    when (values) {
                                        is Either.Left -> Text(values.value)
                                        is Either.Right -> {
                                            val (temp, time) = values.value
                                            when (name) {
                                                is Either.Left -> {}
                                                is Either.Right -> parseText(
                                                    "NAME",
                                                    name.value,
                                                    context
                                                )
                                            }
                                            parseText(
                                                "Temperature",
                                                temp,
                                                context
                                            )
                                            parseText(
                                                "TIME",
                                                time.split(".")[0],
                                                context
                                            )
                                        }
                                    }

                                }
                            }
                        }
                    }

                    Text("${translate("HOPS", context)}:")
                    val hopList = mutableListOf<JSONObject>()
                    val hopsObject = anObject.getJSONObject("HOPS").get("HOP")
                    when (hopsObject) {
                        is JSONObject ->
                            hopList.add(hopsObject)

                        is JSONArray ->
                            for (i in 0 until hopsObject.length()) {
                                hopList.add(
                                    hopsObject.getJSONObject(i)
                                )
                            }
                    }
                    val  hopListWithTime = hopList.map { hopObject ->
                        val time = try { hopObject.getString("TIME") }
                        catch (e: Exception) { null }
                        Pair(hopObject, time) }
                    for (oWithTime in hopListWithTime.sortedByDescending {
                        val (_, time) = it
                        time }) {
                        val (o, time) = oWithTime
                        Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = colorResource(depthToColorId(1)),
                            ) {
                                Column(modifier = Modifier.padding(all = 10.dp)) {
                                    val values = eitherGetString(
                                        o, "NAME", "Can not find hop name"
                                    ).flatMap { name ->
                                        eitherGetString(
                                            o,
                                            "DISPLAY_AMOUNT",
                                            "Can not find hop display amount"
                                        ).flatMap { amount ->
                                            eitherGetString(o, "FORM", "Can not find form").flatMap { form ->
                                                eitherGetString(
                                                    o,
                                                    "ALPHA",
                                                    "Can not find hop alpha"
                                                ).map { alpha ->
                                                    listOf(
                                                        name,
                                                        amount,
                                                        form,
                                                        alpha
                                                    )
                                                    }
                                                }
                                            }
                                    }
                                    when (values) {
                                        is Either.Left -> Text(values.value)
                                        is Either.Right -> {
                                            val (name, amount, form, alpha) = values.value
                                            parseText("NAME", name, context)
                                            parseText(
                                                "Amount",
                                                amount,
                                                context
                                            )
                                            parseText("FORM", form, context)
                                            parseText("ALPHA", alpha, context)

                                            if (time != null) {
                                                parseText("TIME", time, context)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                    Text("${translate("YEASTS", context)}:")
                    val yeastsList = mutableListOf<JSONObject>()
                    val yeastsObject = anObject.getJSONObject("YEASTS").get("YEAST")
                    when (yeastsObject) {
                        is JSONObject ->
                            yeastsList.add(yeastsObject)
                        is JSONArray ->
                            for (i in 0 until yeastsObject.length()) {
                                yeastsList.add(
                                    yeastsObject.getJSONObject(i)
                                )
                            }
                    }
                    for (yeast in yeastsList) {
                        Box(modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 10.dp)) {
                            Surface(
                                shape = MaterialTheme.shapes.medium,
                                color = colorResource(depthToColorId(1)),
                            ) {
                                Column(modifier = Modifier.padding(all = 10.dp)) {
                                    val values = eitherGetString(
                                        yeast,
                                        "NAME",
                                        "Can not find yeast name"
                                    ).flatMap { name ->
                                        eitherGetDouble(
                                            yeast,
                                            "AMOUNT",
                                            "Can not find yeast amount"
                                        ).map { amount ->
                                            listOf(name, (amount * 1000).toInt().toString())
                                        }
                                    }
                                    when (values) {
                                        is Either.Left<String> -> Text(values.value)
                                        is Either.Right<List<String>> -> {
                                            val (name, amount) = values.value
                                            parseText("NAME", name, context)
                                            parseText(
                                                "Amount",
                                                "$amount g",
                                                context
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
    }
}

@Composable
fun briefRecipeView(anObject: Any, context: Context, groupByString: ((a: JSONObject) -> String)? = null) {
    when (anObject) {
        is JSONArray -> {
            val list = mutableListOf<JSONObject>()
            for (i in 0 until anObject.length()) {
                list.add(anObject.getJSONObject(i))
            }
            if (groupByString != null) {
                val groupMap = list.groupBy { groupByString(it) }
                val keyList = groupMap.keys.toList().sorted()
                LazyColumn {
                    items( keyList ) { item ->
                        val orderedSubitems = groupMap.get(item)!!.sortedBy { it.getString("NAME") }
                        Column {
                            Text(item)
                            for (e in orderedSubitems) {
                                briefRecipeCard(e, context)
                            }
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(list.sortedBy { it.getString("NAME") }) { item ->
                        briefRecipeCard(item, context)
                    }
                }
            }
        }
        is JSONObject -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                ) {
                    briefRecipeCard(anObject, context)
                }
            }
        else -> Text("Something went wrong.")
    }
}

@Composable
fun ParseToComposable(anObject: Any, parent: String, context: Context, depth: Int = 0, topLayer: Boolean = false, groupByString: ((a: JSONObject) -> String)? = null){
    when (anObject) {
        is JSONArray -> {
            if (topLayer){
                val list = mutableListOf<JSONObject>()
                for (i in 0 until anObject.length()) {
                    list.add(anObject.getJSONObject(i))
                }
                if (groupByString != null) {
                    val groupMap = list.groupBy { groupByString(it) }
                    val keyList = groupMap.keys.toList().sorted()
                    LazyColumn {
                        items( keyList ) { item ->
                            val orderedSubitems = groupMap.get(item)!!.sortedBy { it.getString("NAME") }
                            Column {
                                Text(item)
                                for (e in orderedSubitems) {
                                    ParseToComposable(e, parent, context, depth)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn {
                        items(list.sortedBy { it.getString("NAME") }) { item ->
                            ParseToComposable(item, parent, context, depth)
                        }
                    }
                }
            }
            else {
                Column(
                    //verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (i in 0 until anObject.length()) {
                        ParseToComposable(anObject.get(i), parent, context, depth)
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
                    ParseToComposable(anObject, parent, context, depth)
                }
            } else {
                val name = try {
                    anObject.getString("NAME")
                } catch (e: Exception) {
                    parent
                }
                if (anObject.isOfLength(1)) {
                    Column(modifier = Modifier.padding(0.dp, 10.dp, 0.dp, 10.dp)) {
                        Text("${translate(parent, context)}:")
                        for (key in anObject.keys()) {
                            if (key != "NAME") {
                                Box(modifier = Modifier.padding(start = 10.dp)) {
                                    ParseToComposable(
                                        anObject.get(key.toString()),
                                        key.toString(),
                                        context,
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
                                                context,
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
            parseText(parent, anObject.toString(), context)
        }
    }
}


@Composable
fun parseText(key: String, value: String, context: Context) {
    Text(
        "${translate(key, context)}: ${translate(value, context)}${NAMEtoUnit(key)}",
        modifier = Modifier.padding(all = 4.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun mapCard(map: Map<String, String>, context: Context) {

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
                        parseText(key, value, context)
                    }
                }
            }
        }
    }
}
