package noid.simplify.data.network

import noid.simplify.data.error.ErrorResponse

sealed class Resource<T>(
        val data: T? = null,
        val error: ErrorResponse? = null,
) {
    class Success<T>(data: T) : Resource<T>(data = data)
    class Loading<T>(data: T? = null) : Resource<T>(data = data)
    class DataError<T>(url: String?, errorCode: Int) : Resource<T>(error = ErrorResponse(url, errorCode))

    override fun toString(): String {
        return when (this) {
            is Success<*> -> "Success[data=$data]"
            is DataError -> "Error[code=${error?.code};url=${error?.url}]"
            is Loading<T> -> "Loading"
        }
    }
}