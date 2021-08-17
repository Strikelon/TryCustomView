package com.example.trycustomview.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.example.trycustomview.R

class ProgressButtonView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : View(context, attributeSet, defStyleAttrs) {

    companion object {
        private const val DEFAULT_X = 0
        private const val DEFAULT_Y = 0
        private const val DEFAULT_MAX_PROGRESS = 100
        private const val DEFAULT_CURRENT_PROGRESS = 0
        private const val DEFAULT_PROGRESS_TEXT = ""
        private const val DEFAULT_TEXT_SIZE = 30f
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_RIPPLE_EFFECT_COLOR = Color.GRAY
        private const val DEFAULT_BOOLEAN = true
        private const val DEFAULT_RIPPLE_EFFECT_STEP_COUNT = 20f
        private const val DEFAULT_RIPPLE_EFFECT_RATIO = 0.5f
    }

    private var drawableBackground: Drawable? = null
    private var drawableProgress: Drawable? = null

    private var maxProgress: Int = DEFAULT_MAX_PROGRESS
    private var currentProgress: Int = DEFAULT_CURRENT_PROGRESS

    private var progressText: String = DEFAULT_PROGRESS_TEXT
    private var progressTextStyle: ProgressTextStyle = ProgressTextStyle.NORMAL
    private var progressTextSize: Float = DEFAULT_TEXT_SIZE
    private var progressTextOnBackgroundColor = DEFAULT_TEXT_COLOR
    private var progressTextOnProgressColor = DEFAULT_TEXT_COLOR
    private var isUseTextOnProgress = DEFAULT_BOOLEAN
    private var rippleEffectColor = DEFAULT_RIPPLE_EFFECT_COLOR

