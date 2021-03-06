package com.example.trycustomview.view

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.trycustomview.R

class PlusScreenButtonView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAtrr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAtrr) {

    private val buttonText: TextView
    private val buttonImage: ImageView
    private val container: ConstraintLayout
    private var listener: OnClickListener? = null

    init {
        Log.i("InteresTag","init")
        inflate(context, R.layout.item_plus_screen_button, this)
        buttonImage = findViewById(R.id.plusScreenImage)
        buttonText = findViewById(R.id.plusScreenText)
        val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.PlusScreenButtonView
        )
        buttonText.text = typedArray.getText(R.styleable.PlusScreenButtonView_description)
        buttonImage.setImageResource(
                typedArray.getResourceId(
                        R.styleable.PlusScreenButtonView_imageDescription,
                        0)
        )
        container = findViewById(R.id.plusContainer)
        container.setOnClickListener {
            Log.i("InteresTag","container clickListener true")
        }
        typedArray.recycle()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        Log.i("InteresTag","dispatchTouchEvent event.action = ${event.action}")
        if (event.action == MotionEvent.ACTION_UP) {
            Log.i("InteresTag","MotionEvent.ACTION_UP")
            listener?.onClick(this)
        }
        if (event.action == MotionEvent.ACTION_MOVE) {
            Log.i("InteresTag","MotionEvent.ACTION_MOVE)")
        }
        if (event.action == MotionEvent.ACTION_SCROLL) {
            Log.i("InteresTag","ACTION_SCROLL")
        }
        if (event.action == MotionEvent.ACTION_OUTSIDE) {
            Log.i("InteresTag","ACTION_OUTSIDE")
        }
        return super.dispatchTouchEvent(event)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        this.listener = listener
    }
}