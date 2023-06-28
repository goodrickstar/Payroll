package com.glass.payroll

import org.threeten.bp.Instant

class Load {
    var stamp = Instant.now().epochSecond
    var rate = 0
    var empty = 0
    var loaded = 0
    var weight = 0.0
    var start: Long = 0
    var stop: Long = 0
    var from = ""
    var to = ""
    var note = ""
    var hazmat = false
    var reefer = false

}
