import result.Result

const val SET_HEAD_EMPTY = "setHead called on an empty list"
const val FIRST_EMPTY = "first called on an empty list"

fun inc(i: Int) = i + 1

fun <T> flatten(list: List<List<T>>): List<T> =
    list.foldLeft(List()) { list1 -> list1::concat }

fun <E> List<E>.concat(list: List<E>) = List.concatViaFoldLeft(this, list)

sealed class List<out E> {
    abstract val length: Int

    abstract fun isEmpty(): Boolean

    abstract fun length(): Int

    abstract fun lengthMemoized(): Int

    abstract fun setHead(x: @UnsafeVariance E): List<E>

    abstract fun first(): E

    abstract fun firstSafe(): Result<E>

    abstract fun rest(): List<E>

    abstract fun lastSafe(): Result<E>

    fun cons(x: @UnsafeVariance E): List<E> = Cons(x, this)

    fun drop(n: Int): List<E> = Companion.drop(n, this)

    fun dropWhile(list: List<@UnsafeVariance E>, p: (E) -> Boolean): List<E> =
        Companion.dropWhile(list, p)

    fun reverse(): List<E> = Companion.reverse(invoke(), this)

    fun <U> reverse2(): List<E> = this.foldLeft(Nil as List<E>) { acc ->
        { acc.cons(it) }
    }

    fun init(): List<E> = reverse().drop(1).reverse()

    fun <U> foldRight(identity: U, f: (E) -> (U) -> U): U =
        Companion.foldRight(this, identity, f)

    fun <U> foldLeft(identity: U, f: (U) -> (E) -> U): U =
        Companion.foldLeft(this, identity, f)

    fun <U> foldRightViaFoldLeft(identity: U, f: (E) -> (U) -> U): U =
        this.foldLeft(identity, { x -> { y -> f(y)(x) } })

    fun <U> coFoldRight(identity: U, f: (E) -> (U) -> U): U =
        Companion.coFoldRight(this.reverse(), identity, f)

    fun <U> map(f: (E) -> U): List<U> =
        this.coFoldRight(Nil as List<U>) { e -> { it.cons(f(e)) } }

    fun filter(p: (E) -> Boolean): List<E> =
        this.foldLeft(Nil as List<E>) { acc ->
            { e ->
                if (p(e)) acc.cons(e)
                else acc
            }
        }.reverse()

    fun <U> flatMap(f: (E) -> List<U>): List<U> = flatten(map(f))

    fun filterViaFlatMap(p: (E) -> Boolean) = this.flatMap { e ->
        if (p(e)) List(e) else Nil
    }

    abstract class Empty<E> : List<E>() {
        override val length: Int get() = 0

        override fun isEmpty(): Boolean = true

        override fun setHead(x: E): List<E> = throw Exception(SET_HEAD_EMPTY)

        override fun length(): Int = 0

        override fun lengthMemoized(): Int = 0

        override fun first(): E = throw Exception(FIRST_EMPTY)

        override fun firstSafe(): Result<E> = Result()

        override fun rest(): List<E> = this

        override fun lastSafe(): Result<E> = Result()

        override fun toString(): String = "[NIL]"
    }

    internal object Nil : Empty<Nothing>()

    internal class Cons<E>(private val head: E, private val tail: List<E>) :
        List<E>() {
        override val length: Int = tail.length() + 1

        override fun isEmpty(): Boolean = false

        override fun length(): Int = foldRight(0) { ::inc }

        override fun lengthMemoized(): Int = length

        override fun setHead(x: E): List<E> = this.tail.cons(x)

        private tailrec fun toString(acc: String, list: List<E>): String =
            if (list.isEmpty()) acc
            else toString("$acc${list.first()}, ", list.rest())

        override fun first(): E = this.head

        override fun firstSafe(): Result<E> = Result(head)

        override fun rest(): List<E> = this.tail

        override fun lastSafe(): Result<E> =
            foldLeft(Result()) { { item -> Result(item) } }

        override fun toString(): String = "[${toString("", this)}NIL]"
    }

    companion object {
        operator
        fun <E> invoke(vararg az: E): List<E> =
            az.foldRight(Nil) { item: E, acc: List<E> ->
                Cons(item, acc)
            }

        internal tailrec fun <E> drop(i: Int, list: List<E>): List<E> =
            when {
                i <= 0 -> list
                list.isEmpty() -> list
                else -> drop(i - 1, list.rest())
            }

        private tailrec fun <E> dropWhile(list: List<E>, p: (E) -> Boolean):
                List<E> =
            when {
                list.isEmpty() -> list
                p(list.first()) -> dropWhile(list.rest(), p)
                else -> list
            }

        private tailrec fun <E> reverse(acc: List<E>, list: List<E>): List<E> =
            if (list.isEmpty()) acc
            else reverse(acc.cons(list.first()), list.rest())

        fun <T, U> foldRight(
            list: List<T>,
            identity: U,
            f: (T) -> (U) -> U): U =
            if (list.isEmpty()) identity
            else f(list.first())(foldRight(list.rest(), identity, f))

        tailrec fun <E, U> foldLeft(
            list: List<E>, acc: U,
            f: (U) -> (E) -> U): U =
            if (list.isEmpty()) acc
            else foldLeft(list.rest(), f(acc)(list.first()), f)

        tailrec fun <T, U> coFoldRight(
            list: List<T>,
            acc: U,
            f: (T) -> (U) -> U): U =
            if (list.isEmpty()) acc
            else coFoldRight(list.rest(), f(list.first())(acc), f)

        fun <T> concatViaFoldRight(list1: List<T>, list2: List<T>): List<T> =
            list1.foldRight(list2, { x -> { y -> y.cons(x) } })

        fun <T> concatViaFoldLeft(list1: List<T>, list2: List<T>): List<T> =
            list1.reverse().foldLeft(list2, { x -> x::cons })
    }
}