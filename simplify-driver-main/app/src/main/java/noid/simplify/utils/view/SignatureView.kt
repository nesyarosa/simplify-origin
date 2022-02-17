package noid.simplify.utils.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import androidx.core.view.ViewCompat
import noid.simplify.R
import noid.simplify.utils.view.signature.Bezier
import noid.simplify.utils.view.signature.ControlTimedPoints
import noid.simplify.utils.view.signature.TimedPoint
import noid.simplify.utils.view.signature.ViewTreeObserverCompat
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt


class SignatureView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var mPoints = mutableListOf<TimedPoint>()
    private var mIsEmpty = false
    private var mHasEditState: Boolean? = null
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mLastVelocity = 0f
    private var mLastWidth = 0f
    private val mDirtyRect: RectF

    // Cache
    private val mPointsCache: MutableList<TimedPoint> = ArrayList()
    private val mControlTimedPointsCached: ControlTimedPoints = ControlTimedPoints()
    private val mBezierCached: Bezier = Bezier()

    //Configurable parameters
    private var mMinWidth = 0
    private var mMaxWidth = 0
    private var mVelocityFilterWeight = 0f
    private var mClearOnDoubleClick = false

    //Double click detector
    private val mGestureDetector: GestureDetector

    private val mPaint: Paint = Paint()
    private var mSignatureBitmap: Bitmap? = null
    private var mSignatureBitmapCanvas: Canvas? = null

    private fun clearView() {
        mPoints = ArrayList()
        mLastVelocity = 0f
        mLastWidth = (mMinWidth + mMaxWidth) / 2.toFloat()
        if (mSignatureBitmap != null) {
            mSignatureBitmap = null
            ensureSignatureBitmap()
        }
        isEmpty = true
        invalidate()
    }

    fun clear() {
        clearView()
        mHasEditState = true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        val eventX = event.x
        val eventY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                mPoints.clear()
                if (mGestureDetector.onTouchEvent(event))
                mLastTouchX = eventX
                mLastTouchY = eventY
                addPoint(getNewPoint(eventX, eventY))
                resetDirtyRect(eventX, eventY)
                addPoint(getNewPoint(eventX, eventY))
                isEmpty = false
            }
            MotionEvent.ACTION_MOVE -> {
                resetDirtyRect(eventX, eventY)
                addPoint(getNewPoint(eventX, eventY))
                isEmpty = false
            }
            MotionEvent.ACTION_UP -> {
                resetDirtyRect(eventX, eventY)
                addPoint(getNewPoint(eventX, eventY))
                parent.requestDisallowInterceptTouchEvent(true)
            }
            else -> return false
        }

        invalidate()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        if (mSignatureBitmap != null) {
            canvas.drawBitmap(mSignatureBitmap!!, 0f, 0f, mPaint)
        }
    }

    private var isEmpty: Boolean
        get() = mIsEmpty
        private set(newValue) {
            mIsEmpty = newValue
        }

    var signatureBitmap: Bitmap?
        get() {
            val originalBitmap = getTransparentSignatureBitmap()
            return if (originalBitmap != null) {
                val whiteBgBitmap = Bitmap.createBitmap(originalBitmap.width, originalBitmap.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(whiteBgBitmap)
                canvas.drawColor(Color.WHITE)
                canvas.drawBitmap(originalBitmap, 0f, 0f, null)
                whiteBgBitmap
            } else {
                originalBitmap
            }
        }
        set(signature) {
            // View was laid out...
            if (ViewCompat.isLaidOut(this)) {
                if (signature != null) {
                    clearView()
                    ensureSignatureBitmap()
                    val tempSrc = RectF()
                    val tempDst = RectF()
                    val dWidth = signature.width
                    val dHeight = signature.height
                    val vWidth = width
                    val vHeight = height

                    // Generate the required transform.
                    tempSrc[0f, 0f, dWidth.toFloat()] = dHeight.toFloat()
                    tempDst[0f, 0f, vWidth.toFloat()] = vHeight.toFloat()
                    val drawMatrix = Matrix()
                    drawMatrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER)
                    if (mSignatureBitmap != null) {
                        val canvas = Canvas(mSignatureBitmap!!)
                        canvas.drawBitmap(signature, drawMatrix, null)
                        isEmpty = false
                        invalidate()
                    }
                }
            } else {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        ViewTreeObserverCompat.removeOnGlobalLayoutListener(
                            viewTreeObserver,
                            this
                        )
                        signatureBitmap = signature
                    }
                })
            }
        }

    private fun getTransparentSignatureBitmap(trimBlankSpace: Boolean = false): Bitmap? {
        ensureSignatureBitmap()
        if (!trimBlankSpace) {
            return mSignatureBitmap
        }
        val imgHeight = mSignatureBitmap!!.height
        val imgWidth = mSignatureBitmap!!.width
        val backgroundColor: Int = Color.TRANSPARENT
        var xMin = Int.MAX_VALUE
        var xMax = Int.MIN_VALUE
        var yMin = Int.MAX_VALUE
        var yMax = Int.MIN_VALUE
        var foundPixel = false

        // Find xMin
        for (x in 0 until imgWidth) {
            var stop = false
            for (y in 0 until imgHeight) {
                if (mSignatureBitmap!!.getPixel(x, y) != backgroundColor) {
                    xMin = x
                    stop = true
                    foundPixel = true
                    break
                }
            }
            if (stop) break
        }

        // Image is empty...
        if (!foundPixel) return null

        // Find yMin
        for (y in 0 until imgHeight) {
            var stop = false
            for (x in xMin until imgWidth) {
                if (mSignatureBitmap!!.getPixel(x, y) != backgroundColor) {
                    yMin = y
                    stop = true
                    break
                }
            }
            if (stop) break
        }

        // Find xMax
        for (x in imgWidth - 1 downTo xMin) {
            var stop = false
            for (y in yMin until imgHeight) {
                if (mSignatureBitmap!!.getPixel(x, y) != backgroundColor) {
                    xMax = x
                    stop = true
                    break
                }
            }
            if (stop) break
        }

        // Find yMax
        for (y in imgHeight - 1 downTo yMin) {
            var stop = false
            for (x in xMin..xMax) {
                if (mSignatureBitmap!!.getPixel(x, y) != backgroundColor) {
                    yMax = y
                    stop = true
                    break
                }
            }
            if (stop) break
        }
        return Bitmap.createBitmap(mSignatureBitmap!!, xMin, yMin, xMax - xMin, yMax - yMin)
    }

    private fun onDoubleClick(): Boolean {
        if (mClearOnDoubleClick) {
            clearView()
            return true
        }
        return false
    }

    private fun getNewPoint(x: Float, y: Float): TimedPoint {
        val mCacheSize = mPointsCache.size
        val timedPoint = if (mCacheSize == 0) {
            TimedPoint()
        } else {
            mPointsCache.removeAt(mCacheSize - 1)
        }

        return timedPoint.set(x, y)
    }

    private fun recyclePoint(point: TimedPoint) {
        mPointsCache.add(point)
    }

    private fun addPoint(newPoint: TimedPoint) {
        mPoints.add(newPoint)
        val pointsCount = mPoints.size
        if (pointsCount > 3) {
            var tmp = calculateCurveControlPoints(mPoints[0], mPoints[1], mPoints[2])
            val c2 = tmp.c2
            recyclePoint(tmp.c1)
            tmp = calculateCurveControlPoints(mPoints[1], mPoints[2], mPoints[3])
            val c3 = tmp.c1
            recyclePoint(tmp.c2)
            val curve = mBezierCached.set(mPoints[1], c2, c3, mPoints[2])
            val startPoint = curve.startPoint
            val endPoint = curve.endPoint
            var velocity = endPoint.velocityFrom(startPoint)
            velocity = if (java.lang.Float.isNaN(velocity)) 0.0f else velocity
            velocity = (mVelocityFilterWeight * velocity
                    + (1 - mVelocityFilterWeight) * mLastVelocity)

            // The new width is a function of the velocity. Higher velocities
            // correspond to thinner strokes.
            val newWidth = strokeWidth(velocity)

            // The Bezier's width starts out as last curve's final width, and
            // gradually changes to the stroke width just calculated. The new
            // width calculation is based on the velocity between the Bezier's
            // start and end mPoints.
            addBezier(curve, mLastWidth, newWidth)

            mLastVelocity = velocity
            mLastWidth = newWidth

            // Remove the first element from the list,
            // so that we always have no more than 4 mPoints in mPoints array.
            recyclePoint(mPoints.removeAt(0))
            recyclePoint(c2)
            recyclePoint(c3)
        } else if (pointsCount == 1) {
            // To reduce the initial lag make it work with 3 mPoints
            // by duplicating the first point
            val firstPoint: TimedPoint = mPoints[0]
            mPoints.add(getNewPoint(firstPoint.x, firstPoint.y))
        }
        mHasEditState = true
    }

    private fun calculateCurveControlPoints(
        s1: TimedPoint,
        s2: TimedPoint,
        s3: TimedPoint
    ): ControlTimedPoints {
        val dx1: Float = s1.x - s2.x
        val dy1: Float = s1.y - s2.y
        val dx2: Float = s2.x - s3.x
        val dy2: Float = s2.y - s3.y
        val m1X: Float = (s1.x + s2.x) / 2.0f
        val m1Y: Float = (s1.y + s2.y) / 2.0f
        val m2X: Float = (s2.x + s3.x) / 2.0f
        val m2Y: Float = (s2.y + s3.y) / 2.0f
        val l1 = sqrt(dx1 * dx1 + dy1 * dy1.toDouble()).toFloat()
        val l2 = sqrt(dx2 * dx2 + dy2 * dy2.toDouble()).toFloat()
        val dxm = m1X - m2X
        val dym = m1Y - m2Y
        var k = l2 / (l1 + l2)
        if (java.lang.Float.isNaN(k)) k = 0.0f
        val cmX = m2X + dxm * k
        val cmY = m2Y + dym * k
        val tx: Float = s2.x - cmX
        val ty: Float = s2.y - cmY
        return mControlTimedPointsCached.set(
            getNewPoint(m1X + tx, m1Y + ty),
            getNewPoint(m2X + tx, m2Y + ty)
        )
    }

    private fun strokeWidth(velocity: Float): Float {
        return (mMaxWidth / (velocity + 1)).coerceAtLeast(mMinWidth.toFloat())
    }

    private fun addBezier(
        curve: Bezier,
        startWidth: Float,
        endWidth: Float
    ) {
        ensureSignatureBitmap()
        val originalWidth = mPaint.strokeWidth
        val widthDelta = endWidth - startWidth
        val drawSteps = ceil(curve.length())
        var i = 0
        while (i < drawSteps) {

            // Calculate the Bezier (x, y) coordinate for this step.
            val t = i.toFloat() / drawSteps
            val tt = t * t
            val ttt = tt * t
            val u = 1 - t
            val uu = u * u
            val uuu = uu * u
            var x = uuu * curve.startPoint.x
            x += 3 * uu * t * curve.control1.x
            x += 3 * u * tt * curve.control2.x
            x += ttt * curve.endPoint.x
            var y = uuu * curve.startPoint.y
            y += 3 * uu * t * curve.control1.y
            y += 3 * u * tt * curve.control2.y
            y += ttt * curve.endPoint.y

            // Set the incremental stroke width and draw.
            mPaint.strokeWidth = startWidth + ttt * widthDelta
            mSignatureBitmapCanvas!!.drawPoint(x, y, mPaint)
            expandDirtyRect(x, y)
            i++
        }
        mPaint.strokeWidth = originalWidth
    }

    /**
     * Called when replaying history to ensure the dirty region includes all
     * mPoints.
     *
     * @param historicalX the previous x coordinate.
     * @param historicalY the previous y coordinate.
     */
    private fun expandDirtyRect(historicalX: Float, historicalY: Float) {
        if (historicalX < mDirtyRect.left) {
            mDirtyRect.left = historicalX
        } else if (historicalX > mDirtyRect.right) {
            mDirtyRect.right = historicalX
        }
        if (historicalY < mDirtyRect.top) {
            mDirtyRect.top = historicalY
        } else if (historicalY > mDirtyRect.bottom) {
            mDirtyRect.bottom = historicalY
        }
    }

    /**
     * Resets the dirty region when the motion event occurs.
     *
     * @param eventX the event x coordinate.
     * @param eventY the event y coordinate.
     */
    private fun resetDirtyRect(eventX: Float, eventY: Float) {

        // The mLastTouchX and mLastTouchY were set when the ACTION_DOWN motion event occurred.
        mDirtyRect.left = mLastTouchX.coerceAtMost(eventX)
        mDirtyRect.right = mLastTouchX.coerceAtLeast(eventX)
        mDirtyRect.top = mLastTouchY.coerceAtMost(eventY)
        mDirtyRect.bottom = mLastTouchY.coerceAtLeast(eventY)
    }

    private fun ensureSignatureBitmap() {
        if (mSignatureBitmap == null) {
            mSignatureBitmap = Bitmap.createBitmap(
                width, height,
                Bitmap.Config.ARGB_8888
            )
            if (mSignatureBitmap != null) mSignatureBitmapCanvas = Canvas(mSignatureBitmap!!)
        }
    }

    private fun convertDpToPx(dp: Float): Int {
        return (context.resources.displayMetrics.density * dp).roundToInt()
    }

    init {
        val a: TypedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SignaturePad,
            0, 0
        )

        try {
            mMinWidth = a.getDimensionPixelSize(
                R.styleable.SignaturePad_penMinWidth,
                convertDpToPx(DEFAULT_ATTR_PEN_MIN_WIDTH_PX.toFloat())
            )
            mMaxWidth = a.getDimensionPixelSize(
                R.styleable.SignaturePad_penMaxWidth,
                convertDpToPx(DEFAULT_ATTR_PEN_MAX_WIDTH_PX.toFloat())
            )
            mPaint.color = a.getColor(R.styleable.SignaturePad_penColor,
                DEFAULT_ATTR_PEN_COLOR
            )
            mVelocityFilterWeight = a.getFloat(
                R.styleable.SignaturePad_velocityFilterWeight,
                DEFAULT_ATTR_VELOCITY_FILTER_WEIGHT
            )
            mClearOnDoubleClick = a.getBoolean(
                R.styleable.SignaturePad_clearOnDoubleClick,
                DEFAULT_ATTR_CLEAR_ON_DOUBLE_CLICK
            )
        } finally {
            a.recycle()
        }

        //Fixed parameters
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.strokeJoin = Paint.Join.ROUND

        mDirtyRect = RectF()
        clearView()
        mGestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                return onDoubleClick()
            }
        })
    }

    companion object {
        //Default attribute values
        private const val DEFAULT_ATTR_PEN_MIN_WIDTH_PX = 3
        private const val DEFAULT_ATTR_PEN_MAX_WIDTH_PX = 7
        private const val DEFAULT_ATTR_PEN_COLOR = Color.BLACK
        private const val DEFAULT_ATTR_VELOCITY_FILTER_WEIGHT = 0.9f
        private const val DEFAULT_ATTR_CLEAR_ON_DOUBLE_CLICK = false
    }
}