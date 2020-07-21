import result.Result
import result.Result.Empty
import result.getResult
import java.io.IOException
import kotlin.math.pow

class Toon private constructor(
    val firstName: String,
    val lastName: String,
    val email: Result<String>
) {
    companion object {
        operator fun invoke(firstName: String, lastName: String): Toon =
            Toon(firstName, lastName, Empty)

        operator fun invoke(
            firstName: String,
            lastName: String,
            email: String
        ) = Toon(firstName, lastName, Result(email))
    }
}

fun mean(list: kotlin.collections.List<Double>): Option<Double> =
    when {
        list.isEmpty() -> Option()
        else -> Option(list.sum() / list.size)
    }

fun variance(list: kotlin.collections.List<Double>): Option<Double> =
    mean(list).flatMap { mean ->
        mean(list.map {
            (it - mean).pow(2.0)
        })
    }

val toUpperCaseOption: (Option<String>) -> Option<String> =
    lift(String::toUpperCase)

val parseWithRadix: (Int) -> (String) -> Int = { radix ->
    { str ->
        Integer.parseInt(str, radix)
    }
}

val parseHex: (String) -> Int = parseWithRadix(16)

fun validate(name: String?): Result<String> = when {
    name?.isNotEmpty() ?: false -> Result(name)
    else -> Result.failure(IOException())
}

fun getName(): Result<String> = try {
    println("Enter a toon name please:")
    validate(readLine())
} catch (e: IOException) {
    Result.failure(e)
}

fun getNumber(): Result<Int> = try {
    println("Enter an integer please:")
    val numberStr = readLine()
    when {
        numberStr?.isNotEmpty() ?: false -> Result(
            Integer.parseInt(numberStr)
        )
        else -> Result
            .failure(NumberFormatException("Please enter a valid number!"))
    }
} catch (e: NumberFormatException) {
    Result.failure(e)
}

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

    val toon = getName()
        .flatMap(toons::getResult)
        .flatMap(Toon::email)

    println(toon)

    println("\n")

    getNumber()
        .flatMap { if (it % 2 == 0) Result(it) else Result() }
        .forEach(
            { println("$it is even.") },
            onEmpty = { println("It's an odd number.") },
            onFailure = { println("Please enter a valid integer: \n$it") })
}