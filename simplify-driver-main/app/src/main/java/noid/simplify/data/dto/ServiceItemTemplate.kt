package noid.simplify.data.dto

import com.squareup.moshi.Json

data class ServiceItemTemplate(

	@Json(name="unitPrice")
	val unitPrice: Int,

	@Json(name="name")
	val name: String,

	@Json(name="id")
	val id: Int
) {
	override fun toString(): String {
		return name
	}
}
