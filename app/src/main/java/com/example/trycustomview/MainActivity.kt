package com.example.trycustomview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.trycustomview.view.ProgressButtonView

class MainActivity : AppCompatActivity() {

    private lateinit var progressButtonView: ProgressButtonView
    private lateinit var changeTextButton: Button
    private lateinit var changeProgressButton: Button
    private lateinit var changeProgressAnimate: Button

    private var progressValue: Int = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progressButtonView = findViewById(R.id.progressButtonView)
        changeTextButton = findViewById(R.id.changeTextButton)
        changeProgressButton = findViewById(R.id.changeProgressButton)
        changeProgressAnimate = findViewById(R.id.changeProgressAnimate)

        progressButtonView.setOnClickListener {
            Log.i("InteresTag","progressButtonView click")
        }

        changeTextButton.setOnClickListener {
            progressButtonView.setTextProgress("Hello World")
        }
        changeProgressButton.setOnClickListener {
            progressButtonView.setProgress(progressValue)
            progressValue += 5
        }
        changeProgressAnimate.setOnClickListener {
            progressButtonView.setAnimateProgress(progressValue, 500)
            progressValue -= 5
        }
    }
}