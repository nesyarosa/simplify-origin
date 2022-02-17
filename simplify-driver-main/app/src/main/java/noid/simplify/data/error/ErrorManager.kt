package noid.simplify.data.error

import noid.simplify.data.error.mapper.ErrorMapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorManager @Inject constructor(private val errorMapper: ErrorMapper) : ErrorFactory {
    override fun getError(errorCode: Int): Error {
        return Error(code = errorCode, message = errorMapper.errorsMap.getValue(errorCode))
    }
}
