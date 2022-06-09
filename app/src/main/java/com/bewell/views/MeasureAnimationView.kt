package com.bewell.views

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.bewell.R
import com.bewell.storage.Preferences

class MeasureAnimationView @JvmOverloads constructor(context: Context,
                                           attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {

    private val paint = Paint()
    private val primaryColour = TypedValue()
    private val surfaceColour = TypedValue()
    private val path = Path()
    private val mTextPaint = TextPaint()

    private var a = 400F
    private var b = 400F
    private var thickness = 20F
    private var beatWidth = 120F
    private var deltaX = 0.0F


    private val mText = resources.getString(R.string.put_finger_on_camera)

    // Called when the view should render its content.
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        var sharedPreferences = context.getSharedPreferences(Preferences.FILE_NAME, Context.MODE_PRIVATE)

        val width = measuredWidth.toFloat()
        val height = measuredHeight.toFloat()

        context.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, primaryColour, true)
        context.theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, surfaceColour, true)

        val going =
            sharedPreferences.getBoolean(Preferences.PREF_GOING, true)
        //println(going)

        if(going) {

            path.reset()


            var shader = LinearGradient(
                width / 2 - a / 2, height / 2, width / 2 + a / 2, height / 2,
                surfaceColour.data, primaryColour.data, Shader.TileMode.MIRROR
            )

            ///////

            paint.style = Paint.Style.FILL_AND_STROKE
            paint.shader = shader
            canvas!!.drawRect(
                width / 2 - a / 2,
                height / 2 - b / 2,
                width / 2 + a / 2,
                height / 2 + b / 2,
                paint
            )

            paint.shader = null
            paint.color = surfaceColour.data
            canvas.drawRect(
                deltaX + width / 2 - a / 2,
                height / 2 + thickness / 2,
                deltaX + width / 2 + a / 2,
                height / 2 + b / 2,
                paint
            )
            canvas.drawRect(
                deltaX + width / 2 - a / 2,
                height / 2 - b / 2,
                deltaX + width / 2 + a / 2,
                height / 2 - thickness / 2,
                paint
            )

            path.moveTo(deltaX + width / 2 + a / 2, height / 2 - b / 2)
            path.lineTo(deltaX + width / 2 + a / 2 + beatWidth / 2, height / 2 - b / 2)
            path.lineTo(deltaX + width / 2 + a / 2, height / 2 - thickness / 2)

            path.moveTo(deltaX + beatWidth + width / 2 + a / 2, height / 2 - b / 2)
            path.lineTo(deltaX + beatWidth + width / 2 + a / 2, height / 2 - thickness / 2)
            path.lineTo(deltaX + beatWidth / 2 + width / 2 + a / 2, height / 2 - b / 2)

            path.moveTo(deltaX + width / 2 + a / 2, height / 2 + thickness / 2)
            path.lineTo(
                deltaX + beatWidth / 2 + width / 2 + a / 2,
                height / 2 - b / 2 + thickness
            )
            path.lineTo(deltaX + beatWidth + width / 2 + a / 2, height / 2 + thickness / 2)

            canvas!!.drawRect(
                deltaX + width / 2 + a / 2,
                height / 2 + thickness / 2,
                deltaX + beatWidth + width / 2 + a / 2,
                height / 2 + b / 2,
                paint
            )

            ////////////

            //paint.color = Color.WHITE
            canvas!!.drawRect(
                deltaX + beatWidth + width / 2 + a / 2,
                height / 2 + thickness / 2,
                deltaX + beatWidth + width / 2 + 1.5F * a,
                height / 2 + b / 2,
                paint
            )
            canvas!!.drawRect(
                deltaX + beatWidth + width / 2 + a / 2,
                height / 2 - b / 2,
                deltaX + beatWidth + width / 2 + 1.5F * a,
                height / 2 - thickness / 2,
                paint
            )

            path.moveTo(deltaX + width / 2 + 1.5F * a + beatWidth, height / 2 - b / 2)
            path.lineTo(deltaX + width / 2 + 1.5F * a + 1.5F * beatWidth, height / 2 - b / 2)
            path.lineTo(deltaX + width / 2 + 1.5F * a + beatWidth, height / 2 - thickness / 2)

            path.moveTo(deltaX + 2F * beatWidth + width / 2 + 1.5F * a, height / 2 - b / 2)
            path.lineTo(
                deltaX + 2F * beatWidth + width / 2 + 1.5F * a,
                height / 2 - thickness / 2
            )
            path.lineTo(deltaX + 1.5F * beatWidth + width / 2 + 1.5F * a, height / 2 - b / 2)

            path.moveTo(deltaX + beatWidth + width / 2 + 1.5F * a, height / 2 + thickness / 2)
            path.lineTo(
                deltaX + 1.5F * beatWidth + width / 2 + 1.5F * a,
                height / 2 - b / 2 + thickness
            )
            path.lineTo(
                deltaX + 2F * beatWidth + width / 2 + 1.5F * a,
                height / 2 + thickness / 2
            )

            canvas!!.drawRect(
                deltaX + beatWidth + width / 2 + 1.5F * a,
                height / 2 + thickness / 2,
                deltaX + 2 * beatWidth + width / 2 + 1.5F * a,
                height / 2 + b / 2,
                paint
            )

            ///////////

            canvas.drawPath(path, paint)

            path.close()

            deltaX -= 5F
            if (deltaX <= -a - beatWidth) deltaX = 0.0F
        }
        else {
            val textX = width/2 - mTextPaint.measureText(mText)/2
            val textY = height/2

            mTextPaint.color = primaryColour.data
            mTextPaint.textSize = 70F
            mTextPaint.typeface = ResourcesCompat.getFont(context, R.font.montserrat_semi_bold)

            val sb = StaticLayout.Builder.obtain(mText, 0, mText.length, mTextPaint, width.toInt())
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
            val mStaticLayout = sb.build()

            canvas!!.translate(textX, textY);
            mStaticLayout.draw(canvas);
        }
        invalidate()
    }
}