    private val paintProgressTextOnBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
    }

    private val paintProgressTextOnProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
    }

    private val paintRippleEffectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private var progressTextOnBackgroundRect = Rect()
    private var progressAnimator: ValueAnimator? = null
    private var isClicked = false
    private var isDrawRippleEffect = false

    private var rippleEffectX = 0f
    private var rippleEffectY = 0f
    private var rippleEffectRadius = 0f
    private var rippleEffectRadiusMax = 0f

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
            R.styleable.ProgressButtonView,
            defStyleAttr,
            defStyleRes
        )

        drawableBackground =
            typedArray.getDrawable(R.styleable.ProgressButtonView_drawableBackground)
        drawableProgress = typedArray.getDrawable(R.styleable.ProgressButtonView_drawableProgress)

        maxProgress =
            typedArray.getInteger(R.styleable.ProgressButtonView_maxProgress, DEFAULT_MAX_PROGRESS)
        currentProgress = typedArray.getInteger(
            R.styleable.ProgressButtonView_currentProgress,
            DEFAULT_CURRENT_PROGRESS
        )
        currentProgress = checkAndFixCurrentProgress(currentProgress)

        progressText = typedArray.getText(R.styleable.ProgressButtonView_progressText).toString()
        progressTextSize = typedArray.getDimension(
            R.styleable.ProgressButtonView_progressTextSize,
            DEFAULT_TEXT_SIZE
        )
        progressTextOnBackgroundColor = typedArray.getColor(
            R.styleable.ProgressButtonView_progressTextOnBackgroundColor,
            DEFAULT_TEXT_COLOR
        )
        progressTextOnProgressColor = typedArray.getColor(
            R.styleable.ProgressButtonView_progressTextOnProgressColor,
            DEFAULT_TEXT_COLOR
        )
        progressTextStyle = ProgressTextStyle.values()[typedArray.getInt(
            R.styleable.ProgressButtonView_progressTextStyle,
            ProgressTextStyle.NORMAL.ordinal
        )]

        isUseTextOnProgress = typedArray.getBoolean(R.styleable.ProgressButtonView_useTextOnProgress, DEFAULT_BOOLEAN)

        rippleEffectColor = typedArray.getColor(R.styleable.ProgressButtonView_rippleEffectColor, DEFAULT_RIPPLE_EFFECT_COLOR)

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

        paintRippleEffectPaint.color = rippleEffectColor

        typedArray.recycle()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.i("InteresTag", "onAttachedToWindow()")
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.i("InteresTag", "onMeasure width = ${MeasureSpec.toString(widthMeasureSpec)}")
        Log.i("InteresTag", "onMeasure height =  ${MeasureSpec.toString(heightMeasureSpec)}")

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val width = measureDimension(desiredWidth, widthMeasureSpec)
        val height = measureDimension(desiredHeight, heightMeasureSpec)

        updateRippleEffectRadius(width)
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

    private fun updateRippleEffectRadius(width: Int) {
        rippleEffectRadiusMax = width * DEFAULT_RIPPLE_EFFECT_RATIO
    }

    private fun updateDrawableBackgroundBounds(width: Int, height: Int) {
        drawableBackground?.setBounds(DEFAULT_X, DEFAULT_Y, width, height)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {

        drawDrawableBackground(canvas)

        drawDrawableProgress(width, height, canvas)

        if (isDrawRippleEffect) {
            drawRippleEffect(rippleEffectX, rippleEffectY, canvas)
        }

        paintProgressTextOnBackground.getTextBounds(
            progressText,
            0,
            progressText.length,
            progressTextOnBackgroundRect
        )
        val progressTextXPos = (width / 2 - progressTextOnBackgroundRect.width() / 2).toFloat()
        val progressTextYPos = (height / 2 - ((paintProgressTextOnBackground.descent()
                + paintProgressTextOnBackground.ascent()) / 2))
        canvas.drawText(
            progressText,
            progressTextXPos,
            progressTextYPos,
            paintProgressTextOnBackground
        )

        val progressWidth = getProgressWidth(width)

        Log.i("InteresTag", "progressWidth() = $progressWidth")
        Log.i("InteresTag", "progressTextXPos = $progressTextXPos")
        val availableProgressTextLength = progressWidth - progressTextXPos
        Log.i("InteresTag", "availableProgressTextLength = $availableProgressTextLength")

        if (progressWidth > 0 && isUseTextOnProgress) {
            val textWidths = Array(progressText.length) { 0f }.toFloatArray()
            paintProgressTextOnProgress.getTextWidths(progressText, textWidths)
            val symbolsCount = calculateSymbolsCount(textWidths, availableProgressTextLength)
            Log.i("InteresTag", "symbolsCount = $symbolsCount")
            if (symbolsCount > 0) {
                canvas.drawText(
                    getCroppedText(progressText, symbolsCount),
                    progressTextXPos,
                    progressTextYPos,
                    paintProgressTextOnProgress
                )
            }
        }
    }

    private fun drawDrawableBackground(canvas: Canvas) {
        drawableBackground?.draw(canvas)
    }

    private fun drawDrawableProgress(width: Int, height: Int, canvas: Canvas) {
        val progressWidth = getProgressWidth(width)
        drawableProgress?.let { drawableProgressNotNull ->
            drawableProgressNotNull.setBounds(DEFAULT_X, DEFAULT_Y, progressWidth, height)
            drawableProgressNotNull.draw(canvas)
        }
    }

    private fun drawRippleEffect(x: Float, y: Float, canvas: Canvas) {
        canvas.drawCircle(x, y, rippleEffectRadius, paintRippleEffectPaint)

        if (rippleEffectRadius <= rippleEffectRadiusMax) {
            rippleEffectRadius += rippleEffectRadiusMax / DEFAULT_RIPPLE_EFFECT_STEP_COUNT
            invalidate()
        } else {
            rippleEffectRadius = 0F
            isDrawRippleEffect = false
            invalidate()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.i("InteresTag", "onDetachedFromWindow()")
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

    private fun calculateSymbolsCount(widthList: FloatArray, availableTextLength: Float): Int {
        var textLength = availableTextLength
        var symbolsCount = 0
        widthList.forEach { symbolWidth ->
            if (textLength > (symbolWidth / 2)) {
                textLength -= symbolWidth
                symbolsCount++
            } else {
                return symbolsCount
            }
        }
        return symbolsCount
    }

    private fun getCroppedText(text: String, symbolsCount: Int): String {
        return text.substring(0, symbolsCount)
    }

    fun setTextProgress(text: String) {
        progressText = text
        invalidate()
    }

    fun setProgress(progress: Int) {
        if (isClicked) {
            return
        }
        currentProgress = checkAndFixCurrentProgress(progress)
        invalidate()
    }

    fun setAnimateProgress(progress: Int, animateDuration: Long) {
        val current = currentProgress / progress.toFloat()
        progressAnimator = ValueAnimator.ofFloat(current, 1f).apply {
            duration = animateDuration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                val interpolation = animation.animatedValue as Float
                Log.i("InteresTag", "interpolation = $interpolation")
                setProgress((interpolation * progress).toInt())
            }
        }
        progressAnimator?.let { progressAnimatorNotNull ->
            if (!progressAnimatorNotNull.isStarted && !isClicked) {
                progressAnimatorNotNull.start()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            startRippleEffectClick(event.x, event.y)
        }
        return false
    }

    private fun startRippleEffectClick(x: Float, y: Float) {
        Log.i("InteresTag", "[ProgressButtonView] click x = $x, y = $y")
        isClicked = true
        isDrawRippleEffect = true
        rippleEffectX = x
        rippleEffectY = y
        invalidate()
    }

    enum class ProgressTextStyle {
        NORMAL, BOLD, ITALIC
    }
}