sealed class Either<out E, out A> {

    abstract fun <B> map(f: (A) -> B): Either<E, B>

    internal
    class Left<out E, out A>(private val value: E) : Either<E, A>() {
        override fun toString(): String = "Left(value=$value)"

        override fun <B> map(f: (A) -> B): Either<E, B> = Left(value)
    }

    internal
    class Right<out E, out A>(private val value: A) : Either<E, A>() {
        override fun toString(): String = "Right(value=$value)"

        override fun <B> map(f: (A) -> B): Either<E, B> = Right(f(value))
    }

    companion object {
        fun <E, A> left(value: E): Either<E, A> = Left(value)
        fun <E, A> right(value: A): Either<E, A> = Right(value)
    }
}