package noid.simplify.constants

import java.util.*

object General {
    const val APP_CENTER_KEY = "0eda8216-063c-43dd-a9e1-401f9b78ebb7"
    const val DEFAULT_ENCODING = "utf-8"
    const val BODY_PARSER_JSON = "application/json; charset=UTF-8"
    const val DEFAULT_COUNTRY_CODE = "+65"
    const val LIMIT_PER_LOAD = 10
    const val AMOUNT_GST = 7.0/100.0
    //ServiceType
    const val SERVICE_TYPE__ADHOC = "ADHOC"
    //JobStatus
    const val STATUS__ASSIGNED = "ASSIGNED"
    const val STATUS__IN_PROGRESS = "IN_PROGRESS"
    const val STATUS__COMPLETED = "COMPLETED"
    const val STATUS__UNASSIGNED = "UNASSIGNED"
    const val STATUS__OVERDUE = "OVERDUE"
    const val STATUS__PAUSED = "PAUSED"
    const val STATUS__CANCELLED = "CANCELLED"
    //FilterBy Date
    const val FILTER_BY__TODAY = "1"
    const val FILTER_BY__TOMORROW = "2"
    const val FILTER_BY__THIS_WEEK = "3"
    const val FILTER_BY__CUSTOM_DATE = "5"
    //Other
    const val ZERO_PRICE = "$0.00"
    //PaymentMethod
    const val METHOD__CASH = "CASH"
    const val METHOD__PAYNOW = "PAYNOW"
    const val METHOD__CHEQUE = "CHEQUE"
    //locale
    val LOCALE_SINGAPORE = Locale("en", "SG")
}