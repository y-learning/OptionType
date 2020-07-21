import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.lang.RuntimeException

sealed class Result<out A> : Serializable {
    abstract fun <B> map(f: (A) -> B): Result<B>

    abstract fun <B> flatMap(f: (A) -> Result<B>): Result<B>

    abstract fun mapFailure(errMsg: String): Result<A>

    abstract fun mapEmpty(errMsg: String): Result<A>

    abstract fun <E : RuntimeException> mapFailure(
        msg: String,
        f: (e: RuntimeException) -> (msg: String) -> E
    ): Result<A>

    abstract fun forEach(effect: (A) -> Unit)

    fun getOrElse(defaultValue: @UnsafeVariance A): A = when (this) {
        is Success -> this.value
        else -> defaultValue
    }

    fun getOrElse(defaultValue: () -> @UnsafeVariance A): A = when (this) {
        is Success -> this.value
        else -> defaultValue()
    }

    fun orElse(defaultValue: () -> Result<@UnsafeVariance A>): Result<A> =
        when (this) {
            is Success -> this
            else -> try {
                defaultValue()
            } catch (e: RuntimeException) {
                Failure<A>(e)
            } catch (e: Exception) {
                Failure<A>(RuntimeException(e))
            }
        }

    fun filter(message: String, p: (A) -> Boolean): Result<A> = flatMap {
        if (p(it)) this
        else failure(message)
    }

    fun filter(p: (A) -> Boolean): Result<A> =
        filter("Condition not matched.", p)

    fun exists(p: (A) -> Boolean): Boolean = map(p).getOrElse(false)

    internal
    object Empty : Result<Nothing>() {
        override fun <B> map(f: (Nothing) -> B): Result<B> = Empty

        override fun <B> flatMap(f: (Nothing) -> Result<B>): Result<B> = Empty

        override fun mapFailure(errMsg: String): Result<Nothing> = this

        override fun <E : RuntimeException> mapFailure(
            msg: String,
            f: (e: RuntimeException) -> (msg: String) -> E
        ): Result<Nothing> = this

        override fun mapEmpty(errMsg: String): Result<Nothing> =
            Failure(RuntimeException(errMsg))

        override fun forEach(effect: (Nothing) -> Unit) {}

        override fun toString(): String = "Empty"
    }

    internal
    data class Failure<out A>(internal val exception: RuntimeException) :
        Result<A>() {

        override fun <B> map(f: (A) -> B): Result<B> = Failure(exception)

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> =
            Failure(exception)

        override fun mapFailure(errMsg: String): Result<A> =
            Failure(RuntimeException(errMsg, exception))

        override fun <E : RuntimeException> mapFailure(
            msg: String,
            f: (e: RuntimeException) -> (msg: String) -> E
        ): Result<A> = Failure(f(exception)(msg))

        override fun mapEmpty(errMsg: String): Result<A> = this

        override fun forEach(effect: (A) -> Unit) {}

        override fun toString(): String = "Failure(exception=$exception)"
    }

    internal
    data class Success<out A>(internal val value: A) : Result<A>() {

        override fun <B> map(f: (A) -> B): Result<B> = try {
            Success(f(value))
        } catch (e: RuntimeException) {
            Failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun <B> flatMap(f: (A) -> Result<B>): Result<B> = try {
            f(value)
        } catch (e: RuntimeException) {
            Failure(e)
        } catch (e: Exception) {
            Failure(RuntimeException(e))
        }

        override fun mapFailure(errMsg: String): Result<A> = this

        override fun <E : RuntimeException> mapFailure(
            msg: String,
            f: (e: RuntimeException) -> (msg: String) -> E
        ): Result<A> = this

        override fun mapEmpty(errMsg: String): Result<A> = this

        override fun forEach(effect: (A) -> Unit) = effect(value)

        override fun toString(): String = "Success(value=$value)"
    }

    companion object {
        operator fun <A> invoke(a: A? = null): Result<A> =
            when (a) {
                null -> Failure(NullPointerException())
                else -> Success(a)
            }

        operator fun <A> invoke(a: A? = null, msg: String): Result<A> =
            when (a) {
                null -> Failure(NullPointerException(msg))
                else -> Success(a)
            }

        operator fun <A> invoke(a: A? = null, p: (A) -> Boolean): Result<A> =
            when (a) {
                null -> Failure(NullPointerException())
                else -> when {
                    p(a) -> Success(a)
                    else -> Empty
                }
            }

        operator fun <A> invoke(
            a: A? = null,
            msg: String,
            p: (A) -> Boolean
        ): Result<A> =
            when (a) {
                null -> Failure(NullPointerException())
                else -> when {
                    p(a) -> Success(a)
                    else -> Failure(
                        IllegalArgumentException(
                            "Argument $a does not match the condition: $msg"
                        )
                    )
                }
            }

        operator fun <A> invoke(): Result<A> = Empty

        fun <A> failure(message: String): Result<A> =
            Failure(IllegalStateException(message))

        fun <A> failure(exception: RuntimeException): Result<A> =
            Failure(exception)

        fun <A> failure(exception: Exception): Result<A> =
            Failure(IllegalStateException(exception))
    }
}