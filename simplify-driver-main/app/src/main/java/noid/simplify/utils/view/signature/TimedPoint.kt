package noid.simplify.utils.view.signature

import kotlin.math.pow
import kotlin.math.sqrt

class TimedPoint {
    var x = 0f
    var y = 0f
    private var timestamp: Long = 0

    operator fun set(x: Float, y: Float): TimedPoint {
        this.x = x
        this.y = y
        timestamp = System.currentTimeMillis()
        return this
    }

    fun velocityFrom(start: TimedPoint): Float {
        var diff = timestamp - start.timestamp
        if (diff <= 0) {
            diff = 1
        }
        var velocity = distanceTo(start) / diff
        if (java.lang.Float.isInfinite(velocity) || java.lang.Float.isNaN(velocity)) {
            velocity = 0f
        }
        return velocity
    }

    private fun distanceTo(point: TimedPoint): Float {
        return sqrt(
            (point.x - x.toDouble()).pow(2.0) + (point.y - y.toDouble()).pow(2.0)
        ).toFloat()
    }
}