package nl.jkool.beerxmlviewer

import org.json.JSONObject

sealed class Either<out L, out R> {

    data class Left<out L>(val value: L): Either<L, Nothing>()

    data class Right<out R>(val value: R): Either<Nothing, R>()

}

inline fun <L, R, T> Either<L, R>.flatMap(
    f : (R) -> Either<L, T>
): Either<L, T> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> f(value)
    }

inline fun <L, R, T> Either<L, R>.map(
    f : (R) -> T
): Either<L, T> =
    when (this) {
        is Either.Left -> this
        is Either.Right -> Either.Right(f(value))
    }

fun eitherGetString(
    o: JSONObject,
    name: String,
    error: String
): Either<String, String> {
    try {
        return Either.Right(o.getString(name))
    } catch (e: Exception) {
        return Either.Left(error)
    }
}

fun eitherGetDouble(
    o: JSONObject,
    name: String,
    error: String
): Either<String, Double> {
    try {
        return Either.Right(o.getDouble(name))
    } catch (e: Exception) {
        return Either.Left(error)
    }
}
