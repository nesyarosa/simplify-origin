package noid.simplify.constants

object Url {
    const val LOGIN = "login"
    const val JOBS = "jobs"
    const val EQUIPMENT = "equipment"
    const val USERS = "users"
    const val JOB_NOTES = "jobnotes"
    const val SERVICE_ITEM_TEMPLATES = "serviceitemtemplates"
    const val ADDITIONAL_SERVICE = "services/additionalService"
    const val SERVICE_ITEMS = "serviceitems"
    const val CLIENT = "clients"
    const val CHANGE_PASSWORD = "changepassword"
    const val FORGOT_PASSWORD = "forgotpassword"
    const val LOGOUT = "logout"

    // SubAPI
    const val UPDATE_TOKEN = "$USERS/token"
    const val GET_URL_UPLOAD_SIGNATURE = "$JOBS/signature"
    const val PREVIOUS_NOTES = "$JOB_NOTES/previous"
}