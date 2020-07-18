sealed class Option<out T> {
    abstract fun isEmpty(): Boolean

    abstract fun <U> map(f: (T) -> U): Option<U>

    abstract fun <U> flatMap(f: (T) -> Option<U>): Option<U>

    fun getOrElse(default: () -> @UnsafeVariance T): T =
        when (this) {
            None -> default()
            is Some -> this.value
        }

    fun orElse(default: () -> Option<@UnsafeVariance T>): Option<T> =
        map { this }.getOrElse(default)

    fun filter(p: (T) -> Boolean): Option<T> =
        flatMap { x -> if (p(x)) this else None }

    object None : Option<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun <U> map(f: (Nothing) -> U): Option<U> = None

        override fun <U> flatMap(f: (Nothing) -> Option<U>): Option<U> = None

        override fun toString(): String = "None"

        override fun equals(other: Any?): Boolean = other === None

        override fun hashCode(): Int = 0
    }

    internal data class Some<T>(internal val value: T) : Option<T>() {
        override fun isEmpty(): Boolean = false

        override fun <U> map(f: (T) -> U): Option<U> = Some(f(value))

        override fun <U> flatMap(f: (T) -> Option<U>): Option<U> =
            map(f).getOrElse { None }
        // or: f(value)
    }

    companion object {
        operator fun <T> invoke(): Option<T> = None
        operator fun <T> invoke(t: T? = null): Option<T> =
            when (t) {
                null -> None
                else -> Some(t)
            }
    }
}

fun max(list: List<Int>): Option<Int> =
    if (list.isEmpty()) Option()
    else Option(list.max())

val max1 = max(listOf(1, 2, 4, 5, 9, 5, 5, 2, 4, 3)).getOrElse { 0 }
val max2 = max(listOf()).getOrElse { 0 }

fun getDefault(): Int = throw RuntimeException()

val max3 = max(listOf(4, 5, 62, 3, 7, 4, 5, 6)).getOrElse(::getDefault)
// val max4 = max(listOf()).getOrElse(::getDefault)

val max5 = max(listOf(5, 3, 8)).map { it + 1 }
val max6 = max(listOf()).map { it + 1 }
