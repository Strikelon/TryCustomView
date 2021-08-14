package com.example.trycustomview.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.trycustomview.R


class SwagPoints @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttrs: Int = 0,
) : View(context, attributeSet, defStyleAttrs) {

    companion object {
        private const val DEFAULT_DIMENSION = 10f
        private const val DEFAULT_COLOR = Color.BLACK
        private const val DEFAULT_BOOLEAN = false
        private const val ANGLE_OFFSET = -90f
        private const val INVALID_VALUE = -1f
        private const val MAX = 100
        private const val MIN = 0
        private const val DEFAULT_STEP = 10
        private const val DEFAULT_WIDTH = 12
        private const val DEFAULT_TEXT_SIZE = 72
    }

    private var mProgressWidth: Int = DEFAULT_WIDTH
    private var progressColor: Int = DEFAULT_COLOR

    private var mArcWidth: Int = DEFAULT_WIDTH
    private var mArcRadius: Int = 0
    private var arcColor: Int = DEFAULT_COLOR

    private var mTextSize: Int = DEFAULT_TEXT_SIZE
    private var mTextColor: Int = DEFAULT_COLOR

    private var mIndicatorIcon: Drawable? = null
    private var indicatorIconHalfWidth: Int = 0
    private var indicatorIconHalfHeight: Int = 0

    private var isClockwise: Boolean = true
    private var ismEnabled: Boolean = true

    private var mPoints: Int = MIN
    private var mMax: Int = MAX
    private var mMin: Int = MIN
    private var mStep: Int = DEFAULT_STEP

    /**
     * The counts of point update to determine whether to change previous progress.
     */
    private var mUpdateTimes = 0
    private var mPreviousProgress = -1f
    private var mCurrentProgress = 0f

    /**
     * Determine whether reach max of point.
     */
    private var isMax = false

    /**
     * Determine whether reach min of point.
     */
    private var isMin = false

    private var mTranslateX: Float = 0f
    private var mTranslateY: Float = 0f

    private var mArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private var mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private var mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var mArcRect = RectF()
    private var mTextRect = Rect()

    private var mProgressSweep = 0f
    private var mIndicatorIconX = 0
    private var mIndicatorIconY = 0

    private var mTouchAngle = 0.0
    private var mOnSwagPointsChangeListener: OnSwagPointsChangeListener? = null

    interface OnSwagPointsChangeListener {
        /**
         * Notification that the point value has changed.
         *
         * @param swagPoints The SwagPoints view whose value has changed
         * @param points     The current point value.
         * @param fromUser   True if the point change was triggered by the user.
         */
        fun onPointsChanged(swagPoints: SwagPoints, points: Float, fromUser: Boolean)
        fun onStartTrackingTouch(swagPoints: SwagPoints?)
        fun onStopTrackingTouch(swagPoints: SwagPoints?)
    }

    fun setOnSwagPointsChangeListener(onSwagPointsChangeListener: OnSwagPointsChangeListener) {
        mOnSwagPointsChangeListener = onSwagPointsChangeListener
    }

    init {
        setupAttrs(context, attributeSet, defStyleAttrs, defStyleRes = 0)
    }

    private fun setupAttrs(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.SwagPoints,
            defStyleAttr,
            defStyleRes
        )

        mIndicatorIcon = typedArray.getDrawable(R.styleable.SwagPoints_indicatorIcon)

        mIndicatorIcon?.let { mIndicatorIconNotNull ->
            indicatorIconHalfWidth = 55
            indicatorIconHalfHeight = 55
            Log.i("InteresTag","indicatorIconHalfWidth = $indicatorIconHalfWidth")
            Log.i("InteresTag","indicatorIconHalfHeight = $indicatorIconHalfHeight")
            mIndicatorIconNotNull.setBounds(
                -indicatorIconHalfWidth, -indicatorIconHalfHeight, indicatorIconHalfWidth,
                indicatorIconHalfHeight
            );
        }

        mPoints = typedArray.getInteger(R.styleable.SwagPoints_points, mPoints)
        mMin = typedArray.getInteger(R.styleable.SwagPoints_min, mMin)
        mMax = typedArray.getInteger(R.styleable.SwagPoints_max, mMax)
        mStep = typedArray.getInteger(R.styleable.SwagPoints_step, mStep)

        mProgressWidth = typedArray.getDimension(
            R.styleable.SwagPoints_progressWidth,
            DEFAULT_DIMENSION
        ).toInt()
        progressColor = typedArray.getColor(R.styleable.SwagPoints_progressColor, DEFAULT_COLOR)

        mArcWidth = typedArray.getDimension(R.styleable.SwagPoints_arcWidth, DEFAULT_DIMENSION).toInt()
        arcColor = typedArray.getColor(R.styleable.SwagPoints_arcColor, DEFAULT_COLOR)

        mTextSize = typedArray.getDimension(R.styleable.SwagPoints_textSize, DEFAULT_DIMENSION).toInt()
        mTextColor = typedArray.getColor(R.styleable.SwagPoints_textColor, DEFAULT_COLOR)

        isClockwise = typedArray.getBoolean(R.styleable.SwagPoints_clockwise, DEFAULT_BOOLEAN)
        ismEnabled = typedArray.getBoolean(R.styleable.SwagPoints_enabled, DEFAULT_BOOLEAN)

        typedArray.recycle()

        with(mArcPaint) {
            color = arcColor
            strokeWidth = mArcWidth.toFloat()
        }

        with(mProgressPaint) {
            color = progressColor
            strokeWidth = mProgressWidth.toFloat()
        }

        with(mTextPaint) {
            color = mTextColor
            textSize = mTextSize.toFloat()
        }

        mPoints = if (mPoints > mMax) mMax else mPoints
        mPoints = if (mPoints < mMin) mMin else mPoints

        mProgressSweep = mPoints.toFloat() / valuePerDegree()
    }

    private fun valuePerDegree(): Float {
        return mMax.toFloat() / 360.0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultSize(suggestedMinimumHeight, heightMeasureSpec)
        val min = Math.min(width, height)

        mTranslateX = (width * 0.5f)
        mTranslateY = (height * 0.5f)

        val arcDiameter = (min - paddingLeft)
        mArcRadius = arcDiameter / 2

        val top = (height / 2 - mArcRadius).toFloat()
        val left = (width / 2 - mArcRadius).toFloat()

        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter)

        updateIndicatorIconPosition()
        setMeasuredDimension(width, height)
    }

    private fun updateIndicatorIconPosition() {
        val thumbAngle = (mProgressSweep + 90).toInt()
        mIndicatorIconX = (mArcRadius * Math.cos(Math.toRadians(thumbAngle.toDouble()))).toInt()
        mIndicatorIconY = (mArcRadius * Math.sin(Math.toRadians(thumbAngle.toDouble()))).toInt()
        Log.i("InteresTag", "thumbAngle = $thumbAngle")
        Log.i("InteresTag", "mIndicatorIconX = $mIndicatorIconX")
        Log.i("InteresTag", "mIndicatorIconY = $mIndicatorIconY")
    }

    override fun onDraw(canvas: Canvas) {
        if (!isClockwise) {
            canvas.scale(-1f, 1f, mArcRect.centerX(), mArcRect.centerY());
        }

        val textPoint = mPoints.toString()
        mTextPaint.getTextBounds(textPoint, 0, textPoint.length, mTextRect)

        val xPos = (width / 2 - mTextRect.width() / 2).toFloat()
        val yPos = ((mArcRect.centerY()) - ((mTextPaint.descent() + mTextPaint.ascent()) / 2)).toFloat()

        canvas.drawText(textPoint, xPos, yPos, mTextPaint)

        Log.i("InteresTag", "mProgressSweep = $mProgressSweep")

        canvas.drawArc(mArcRect, ANGLE_OFFSET, 360f, false, mArcPaint)
        canvas.drawArc(mArcRect, ANGLE_OFFSET, mProgressSweep, false, mProgressPaint)

        if (ismEnabled) {
            // draw the indicator icon
            mIndicatorIcon?.let { mIndicatorIconNotNull ->
                canvas.translate(
                    mTranslateX - mIndicatorIconX,
                    mTranslateY - mIndicatorIconY
                )
                mIndicatorIconNotNull.draw(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (ismEnabled) {
            this.parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mOnSwagPointsChangeListener?.onStartTrackingTouch(this)
                }
                MotionEvent.ACTION_MOVE -> updateOnTouch(event)
                MotionEvent.ACTION_UP -> {
                    mOnSwagPointsChangeListener?.onStopTrackingTouch(
                        this
                    )
                    isPressed = false
                    this.parent.requestDisallowInterceptTouchEvent(false)
                }
                MotionEvent.ACTION_CANCEL -> {
                    mOnSwagPointsChangeListener?.onStopTrackingTouch(this)
                    isPressed = false
                    this.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            return true
        }
        return false
    }

    private fun updateOnTouch(event: MotionEvent) {
        isPressed = true
        mTouchAngle = convertTouchEventPointToAngle(event.x, event.y)
        val progress: Float = convertAngleToProgress(mTouchAngle)
        updateProgress(progress, true)
    }

    private fun convertTouchEventPointToAngle(xPos: Float, yPos: Float): Double {
        // transform touch coordinate into component coordinate
        var x = xPos - mTranslateX
        val y = yPos - mTranslateY
        x = if (isClockwise) x else -x
        var angle = Math.toDegrees(Math.atan2(y.toDouble(), x.toDouble()) + Math.PI / 2)
        angle = if (angle < 0) angle + 360 else angle
        return angle
    }

    private fun convertAngleToProgress(angle: Double): Float {
        return Math.round(valuePerDegree() * angle).toFloat()
    }

    private fun updateProgress(progress: Float, fromUser: Boolean) {

        // detect points change closed to max or min
        var progress = progress
        val maxDetectValue = (mMax.toDouble() * 0.95).toInt()
        val minDetectValue = (mMax.toDouble() * 0.05).toInt() + mMin
        mUpdateTimes++
        if (progress == INVALID_VALUE) {
            return
        }

        // avoid accidentally touch to become max from original point
        if (progress > maxDetectValue && mPreviousProgress === INVALID_VALUE) {
            return
        }


        // record previous and current progress change
        if (mUpdateTimes == 1) {
            mCurrentProgress = progress.toFloat()
        } else {
            mPreviousProgress = mCurrentProgress
            mCurrentProgress = progress.toFloat()
        }

        mPoints = (progress - progress % mStep).toInt()
        /**
         * Determine whether reach max or min to lock point update event.
         *
         * When reaching max, the progress will drop from max (or maxDetectPoints ~ max
         * to min (or min ~ minDetectPoints) and vice versa.
         *
         * If reach max or min, stop increasing / decreasing to avoid exceeding the max / min.
         */
        if (mUpdateTimes > 1 && !isMin && !isMax) {
            if (mPreviousProgress >= maxDetectValue && mCurrentProgress <= minDetectValue && mPreviousProgress > mCurrentProgress) {
                isMax = true
                progress = mMax.toFloat()
                mPoints = mMax
                mOnSwagPointsChangeListener?.let { mOnSwagPointsChangeListenerNotNull ->
                    mOnSwagPointsChangeListenerNotNull.onPointsChanged(this, progress, fromUser)
                    return
                }
            } else if (mCurrentProgress >= maxDetectValue && mPreviousProgress <= minDetectValue && mCurrentProgress > mPreviousProgress || mCurrentProgress <= mMin) {
                isMin = true
                progress = mMin.toFloat()
                mPoints = mMin
                mOnSwagPointsChangeListener?.let { mOnSwagPointsChangeListenerNotNull ->
                    mOnSwagPointsChangeListenerNotNull.onPointsChanged(this, progress, fromUser)
                    return
                }
            }
            invalidate()
        } else {

            // Detect whether decreasing from max or increasing from min, to unlock the update event.
            // Make sure to check in detect range only.
            if (isMax and (mCurrentProgress < mPreviousProgress) && mCurrentProgress >= maxDetectValue) {
                isMax = false
            }
            if (isMin
                && mPreviousProgress < mCurrentProgress
                && mPreviousProgress <= minDetectValue && mCurrentProgress <= minDetectValue && mPoints >= mMin
            ) {
                isMin = false
            }
        }
        if (!isMax && !isMin) {
            progress = if (progress > mMax) mMax.toFloat() else progress
            progress = if (progress < mMin) mMin.toFloat() else progress
            mOnSwagPointsChangeListener?.let { mOnSwagPointsChangeListenerNotNull ->
                progress -= progress % mStep
                mOnSwagPointsChangeListenerNotNull.onPointsChanged(this, progress, fromUser)
            }
            mProgressSweep = progress.toFloat() / valuePerDegree()
            updateIndicatorIconPosition()
            invalidate()
        }
    }

}