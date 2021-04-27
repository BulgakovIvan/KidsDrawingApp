package com.example.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.kidsdrawingapp.databinding.ActivityMainBinding
import com.example.kidsdrawingapp.databinding.DialogBrushSizeBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.drawingView.setSizeForBrush(20f)

        mImageButtonCurrentPaint = binding.llPaintColors[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )

        binding.ibBrush.setOnClickListener {
            showBrushSizeChooserDialog()
        }
    }

    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        var bindingDialog = DialogBrushSizeBinding.inflate(layoutInflater)

        brushDialog.setContentView(bindingDialog.root)
        brushDialog.setTitle("Brush size:")

        val smallBtn = bindingDialog.ibSmallBrush
        smallBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(10f)
            brushDialog.dismiss()
        }

        val mediumBtn = bindingDialog.ibMediumBrush
        mediumBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(20f)
            brushDialog.dismiss()
        }

        val largeBtn = bindingDialog.ibLargeBrush
        largeBtn.setOnClickListener {
            binding.drawingView.setSizeForBrush(30f)
            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    fun paintClicked(view: View) {
        if (view != mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton

            val colorTag = imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)

            imageButton.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                    ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint = view
        }
    }
}