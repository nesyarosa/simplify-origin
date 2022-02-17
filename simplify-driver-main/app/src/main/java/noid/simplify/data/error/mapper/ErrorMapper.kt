package noid.simplify.data.error.mapper

import noid.simplify.App
import noid.simplify.R
import noid.simplify.constants.ErrorCode

class ErrorMapper : ErrorMapperInterface {

    override fun getErrorString(errorId: Int): String? {
        return App.context.get()?.getString(errorId)
    }

    override val errorsMap: Map<Int, String?>
        get() = mapOf(
            Pair(ErrorCode.EXPIRED_TOKEN_AUTH, getErrorString(R.string.err_token_expired)),
            Pair(ErrorCode.INVALID_CREDENTIALS, getErrorString(R.string.err_invalid_credentials)),
            Pair(ErrorCode.JOB_NOT_FOUND_ERROR_CODE, getErrorString(R.string.err_job_not_found)),
            Pair(ErrorCode.INACTIVE_ACCOUNT, getErrorString(R.string.err_inactive_account)),
            Pair(ErrorCode.JOB_ALREADY_UPDATED_ERROR_CODE, getErrorString(R.string.err_job_already_updated)),
            Pair(ErrorCode.OTHER_JOB_IN_PROGRESS, getErrorString(R.string.err_other_has_started)),
            Pair(ErrorCode.EXISTING_JOB_IN_PROGRESS_ERROR_CODE, getErrorString(R.string.err_job_already_updated)),
            Pair(ErrorCode.SERVICE_ITEM_NOT_EDITABLE, getErrorString(R.string.err_service_item)),
            Pair(ErrorCode.INSUFFICIENT_PERMISSION_ERROR_CODE, getErrorString(R.string.err_forbidden_access))
        ).withDefault { getErrorString(R.string.err_network) }
}
