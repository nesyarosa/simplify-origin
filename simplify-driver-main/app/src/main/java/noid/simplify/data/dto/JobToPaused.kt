package noid.simplify.data.dto

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import noid.simplify.constants.General

@Parcelize
data class JobToPaused (
    @Json(name="jobStatus")
    var jobStatus: String = General.STATUS__IN_PROGRESS,

    @Json(name="location")
    var location: String = ""
) : Parcelable