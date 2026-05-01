package nl.jkool.beerxmlviewer

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EitherTest {

    @Test
    fun map_transformsRightAndLeavesLeftUntouched() {
        val right = Either.Right(2).map { it * 3 }
        val left = Either.Left("failed").map { value: Int -> value * 3 }

        assertEquals(Either.Right(6), right)
        assertEquals(Either.Left("failed"), left)
    }

    @Test
    fun flatMap_chainsRightAndLeavesLeftUntouched() {
        val right = Either.Right("12").flatMap { Either.Right(it.toInt()) }
        val left = Either.Left("missing").flatMap { value: String -> Either.Right(value.toInt()) }

        assertEquals(Either.Right(12), right)
        assertEquals(Either.Left("missing"), left)
    }

    @Test
    fun eitherGetString_returnsRightForPresentFieldAndLeftForMissingField() {
        val json = JSONObject().put("NAME", "Cascade")

        assertEquals(Either.Right("Cascade"), eitherGetString(json, "NAME", "name missing"))
        assertEquals(Either.Left("amount missing"), eitherGetString(json, "AMOUNT", "amount missing"))
    }

    @Test
    fun eitherGetDouble_returnsRightForNumericFieldAndLeftForMissingField() {
        val json = JSONObject().put("ALPHA", 5.5)
        val alpha = eitherGetDouble(json, "ALPHA", "alpha missing")

        assertTrue(alpha is Either.Right)
        assertEquals(5.5, (alpha as Either.Right).value, 0.0)
        assertEquals(Either.Left("beta missing"), eitherGetDouble(json, "BETA", "beta missing"))
    }
}
