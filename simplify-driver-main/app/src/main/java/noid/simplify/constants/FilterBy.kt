package noid.simplify.constants

object FilterBy {
    const val TODAY = 1
    const val CUSTOM_DATE = 5

    //Private
    private const val OVERDUE = 2
    private const val TOMORROW = 3
    private const val THIS_WEEK = 4
    private const val ASSIGNED_FILTER = "7"
    private const val OVERDUE_FILTER = "5"

    fun getQuery(indexTab: Int, userId: Int?, startDate: String?, endDate: String?): HashMap<String, String> {
        val query = HashMap<String, String>()
        query["ei"] = userId.toString()
        query["j"] = ASSIGNED_FILTER
        query["ob"] = "startDateTime"
        query["ot"] = "ASC"
        when (indexTab) {
            TODAY -> {
                query["fb"] = General.FILTER_BY__TODAY
            }
            TOMORROW -> {
                query["fb"] = General.FILTER_BY__TOMORROW
            }
            THIS_WEEK -> {
                query["fb"] = General.FILTER_BY__THIS_WEEK
            }
            CUSTOM_DATE -> {
                query["fb"] = General.FILTER_BY__CUSTOM_DATE
                query["sd"] = startDate.toString()
                query["ed"] = endDate.toString()
            }
            OVERDUE -> {
                query["j"] = OVERDUE_FILTER
            }
        }
        return query
    }
}