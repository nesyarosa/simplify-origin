package noid.simplify.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import noid.simplify.constants.ErrorCode
import noid.simplify.constants.General
import noid.simplify.data.network.*
import noid.simplify.utils.Tools
import noid.simplify.utils.extensions.isHasHTMLTags
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.io.IOException
import javax.inject.Inject


class MainRepository @Inject constructor(
        private val retrofitInstance: RetrofitInstance,
        private val networkHelper: NetworkHelper
) {

    suspend fun fetch(url: String,
                      query: HashMap<String,
                              String>? = null): Flow<Resource<String>> {
        return flow {
            val optionsQuery = query ?: HashMap()
            val service = retrofitInstance.create(RetrofitService::class.java)
            emit(
                when (val response = apiCall { service.get(url, optionsQuery) }) {
                    is String -> Resource.Success(data = response)
                    else -> Resource.DataError(url = url, errorCode = response as Int)
                }
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun post(url: String, data: Any? = null, query: HashMap<String, String>? = null): Flow<Resource<String>> {
        return flow {
            val optionsQuery = query ?: HashMap()
            val service = retrofitInstance.create(RetrofitService::class.java)
            val dataInput = DataUtil.toJson(data).toRequestBody(General.BODY_PARSER_JSON.toMediaTypeOrNull())
            emit(
                    when (val response = apiCall {
                        if (data == null) {
                            service.post(url)
                        } else {
                            service.post(url, dataInput, optionsQuery)
                        }
                    }) {
                        is String -> Resource.Success(data = response)
                        else -> Resource.DataError(url = url, errorCode = response as Int)
                    }
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun put(url: String, data: Any, query: HashMap<String, String>? = null): Flow<Resource<String>> {
        return flow {
            val optionsQuery = query ?: HashMap()
            val service = retrofitInstance.create(RetrofitService::class.java)
            val dataInput = DataUtil.toJson(data).toRequestBody(General.BODY_PARSER_JSON.toMediaTypeOrNull())
            emit(
                    when (val response = apiCall { service.put(url, dataInput, optionsQuery) }) {
                        is String -> Resource.Success(data = response)
                        else -> Resource.DataError(url = url, errorCode = response as Int)
                    }
            )
        }.flowOn(Dispatchers.IO)
    }

    suspend fun delete(url: String): Flow<Resource<String>> {
        return flow {
            val service = retrofitInstance.create(RetrofitService::class.java)
            emit(
                    when (val response = apiCall { service.delete(url) }) {
                        is String -> Resource.Success(data = response)
                        else -> Resource.DataError(url = url, errorCode = response as Int)
                    }
            )
        }.flowOn(Dispatchers.IO)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun uploadImage(url: String, imageFile: File): Flow<Resource<String>> {
        return flow {
            if (!networkHelper.isNetworkConnected()) {
                emit(Resource.DataError(url = url, errorCode = ErrorCode.NO_INTERNET_CONNECTION))
                return@flow
            }

            val request = Request.Builder()
                    .header("Content-Type", "image/jpg")
                    .url(url)
                    .put(imageFile.asRequestBody("application/octet-stream".toMediaTypeOrNull()))
                    .build()
            val client = OkHttpClient()
            client.newCall(request).execute().use { response ->
                emit(
                        if (response.isSuccessful) {
                            Resource.Success(data = response.toString())
                        } else {
                            Resource.DataError(url = url, errorCode = response.code)
                        }
                )
                response.body?.close()
            }
        }.flowOn(Dispatchers.IO)
    }

    private suspend fun apiCall(responseCall: suspend () -> Response<*>): Any {
        if (!networkHelper.isNetworkConnected()) {
            retrofitInstance.cancelAllRequest()
            return ErrorCode.NO_INTERNET_CONNECTION
        }

        return try {
            val response = responseCall.invoke()
            val errorCode = getErrorCode(response)
            if (response.isSuccessful) response.body() ?: "success" else errorCode
        } catch (e: IOException) {
            when (e) {
                is HttpException -> e.code()
                else -> ErrorCode.RUNTIME_ERROR_CODE
            }
        }
    }

    private fun getErrorCode(response: Response<*>): Int {
        val errorBody = response.errorBody()?.charStream()?.readText()
        return if (errorBody == null) {
            response.code()
        } else {
            if (errorBody.isHasHTMLTags()) {
                response.code()
            } else {
                Tools.getResponseStringByKey(data = errorBody, key = "errorCode").toInt()
            }
        }
    }
}