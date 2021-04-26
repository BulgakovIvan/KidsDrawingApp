package com.example.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


class DrawingView(context: Context, attrs: AttributeSet): View(context, attrs) {
//    The Path class encapsulates compound (multiple contour) geometric paths
//    consisting of straight line segments, quadratic curves, and cubic curves.
//    It can be drawn with canvas.drawPath(path, paint), either filled or stroked
//    (based on the paint's Style), or it can be used for clipping or to draw text on a path.
    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null

// The Paint class holds the style and color information
// about how to draw geometries, text and bitmaps.
    private var mDrawPaint: Paint? = null
    private  var mCanvasPaint: Paint? = null
    private var mBrushSize: Float = 20f
    private var color = Color.BLACK
    private var canvas: Canvas? = null

    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPath = CustomPath(color, mBrushSize)

        mDrawPaint = Paint()
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND //The Join specifies the treatment where lines and curve segments join on a stroked path.
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND //	The Cap specifies the treatment for the beginning and ending of stroked lines and paths.

        mCanvasPaint = Paint(Paint.DITHER_FLAG)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint!!)
        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
            }

            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }

            MotionEvent.ACTION_UP -> {
                mDrawPath = CustomPath(color, mBrushSize)
            }
            else -> return false
        }

        invalidate()
        return true
    }

    internal inner class CustomPath(var color: Int,
                                    var brushThickness: Float): Path() {

    }


}