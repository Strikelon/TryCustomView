package com.example.trycustomview.view

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.trycustomview.R

class ProgressButtonView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttrs: Int = 0,
) : View(context, attributeSet, defStyleAttrs) {

    companion object {
        private const val DEFAULT_X = 0
        private const val DEFAULT_Y = 0
        private const val DEFAULT_MAX_PROGRESS = 100
        private const val DEFAULT_CURRENT_PROGRESS = 0
        private const val DEFAULT_PROGRESS_TEXT = ""
        private const val DEFAULT_TEXT_SIZE = 30f
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_ID = 0
    }

    private var drawableBackground: Drawable? = null
    private var drawableProgress: Drawable? = null

    private var maxProgress: Int = DEFAULT_MAX_PROGRESS
    private var currentProgress: Int = DEFAULT_CURRENT_PROGRESS

    private var progressText : String = DEFAULT_PROGRESS_TEXT
    private var progressTextStyle : ProgressTextStyle = ProgressTextStyle.NORMAL
    private var progressTextSize: Float = DEFAULT_TEXT_SIZE
    private var progressTextOnBackgroundColor = DEFAULT_TEXT_COLOR
    private var progressTextOnProgressColor = DEFAULT_TEXT_COLOR

    private val paintProgressTextOnBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
    }

    private val paintProgressTextOnProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
    }

    private var progressTextOnBackgroundRect = Rect()

    init {
        setupAttrs(context, attributeSet, defStyleAttrs, defStyleRes = 0)
    }

    private fun setupAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButtonView, defStyleAttr, defStyleRes)

        drawableBackground = typedArray.getDrawable(R.styleable.ProgressButtonView_drawableBackground)
        drawableProgress = typedArray.getDrawable(R.styleable.ProgressButtonView_drawableProgress)

        maxProgress = typedArray.getInteger(R.styleable.ProgressButtonView_maxProgress, DEFAULT_MAX_PROGRESS)
        currentProgress = typedArray.getInteger(R.styleable.ProgressButtonView_currentProgress, DEFAULT_CURRENT_PROGRESS)
        currentProgress = checkAndFixCurrentProgress(currentProgress)

        progressText = typedArray.getText(R.styleable.ProgressButtonView_progressText).toString()
        progressTextSize = typedArray.getDimension(R.styleable.ProgressButtonView_progressTextSize, DEFAULT_TEXT_SIZE)
        progressTextOnBackgroundColor = typedArray.getColor(R.styleable.ProgressButtonView_progressTextOnBackgroundColor, DEFAULT_TEXT_COLOR)
        progressTextOnProgressColor = typedArray.getColor(R.styleable.ProgressButtonView_progressTextOnProgressColor, DEFAULT_TEXT_COLOR)
        progressTextStyle = ProgressTextStyle.values()[typedArray.getInt(R.styleable.ProgressButtonView_progressTextStyle, ProgressTextStyle.NORMAL.ordinal)]

        with(paintProgressTextOnBackground) {
            textSize = progressTextSize
            color = progressTextOnBackgroundColor
            setTypeface(getTextTypeFace(progressTextStyle))
        }

        with(paintProgressTextOnProgress) {
            textSize = progressTextSize
            color = progressTextOnProgressColor
            setTypeface(getTextTypeFace(progressTextStyle))
        }

        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.i("InteresTag","onAttachedToWindow()")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.i("InteresTag", "onMeasure width = ${MeasureSpec.toString(widthMeasureSpec)}")
        Log.i("InteresTag", "onMeasure height =  ${MeasureSpec.toString(heightMeasureSpec)}")

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val width = measureDimension(desiredWidth, widthMeasureSpec)
        val height = measureDimension(desiredHeight, heightMeasureSpec)

        updateDrawableBackgroundBounds(width, height)
        setMeasuredDimension(width, height)
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        if (result < desiredSize) {
            Log.i("InteresTag", "The view is too small, the content might get cut")
        }
        return result
    }

    private fun updateDrawableBackgroundBounds(width: Int, height: Int) {
        drawableBackground?.setBounds(DEFAULT_X, DEFAULT_Y, width, height)
    }

    override fun onDraw(canvas: Canvas) {
        drawableBackground?.draw(canvas)

        val progressWidth = getProgressWidth(width)

        drawableProgress?.let { drawableProgressNotNull ->
            drawableProgressNotNull.setBounds(DEFAULT_X, DEFAULT_Y, progressWidth, height)
            drawableProgressNotNull.draw(canvas)
        }

        paintProgressTextOnBackground.getTextBounds(progressText, 0, progressText.length, progressTextOnBackgroundRect)
        val xPos = (width / 2 - progressTextOnBackgroundRect.width() / 2).toFloat()
        val yPos = (height / 2 - ((paintProgressTextOnBackground.descent() + paintProgressTextOnBackground.ascent()) / 2))

        canvas.drawText(progressText, xPos, yPos, paintProgressTextOnBackground)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.i("InteresTag","onDetachedFromWindow()")
    }

    private fun getProgressWidth(width: Int): Int {
        return (currentProgress * width) / maxProgress
    }

    private fun checkAndFixCurrentProgress(progress: Int): Int {
        if (progress < 0) return 0
        if (progress > maxProgress) return maxProgress
        return progress
    }

    private fun getTextTypeFace(progressTextStyle: ProgressTextStyle): Typeface {
        return when (progressTextStyle) {
            ProgressTextStyle.NORMAL -> Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            ProgressTextStyle.BOLD -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            ProgressTextStyle.ITALIC -> Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        }
    }

    enum class ProgressTextStyle {
        NORMAL, BOLD, ITALIC
    }
}