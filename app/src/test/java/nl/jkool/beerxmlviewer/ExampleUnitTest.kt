package nl.jkool.beerxmlviewer

import org.junit.Test

import org.junit.Assert.*
import org.json.JSONArray
import org.json.JSONObject

class HelperFunctionsTest {
    @Test
    fun stripUrl_removesPlainFtpPrefixOnly() {
        assertEquals("example.com", stripUrl("ftp://example.com"))
        assertEquals("example.com", stripUrl(" FTP://example.com "))
        assertEquals("ftpes://example.com", stripUrl("ftpes://example.com"))
        assertEquals("ftps://example.com", stripUrl(" ftps://example.com "))
    }

    @Test
    fun ftpSettingsValidationError_acceptsCompleteSettings() {
        assertNull(ftpSettingsValidationError("ftp://example.com", "/", "user", "password"))
        assertNull(ftpSettingsValidationError("ftpes://example.com:21", "/xml", "user", "password"))
    }

    @Test
    fun ftpSettingsValidationError_rejectsMissingRequiredSettings() {
        assertEquals("FTP site is required.", ftpSettingsValidationError("", "/", "user", "password"))
        assertEquals("FTP path is required.", ftpSettingsValidationError("example.com", "", "user", "password"))
        assertEquals("FTP username is required.", ftpSettingsValidationError("example.com", "/", "", "password"))
        assertEquals("FTP password is required.", ftpSettingsValidationError("example.com", "/", "user", ""))
    }

    @Test
    fun ftpSettingsValidationError_rejectsInvalidServerSettings() {
        assertEquals("FTP site is invalid.", ftpSettingsValidationError("ftp://", "/", "user", "password"))
        assertEquals("FTP site is invalid.", ftpSettingsValidationError("example.com:not-a-port", "/", "user", "password"))
        assertEquals("FTP port is invalid.", ftpSettingsValidationError("example.com:70000", "/", "user", "password"))
    }

    @Test
    fun hasValidFtpSettings_matchesDownloadStartValidation() {
        assertTrue(hasValidFtpSettings("ftp://example.com", "", "user", "password"))
        assertFalse(hasValidFtpSettings("", "/", "user", "password"))
        assertFalse(hasValidFtpSettings("example.com:invalid", "/", "user", "password"))
    }

    @Test
    fun displayName_usesFallbackForMissingName() {
        assertEquals("Unnamed", JSONObject().displayName())
        assertEquals("Fallback", JSONObject().displayName("Fallback"))
        assertEquals("Recipe", JSONObject().put("NAME", "Recipe").displayName())
    }

    @Test
    fun jsonObjectList_acceptsSingleObjectAndArrays() {
        assertEquals(1, jsonObjectList(JSONObject().put("NAME", "Single")).size)
        assertEquals(2, jsonObjectList(JSONArray().put(JSONObject()).put(JSONObject())).size)
        assertTrue(jsonObjectList(null).isEmpty())
    }

    @Test
    fun jsonObjectList_ignoresNonObjectArrayEntries() {
        val input = JSONArray()
            .put(JSONObject().put("NAME", "Object"))
            .put("not an object")

        val result = jsonObjectList(input)

        assertEquals(1, result.size)
        assertEquals("Object", result[0].getString("NAME"))
    }

    @Test
    fun styleName_usesStyleNameWhenPresentAndFallbackWhenMissing() {
        assertEquals("Unknown style", JSONObject().styleName())
        assertEquals(
            "American Pale Ale",
            JSONObject()
                .put("STYLE", JSONObject().put("NAME", "American Pale Ale"))
                .styleName()
        )
    }

    @Test
    fun toStringMap_convertsJsonValuesToStrings() {
        val result = JSONObject()
            .put("site", "example.com")
            .put("fullInfo", true)
            .toStringMap()

        assertEquals("example.com", result["site"])
        assertEquals("true", result["fullInfo"])
    }
}
