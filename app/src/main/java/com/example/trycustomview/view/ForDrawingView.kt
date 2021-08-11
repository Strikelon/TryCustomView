package com.example.trycustomview.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class ForDrawingView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAttrs: Int = 0,
): View(context, attributeSet, defStyleAttrs) {

    companion object {
        private const val RECT_WIDTH = 100f
        private const val RECT_HEIGHT = 200f
    }

    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG)
    private val paintStrokeStyle = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    private var rectLeft = 350f
    private var rectTop = 300f
    private var rectTransStart = false
    private var deltaX = 0f
    private var deltaY = 0f

    private val textToWrite = "text left"

    init {
        p.isAntiAlias = true
        p.color = Color.RED
        // толщина линии = 10
        p.strokeWidth = 10f

        paintText.isAntiAlias = true
        paintText.color = Color.BLUE
        paintText.strokeWidth = 3f
        p.textSize = 30f

        paintStrokeStyle.isAntiAlias = true
        paintStrokeStyle.color = Color.GREEN
        paintStrokeStyle.strokeWidth = 10f
        paintStrokeStyle.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        Log.i("InteresTag","onDraw width = $width")
        Log.i("InteresTag","onDraw height = $height")
        canvas.drawARGB(80, 102, 204, 255)
//        canvas.drawPoint(50f, 50f, p)
//        canvas.drawLine(100f,100f,500f,50f,p)
//        canvas.drawCircle(100f, 200f, 50f, p)
//        canvas.drawRect(200f, 150f, 400f, 200f, p)
        rectF.set(rectLeft, rectTop, rectLeft + RECT_WIDTH, rectTop + RECT_HEIGHT)
        canvas.drawRoundRect(rectF, 20f, 20f, p)
//        rectF.offset(-150f, 0f)
//        canvas.drawOval(rectF, paintStrokeStyle)
//        rectF.offset(-150f, 0f)
//        canvas.drawArc(rectF, 45f, 270f, false, p)

//        val textWidth = paintText.measureText(textToWrite)
//        Log.i("InteresTag","textWidth = $textWidth")
//        val textWidths = Array<Float>(textToWrite.length){0f}.toFloatArray()
//        paintText.getTextWidths(textToWrite, textWidths)
//        textWidths.forEachIndexed { index, fl ->
//            Log.i("InteresTag","textWidths [$index] = $fl")
//        }
//        canvas.drawText(textToWrite, 150f, 500f, paintText)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.i("InteresTag","dispatchTouchEvent event = ACTION_DOWN")
                Log.i("InteresTag","dispatchTouchEvent event x = ${event.x}")
                Log.i("InteresTag","dispatchTouchEvent event y = ${event.y}")
                if (rectF.contains(event.x, event.y)) {
                    Log.i("InteresTag","dispatchTouchEvent event = ACTION_DOWN true")
                    deltaX = event.x - rectF.left
                    deltaY = event.y - rectF.top
                    rectTransStart = true
                } else {
                    rectTransStart = false
                    releaseDelta()
                }
            }
            MotionEvent.ACTION_UP -> {
                Log.i("InteresTag","dispatchTouchEvent event = ACTION_UP")
                Log.i("InteresTag","dispatchTouchEvent event x = ${event.x}")
                Log.i("InteresTag","dispatchTouchEvent event y = ${event.y}")
                rectTransStart = false
                releaseDelta()
            }
            MotionEvent.ACTION_SCROLL -> {
                Log.i("InteresTag","dispatchTouchEvent event = ACTION_SCROLL")
                Log.i("InteresTag","dispatchTouchEvent event x = ${event.x}")
                Log.i("InteresTag","dispatchTouchEvent event y = ${event.y}")
            }
            MotionEvent.ACTION_MOVE -> {
                Log.i("InteresTag","dispatchTouchEvent event = ACTION_MOVE")
                Log.i("InteresTag","dispatchTouchEvent event x = ${event.x}")
                Log.i("InteresTag","dispatchTouchEvent event y = ${event.y}")
                if (rectTransStart) {
                    rectLeft = event.x - deltaX
                    rectTop = event.y - deltaY
                    invalidate()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun releaseDelta() {
        deltaX = 0f
        deltaY = 0f
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

        setMeasuredDimension(measureDimension(desiredWidth, widthMeasureSpec),
                measureDimension(desiredHeight, heightMeasureSpec))
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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.i("InteresTag","onDetachedFromWindow()")
    }

}