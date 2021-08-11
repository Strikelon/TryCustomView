package com.example.trycustomview.view

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.trycustomview.R


class GoalProgressBar @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAttrs: Int = 0,
) : View(context, attributeSet, defStyleAttrs) {

    companion object {
        private const val DEFAULT_DIMENSION = 10f
        private const val DEFAULT_COLOR = Color.BLACK
    }

    private var progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL_AND_STROKE
    }

    private var goalIndicatorHeight = 0f
    private var goalIndicatorWidth = 0f
    private var goalReachedColor = 0
    private var goalNotReachedColor = 0
    private var unfilledSectionColor = 0
    private var barHeight = 0f
    private var indicatorType: IndicatorType = IndicatorType.Line

    private var progress: Int = 0
    private var goal: Int = 0
    private var isGoalReached: Boolean = false

    init {
        setupAttrs(context, attributeSet, defStyleAttrs, defStyleRes = 0)
    }

    private fun setupAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.GoalProgressBar, defStyleAttr, defStyleRes)

        goalIndicatorHeight = typedArray.getDimension(R.styleable.GoalProgressBar_goalIndicatorHeight, DEFAULT_DIMENSION)
        goalIndicatorWidth = typedArray.getDimension(R.styleable.GoalProgressBar_goalIndicatorWidth, DEFAULT_DIMENSION)
        barHeight = typedArray.getDimension(R.styleable.GoalProgressBar_barHeight, DEFAULT_DIMENSION)
        goalReachedColor = typedArray.getColor(R.styleable.GoalProgressBar_goalReachedColor, DEFAULT_COLOR)
        goalNotReachedColor = typedArray.getColor(R.styleable.GoalProgressBar_goalNotReachedColor, DEFAULT_COLOR)
        unfilledSectionColor = typedArray.getColor(R.styleable.GoalProgressBar_unfilledSectionColor, DEFAULT_COLOR)
        indicatorType = IndicatorType.values()[typedArray.getInt(R.styleable.GoalProgressBar_indicatorType, IndicatorType.Line.ordinal)]

        typedArray.recycle()
    }

    fun setProgress(progress: Int) {
        setProgress(progress, true)
    }

    private fun setProgress(progress: Int, animate: Boolean) {
        if (animate) {
            Log.i("InteresTag","progress = $progress")
            val currentProgress = this.progress / progress.toFloat()
            Log.i("InteresTag","currentProgress = $currentProgress")
            val barAnimator = ValueAnimator.ofFloat(currentProgress, 1f).apply {
                duration = 700
                interpolator = DecelerateInterpolator()
                addUpdateListener { animation ->
                    val interpolation = animation.animatedValue as Float
                    Log.i("InteresTag","interpolation = $interpolation")
                    setProgress((interpolation * progress).toInt(), false)
                }
            }
            if (!barAnimator.isStarted) {
                barAnimator.start()
            }
        } else {
            Log.i("InteresTag","not animateProgress = $progress")
            this.progress = progress
            updateGoalReached()
            invalidate()
        }
    }

    fun setGoal(goal: Int) {
        this.goal = goal
        updateGoalReached()
        invalidate()
    }

    private fun updateGoalReached() {
        isGoalReached = progress >= goal
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.i("InteresTag", "onAttachedToWindow()")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.i("InteresTag", "onMeasure width = ${MeasureSpec.toString(widthMeasureSpec)}")
        Log.i("InteresTag", "onMeasure height =  ${MeasureSpec.toString(heightMeasureSpec)}")

        val width = MeasureSpec.getSize(widthMeasureSpec)

        // set height

        // set height
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val height: Int = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> Math.min(goalIndicatorHeight, heightSize.toFloat()).toInt()
            else -> goalIndicatorHeight.toInt()
        }

        setMeasuredDimension(width, height)
    }


    override fun onDraw(canvas: Canvas) {
        val halfHeight = height / 2f
        val progressEndX = (width * progress / 100f)

        with(progressPaint) {
            strokeWidth = barHeight
            color = if (isGoalReached) goalReachedColor else goalNotReachedColor
        }
        canvas.drawLine(0f, halfHeight, progressEndX, halfHeight, progressPaint)

        progressPaint.color = unfilledSectionColor;
        canvas.drawLine(progressEndX, halfHeight, width.toFloat(), halfHeight, progressPaint)

        val indicatorPosition = width * goal / 100f
        with(progressPaint) {
            color = goalReachedColor
            strokeWidth = goalIndicatorWidth
        }
        when(indicatorType) {
            IndicatorType.Line -> canvas.drawLine(indicatorPosition, halfHeight - goalIndicatorHeight / 2,
                    indicatorPosition, halfHeight + goalIndicatorHeight / 2, progressPaint)
            IndicatorType.Circle -> canvas.drawCircle(indicatorPosition,
                    goalIndicatorHeight / 2,
                    goalIndicatorHeight / 2,
                    progressPaint)
            IndicatorType.Square -> canvas.drawRect(indicatorPosition - (goalIndicatorHeight / 2),
                    0f,
                    indicatorPosition + (goalIndicatorHeight / 2),
                    goalIndicatorHeight,
                    progressPaint);
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.i("InteresTag", "onDetachedFromWindow()")
    }

    enum class IndicatorType {
        Line, Circle, Square
    }

}