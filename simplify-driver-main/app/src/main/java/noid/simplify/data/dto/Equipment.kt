package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Equipment(

	@Json(name="serviceAddressId")
	val serviceAddressId: Int,

	@Json(name="serialNumber")
	val serialNumber: String,

	@Json(name="updatedBy")
	val updatedBy: Int,

	@Json(name="address")
	val address: String,

	@Json(name="displayName")
	val displayName: String,

	@Json(name="isActive")
	val isActive: Boolean,

	@Json(name="createdAt")
	val createdAt: String,

	@Json(name="model")
	val model: String,

	@Json(name="location")
	val location: String,

	@Json(name="id")
	val id: Int,

	@Json(name="dateWorkDone")
	val dateWorkDone: String?,

	@Json(name="brand")
	val brand: String,

	@Json(name="remarks")
	val remarks: String?,

	@Json(name="updatedAt")
	val updatedAt: String
) : Parcelable
