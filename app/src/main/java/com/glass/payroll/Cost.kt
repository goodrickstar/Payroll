package com.glass.payroll

import org.threeten.bp.Instant

class Cost {
    var stamp = Instant.now().epochSecond
    var label = ""
    var cost = 0
    var odometer = 0
    var note = ""
    var location = ""
}
