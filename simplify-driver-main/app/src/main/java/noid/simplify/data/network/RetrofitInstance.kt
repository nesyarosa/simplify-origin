package noid.simplify.data.network

import noid.simplify.BuildConfig
import noid.simplify.data.UserPreferences
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetrofitInstance @Inject constructor(private val userPreferences: UserPreferences) {
    private val okHttpBuilder: OkHttpClient.Builder = OkHttpClient.Builder()

    private var headerInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = userPreferences.authToken
        val request = original.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .header(contentType, contentTypeValue)
            .method(original.method, original.body)
            .build()

        chain.proceed(request)
    }

    private val logger: HttpLoggingInterceptor
        get() {
            val loggingInterceptor = HttpLoggingInterceptor()
            if (BuildConfig.DEBUG) {
                loggingInterceptor.apply { level = HttpLoggingInterceptor.Level.BODY }
            }
            return loggingInterceptor
        }

    init {
        val dispatcher = Dispatcher()
        dispatcher.maxRequestsPerHost = 1
        okHttpBuilder.dispatcher(dispatcher)
        okHttpBuilder.addInterceptor(headerInterceptor)
        okHttpBuilder.addInterceptor(logger)
        okHttpBuilder.connectTimeout(timeoutConnect.toLong(), TimeUnit.SECONDS)
        okHttpBuilder.readTimeout(timeoutRead.toLong(), TimeUnit.SECONDS)
    }

    fun <S> create(serviceClass: Class<S>): S {
        val client = okHttpBuilder.build()
        val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.SERVER_URL).client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
        return retrofit.create(serviceClass)
    }

    fun cancelAllRequest() {
        val client = okHttpBuilder.build()
        client.dispatcher.cancelAll()
    }

    companion object {
        private const val timeoutRead = 30   //In seconds
        private const val contentType = "Content-Type"
        private const val contentTypeValue = "application/json"
        private const val timeoutConnect = 30   //In seconds
    }
}