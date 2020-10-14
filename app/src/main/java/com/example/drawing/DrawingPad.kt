package com.example.drawing

import android.R
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.view.*
import java.io.ByteArrayOutputStream
import java.util.*


class DrawingPad(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mX = 0f
    private var mY = 0f
    private var mPath: Path? = null
    private var mPaint: Paint? = null
    private var currentColor = 0
    private var backgroundCol = Color.WHITE
    private var strokeWidth = 0
    private lateinit var mBitmap : Bitmap
    private lateinit var mCanvas : Canvas
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    private val paths: ArrayList<Draw> = ArrayList<Draw>()
    private val undo: ArrayList<Draw> = ArrayList<Draw>()


    init {
        mPaint = Paint()
        mPaint!!.color = Color.BLACK
        mPaint!!.style = Paint.Style.STROKE
    }

    fun initialise(displayMetrics: DisplayMetrics) {
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        currentColor = Color.BLACK
        strokeWidth = 20
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        mCanvas.drawColor(Color.WHITE)
        for (draw in paths) {
            mPaint!!.color = draw.color
            mPaint!!.strokeWidth = draw.strokeWidth.toFloat()
            mPaint!!.maskFilter = null
            mCanvas.drawPath(draw.path, mPaint!!)
        }
        canvas.drawBitmap(mBitmap, 0f, 0f, mBitmapPaint)
        canvas.restore()
    }

    //all touch events------------------------------------------------------------------------------
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
                invalidate()
            }
        }
        return true
    }
    private fun touchStart(x: Float, y: Float) {
        mPath = Path()
        val draw = Draw(currentColor, strokeWidth , mPath!!)
        paths.add(draw)
        mPath!!.reset()
        mPath!!.moveTo(x, y)
        mX = x
        mY = y
    }
    private fun touchMove(x: Float, y: Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if (dx >= 4f || dy >= 4f) {
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }
    private fun touchUp() {
        mPath!!.lineTo(mX, mY)
    }
    //----------------------------------------------------------------------------------------------

    //commands (undo,redo,setColor)----------------------------------------------------------------
    fun clear() {
        backgroundCol = Color.WHITE
        paths.clear()
        invalidate()
    }
    fun undo() {
        if (paths.size > 0) {
            undo.add(paths.removeAt(paths.size - 1))
            invalidate() // add
        } else {
            Toast.makeText(context, "Nothing to undo", Toast.LENGTH_LONG).show()
        }
    }
    fun redo() {
        if (undo.size > 0) {
            paths.add(undo.removeAt(undo.size - 1))
            invalidate() // add
        } else {
            Toast.makeText(context, "Nothing to undo", Toast.LENGTH_LONG).show()
        }
    }
    fun setColor(color: Int) {
        currentColor = color
    }
    //---------------------------------------------------------------------------------------------




}