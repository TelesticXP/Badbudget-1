package com.example.badbudget

import android.graphics.Color
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File

class ReceiptViewerActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        val img = ImageView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            setOnClickListener { finish() }
        }
        setContentView(img)

        intent.getStringExtra("path")?.let { Glide.with(this).load(File(it)).into(img) }
    }
}
