package models

import java.util.ArrayList

class Mail(
        var subject: String?,
        var message: String?,
        var recipientTo: ArrayList<String>?
) {
    var recipientCc: Array<String>? = null
}
