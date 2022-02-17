package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckListJob (
    @Json(name="id")
    val id : Int? = 0,

    @Json(name="name")
    val name : String? = "",

    @Json(name="description")
    val description : String? = "",

    @Json(name="jobId")
    val jobId : Int? = 0,

    @Json(name="ChecklistJobItems")
    val ChecklistJobItems : List<ChecklistItems>? = emptyList(),

) : Parcelable {

    fun getStatus() : Boolean {
        val done = ChecklistJobItems?.count { it.status }
        val all = ChecklistJobItems?.count()
        return done == all
    }

    fun isDescriptionNull(): Boolean {
        return description.isNullOrBlank()
    }

}