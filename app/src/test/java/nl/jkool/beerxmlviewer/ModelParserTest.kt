package nl.jkool.beerxmlviewer

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelParserTest {

    private data class ParserCase(
        val root: String,
        val child: String,
        val itemName: String,
        val parse: (JSONObject?) -> Any?,
        val wrap: (JSONObject) -> JSONObject?
    )

    private val parserCases = listOf(
        ParserCase("HOPS", "HOP", "Cascade", { jsonToHopsObject(it).data }, { Hops(it).toJSON() }),
        ParserCase("FERMENTABLES", "FERMENTABLE", "Pilsner malt", { jsonToFermentablesObject(it).data }, { Fermentables(it).toJSON() }),
        ParserCase("YEASTS", "YEAST", "US-05", { jsonToYeastsObject(it).data }, { Yeasts(it).toJSON() }),
        ParserCase("MISCS", "MISC", "Irish moss", { jsonToMiscsObject(it).data }, { Miscs(it).toJSON() }),
        ParserCase("WATERS", "WATER", "Tap water", { jsonToWatersObject(it).data }, { Waters(it).toJSON() }),
        ParserCase("EQUIPMENTS", "EQUIPMENT", "Kettle", { jsonToEquipmentsObject(it).data }, { Equipments(it).toJSON() }),
        ParserCase("STYLES", "STYLE", "IPA", { jsonToStylesObject(it).data }, { Styles(it).toJSON() }),
        ParserCase("MASHS", "MASH", "Single infusion", { jsonToMashsObject(it).data }, { Mashs(it).toJSON() }),
        ParserCase("RECIPES", "RECIPE", "Pale ale", { jsonToRecipesObject(it).data }, { Recipes(it).toJSON() }),
        ParserCase("RECIPES", "RECIPE", "Brew day", { jsonToBrewsObject(it).data }, { Brews(it).toJSON() })
    )

    @Test
    fun jsonParsers_returnNullDataForNullInput() {
        parserCases.forEach { parserCase ->
            assertNull(parserCase.parse(null))
        }
    }

    @Test
    fun jsonParsers_extractSingleObjectsFromExpectedBeerXmlEnvelope() {
        parserCases.forEach { parserCase ->
            val data = parserCase.parse(envelope(parserCase))

            assertTrue(data is JSONObject)
            assertEquals(parserCase.itemName, (data as JSONObject).getString("NAME"))
        }
    }

    @Test
    fun modelObjects_wrapDataUsingExpectedBeerXmlEnvelope() {
        parserCases.forEach { parserCase ->
            val wrapped = parserCase.wrap(JSONObject().put("NAME", parserCase.itemName))

            assertEquals(
                parserCase.itemName,
                wrapped!!
                    .getJSONObject(parserCase.root)
                    .getJSONObject(parserCase.child)
                    .getString("NAME")
            )
        }
    }

    @Test
    fun modelObjects_returnNullJsonWhenDataIsMissing() {
        assertNull(Hops(null).toJSON())
        assertNull(Recipes(null).toJSON())
        assertNull(Brews(null).toJSON())
    }

    @Test
    fun beerXmlWithMultipleObjectsParsesToJsonArray() {
        val xml = """
            <HOPS>
                <HOP><NAME>Cascade</NAME><ALPHA>5.5</ALPHA></HOP>
                <HOP><NAME>Saaz</NAME><ALPHA>3.5</ALPHA></HOP>
            </HOPS>
        """.trimIndent()

        val hops = jsonToHopsObject(beerXmlToJSONObject(xml))
        val hopList = jsonObjectList(hops.data)

        assertEquals(listOf("Cascade", "Saaz"), hopList.map { it.getString("NAME") })
    }

    @Test
    fun beerXmlRootDetectionMapsKnownEnvelopesToNavigationCodes() {
        assertEquals(objectToCode["Hop"], beerXmlObjectCodeForRoot("HOPS"))
        assertEquals(objectToCode["Fermentable"], beerXmlObjectCodeForRoot("FERMENTABLES"))
        assertEquals(objectToCode["Yeast"], beerXmlObjectCodeForRoot("YEASTS"))
        assertEquals(objectToCode["Misc"], beerXmlObjectCodeForRoot("MISCS"))
        assertEquals(objectToCode["Water"], beerXmlObjectCodeForRoot("WATERS"))
        assertEquals(objectToCode["Equipment"], beerXmlObjectCodeForRoot("EQUIPMENTS"))
        assertEquals(objectToCode["Style"], beerXmlObjectCodeForRoot("STYLES"))
        assertEquals(objectToCode["Mash"], beerXmlObjectCodeForRoot("MASHS"))
        assertEquals(objectToCode["Recipe"], beerXmlObjectCodeForRoot("RECIPES"))
    }

    @Test
    fun beerXmlRootDetectionIgnoresUnknownXmlRoots() {
        assertNull(beerXmlObjectCodeForRoot("NOT_BEER_XML"))
    }

    @Test
    fun recipeParserHandlesMissingOptionalStyle() {
        val xml = """
            <RECIPES>
                <RECIPE>
                    <NAME>No style recipe</NAME>
                    <EST_OG>1.050</EST_OG>
                </RECIPE>
            </RECIPES>
        """.trimIndent()

        val recipe = jsonToRecipesObject(beerXmlToJSONObject(xml)).data as JSONObject

        assertEquals("No style recipe", recipe.displayName())
        assertEquals("Unknown style", recipe.styleName())
    }

    private fun envelope(parserCase: ParserCase): JSONObject =
        JSONObject().put(
            parserCase.root,
            JSONObject().put(
                parserCase.child,
                JSONObject().put("NAME", parserCase.itemName)
            )
        )
}
