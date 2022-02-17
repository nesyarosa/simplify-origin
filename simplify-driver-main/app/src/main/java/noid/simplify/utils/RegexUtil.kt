package noid.simplify.utils

import java.util.regex.Pattern

object RegexUtil {
    val EMAIL_PATTERN: Pattern = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )
    val PASSWORD_PATTERN: Pattern = Pattern.compile(
        "(?=.*[0-9])" +        //at least 1 number
                "(?=.*[a-z])" +         //at least 1 lowercase letter
                "(?=.*[A-Z])" +         //at least 1 uppercase letter
                "(?=.*[^\\w\\s])" +    //at least 1 symbol
                ".{8,32}"            //at least 6 characters and max 32 characters
    )
    val HTML_PATTERN: Pattern = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>")
}
