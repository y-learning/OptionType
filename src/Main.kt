import java.lang.Exception
import kotlin.math.pow

data class Toon(
    val firstName: String,
    val lastName: String,
    val email: Option<String> = Option()
) {
    companion object {
        operator fun invoke(
            firstName: String,
            lastName: String,
            email: String? = null
        ) = Toon(firstName, lastName, Option(email))
    }
}

fun <K, V> Map<K, V>.getOption(key: K) = Option(this[key])

const val NO_DATA = "No data"

fun mean(list: List<Double>): Option<Double> =
    when {
        list.isEmpty() -> Option()
        else -> Option(list.sum() / list.size)
    }

fun variance(list: List<Double>): Option<Double> =
    mean(list).flatMap { mean ->
        mean(list.map {
            (it - mean).pow(2.0)
        })
    }

fun <A, B> lift(f: (A) -> B): (Option<A>) -> Option<B> = {
    try {
        it.map(f)
    } catch (e: Exception) {
        Option()
    }
}

fun <A, B> hLift(f: (A) -> B): (A) -> Option<B> = {
    try {
        Option(it).map(f)
    } catch (e: Exception) {
        Option()
    }
}

val toUpperCaseOption: (Option<String>) -> Option<String> =
    lift(String::toUpperCase)

fun main() {
    val fName1 = "Mickey"
    val fName2 = "Minnie"
    val fName3 = "Donald"
    val lName1 = "Mouse"

    val toons: Map<String, Toon> = mapOf(
        fName1 to Toon(fName1, lName1, "mickey@disney.com"),
        fName2 to Toon(fName2, lName1),
        fName3 to Toon(fName3, "Duck", "donald@disney")
    )

    val mickeyEmail = toons.getOption(fName1).flatMap { it.email }
    val minnieEmail = toons.getOption(fName2).flatMap { it.email }
    val goofyEmail = toons.getOption("Goofy").flatMap { it.email }

    println(mickeyEmail.getOrElse { NO_DATA })
    println(minnieEmail.getOrElse { NO_DATA })
    println(goofyEmail.getOrElse { NO_DATA })

    println()

    val e1 = toons[fName1]?.email ?: NO_DATA
    val e2 = toons[fName2]?.email ?: NO_DATA
    val e3 = toons["Goofy"]?.email ?: NO_DATA

    println(e1)
    println(e2)
    println(e3)
}