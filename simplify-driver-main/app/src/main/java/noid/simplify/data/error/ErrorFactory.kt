package noid.simplify.data.error

interface ErrorFactory {
    fun getError(errorCode: Int): Error
}
