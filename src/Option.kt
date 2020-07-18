sealed class Option<out T> {
    abstract fun isEmpty(): Boolean

    internal object None : Option<Nothing>() {
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