import java.lang.RuntimeException

sealed class Option<out T> {
    abstract fun isEmpty(): Boolean

    fun getOrElse(default: () -> @UnsafeVariance T): T =
        when (this) {
            None -> default()
            is Some -> this.value
        }

    object None : Option<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun toString(): String = "None"

        override fun equals(other: Any?): Boolean = other === None

        override fun hashCode(): Int = 0
    }

    internal data class Some<T>(internal val value: T) : Option<T>() {
        override fun isEmpty(): Boolean = false
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
val max4 = max(listOf()).getOrElse(::getDefault)
