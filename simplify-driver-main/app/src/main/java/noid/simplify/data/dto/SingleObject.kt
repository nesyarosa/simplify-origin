package noid.simplify.data.dto

import com.squareup.moshi.Json

data class NoteDelete(@Json(name = "id") var id: Int = -1)

data class Notification(@Json(name = "token") var token: String = "")

data class JobUpdate(@Json(name = "jobId") var jobId: Int = -1)

data class RemarksUpdate(@Json(name = "remarks") var remarks: String? = "")
