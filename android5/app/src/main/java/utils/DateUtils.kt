package utils

import java.text.SimpleDateFormat
import java.util.*

val blockaDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ"
val blockaDateFormatNoNanos = "yyyy-MM-dd'T'HH:mm:ssZ"

val userDateFormatSimple = "d MMMM yyyy"
val userDateFormatFull = "yyyyMMd jms"
val userDateFormatChat = "E., MMd, jms"

private val simpleFormat = SimpleDateFormat(userDateFormatSimple)
fun Date.toSimpleString(): String {
    return simpleFormat.format(this)
}
