package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    @Json(name = "id")
    var id: Int = -1,

    @Json(name = "jobId")
    var jobId: Int? = -1,

    @Json(name = "notes")
    var notes: String? = "",

    @Json(name = "image")
    var image: String? = null,

    @Json(name = "imageUrl")
    var imageUrl: String? = null

): Parcelable