package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class ChecklistItems (
    @Json(name="id")
    val id: Int? = 0,

    @Json(name="name")
    val name: String? = "",

    @Json(name="status")
    var status: Boolean = false,

    @Json(name="remarks")
    val remarks: String? = null,

    @Json(name="checklistJobId")
    val checklistJobId : Int? = 0

) : Parcelable