package com.gr.passwordview.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.InputType
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.BaseInputConnection
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.gr.passwordview.R
import com.gr.passwordview.util.dp2px
import com.gr.passwordview.util.sp2px
import java.util.*

/**
 * Created by gr on 2017/5/22.
 */
class PasswordView(context: Context, attrs: AttributeSet) : View(context, attrs),View.OnKeyListener {
    val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val height = dp2px(40f)
    val inputManager=context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    var passwordLength = 4
    var passwordVisible = false

    var lineWidth = dp2px(40f)
    val lineHeight = dp2px(1.5f)
    var lineSpace = dp2px(15f)
    val lineColor = Color.parseColor("#212121")

    val textSize = sp2px(20f)
    val textColor = Color.parseColor("#353535")
    val encipher_text = "*"
    var passwords = ArrayList<String>()

    val cursorWidth = dp2px(1f)
    val cursorHeight = dp2px(20f)
    val cursorColor = Color.parseColor("#4f4f4f")
    var cursorVisible = false
    val timer = Timer()
    val cursorDuration=600L
    val cursorTask = object : TimerTask() {
        override fun run() {
            cursorVisible = !cursorVisible
            postInvalidate()
        }
    }

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PasswordView)
        passwordLength = array.getInteger(R.styleable.PasswordView_password_length, 4)
        passwordVisible = array.getBoolean(R.styleable.PasswordView_password_visible, false)
        array.recycle()

        isFocusableInTouchMode = true
        setOnKeyListener(this)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        timer.scheduleAtFixedRate(cursorTask,0,cursorDuration)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 0
        val specMode = MeasureSpec.getMode(widthMeasureSpec)
        when (specMode) {
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST ->
                width = (lineWidth * passwordLength + lineSpace * (passwordLength - 1)).toInt()
            MeasureSpec.EXACTLY -> {
                width = MeasureSpec.getSize(widthMeasureSpec)
                val originalWidth = lineWidth * passwordLength + lineSpace * (passwordLength - 1)
                val scale = width / originalWidth
                passwordLength = (passwordLength / scale).toInt()
                lineSpace /= scale
            }
        }
        setMeasuredDimension(width, height.toInt())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawLine(canvas)
        drawText(canvas)
        drawCursor(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action==MotionEvent.ACTION_DOWN){
            requestFocus()
            inputManager.showSoftInput(this,InputMethodManager.SHOW_FORCED)
            return true
        }
        return super.onTouchEvent(event)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus){
            inputManager.hideSoftInputFromWindow(windowToken,0)
        }
    }

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        outAttrs.inputType=InputType.TYPE_CLASS_NUMBER
        outAttrs.imeOptions=EditorInfo.IME_ACTION_DONE
        return BaseInputConnection(this,false)
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action!=KeyEvent.ACTION_DOWN) return false
        val size=passwords.size
        when(event.keyCode){
            KeyEvent.KEYCODE_DEL->{
                if (size==0) return true
                passwords.removeAt(size-1)
                postInvalidate()
                return true
            }
            in KeyEvent.KEYCODE_0..KeyEvent.KEYCODE_9->{
                if (size==passwordLength) return true
                passwords.add((keyCode-7).toString())
                postInvalidate()
                return true
            }
            KeyEvent.KEYCODE_ENTER->{
                if (size!=passwordLength) return true
                Toast.makeText(context,passwords.joinToString(""),Toast.LENGTH_SHORT).show()
                passwords.clear()
                postInvalidate()
                return true
            }
            else->return false
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        timer.cancel()
    }

    private fun drawCursor(canvas: Canvas) {
        paint.color = cursorColor
        paint.strokeWidth = cursorWidth
        if (cursorVisible) {
            val x = lineWidth / 2f + (lineWidth + lineSpace) * passwords.size.toFloat()
            val startY = (height - cursorHeight) / 2f
            val stopY = startY + cursorHeight
            canvas.drawLine(x, startY, x, stopY, paint)
        }
    }

    private fun drawText(canvas: Canvas) {
        paint.color = textColor
        paint.textSize = textSize
        paint.textAlign = Paint.Align.CENTER

        val rect = Rect()
        canvas.getClipBounds(rect)
        val canvasHeight = rect.height()
        paint.getTextBounds(encipher_text, 0, encipher_text.length, rect)
        val y = canvasHeight / 2f + rect.height() / 2f - rect.bottom

        for (i in 0..passwords.size - 1) {
            val x = lineWidth / 2f + (lineWidth + lineSpace) * i.toFloat()
            if (passwordVisible) {
                canvas.drawText(passwords[i], x, y, paint)
            } else {
                canvas.drawText(encipher_text, x, y, paint)
            }
        }
    }

    private fun drawLine(canvas: Canvas) {
        paint.color = lineColor
        paint.strokeWidth = lineHeight
        for (i in 0..passwordLength - 1) {
            val startX = (lineWidth + lineSpace) * i
            val stopX = startX + lineWidth
            val y = height - lineHeight / 2
            canvas.drawLine(startX, y, stopX, y, paint)
        }
    }
}