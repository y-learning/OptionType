import java.io.IOException
import java.lang.Exception
import kotlin.math.pow

class Toon private constructor(
    val firstName: String,
    val lastName: String,
    val email: Result<String>
) {
    companion object {
        operator fun invoke(firstName: String, lastName: String): Toon {
            val message = "$firstName $lastName has no email address."
            return Toon(firstName, lastName, Result.failure(message))
        }

        operator fun invoke(
            firstName: String,
            lastName: String,
            email: String
        ) =
            Toon(firstName, lastName, Result(email))
    }
}

fun <K, V> Map<K, V>.getOption(key: K) = Option(this[key])

const val NO_DATA = "No data"

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

val parseWithRadix: (Int) -> (String) -> Int = { radix ->
    { str ->
        Integer.parseInt(str, radix)
    }
}

val parseHex: (String) -> Int = parseWithRadix(16)

fun <A, B, C> map2(
    oa: Option<A>,
    ob: Option<B>,
    f: (A) -> (B) -> C
): Option<C> = oa.flatMap { a -> ob.map { f(a)(it) } }

fun <A> sequence(list: List<Option<A>>): Option<List<A>> =
    list.foldRight(Option(List())) { e: Option<A> ->
        { y: Option<List<A>> ->
            map2(e, y) { a: A ->
                { b: List<A> -> b.cons(a) }
            }
        }
    }

// Recursive version of sequence
fun <A> sequence2(list: List<Option<A>>): Option<List<A>> {
    return if (list.isEmpty()) Option(List())
    else list.first().flatMap { x: A ->
        sequence2(list.rest()).map { it.cons(x) }
    }
}

fun <A, B> trverse(list: List<A>, f: (A) -> Option<B>): Option<List<B>> =
    list.foldRight(Option(List())) { a: A ->
        { optionListB: Option<List<B>> ->
            map2(f(a), optionListB) { b ->
                { listB: List<B> -> listB.cons(b) }
            }
        }
    }


fun <A> sequence3(list: List<Option<A>>): Option<List<A>> =
    trverse(list) { it }

fun <K, V> Map<K, V>.getResult(key: K) = when {
    this.containsKey(key) -> Result(this[key])
    else -> Result.failure("The key $key, is not found in the map!")
}

fun validate(name: String?): Result<String> = when {
    name?.isNotEmpty() ?: false -> Result(name)
    else -> Result.failure("Invalid name $name")
}

fun getName(): Result<String> = try {
    validate(readLine())
} catch (e: IOException) {
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
}