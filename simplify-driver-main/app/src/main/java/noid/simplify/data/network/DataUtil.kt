package noid.simplify.data.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object DataUtil {
    inline fun <reified T> toList(data: String?) : List<T> {
        if (data.isNullOrBlank()) return emptyList()
        val type = Types.newParameterizedType(List::class.java, T::class.java)
        val builder = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<List<T>> = builder.adapter(type)
        return adapter.nullSafe().fromJson(data) ?: emptyList()
    }

    inline fun <reified T> toModel(data: String?) : T? {
        if (data.isNullOrBlank()) return null
        val builder = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<T> = builder.adapter(T::class.java)
        return adapter.nullSafe().fromJson(data)
    }

    fun toJson(data: Any?) : String {
        val builder = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val adapter: JsonAdapter<Any> = builder.adapter(Any::class.java)
        return adapter.nullSafe().toJson(data)
    }
}