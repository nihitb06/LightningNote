package com.dev.nihitb06.newlightningnote.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.v8.renderscript.Allocation
import android.support.v8.renderscript.Element
import android.support.v8.renderscript.RenderScript
import android.support.v8.renderscript.ScriptIntrinsicBlur
import android.view.View

class ImageProcessingUtils {

    companion object {
        private const val BITMAP_SCALE = 0.4f
        private const val BLUR_RADIUS = 7.5f

        fun blurScreenshot(context: Context, view: View): Bitmap = blur(context, takeScreenshot(view))

        private fun takeScreenshot(view: View): Bitmap {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            view.draw(canvas)

            return bitmap
        }

        private fun blur(context: Context, bitmap: Bitmap): Bitmap {
            val width = Math.round(bitmap.width * BITMAP_SCALE)
            val height = Math.round(bitmap.height * BITMAP_SCALE)

            val inputBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
            val outputBitmap = Bitmap.createBitmap(inputBitmap)

            val renderScript = RenderScript.create(context)
            val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))

            val tempIn = Allocation.createFromBitmap(renderScript, inputBitmap)
            val tempOut = Allocation.createFromBitmap(renderScript, outputBitmap)

            scriptIntrinsicBlur.setRadius(BLUR_RADIUS)
            scriptIntrinsicBlur.setInput(tempIn)
            scriptIntrinsicBlur.forEach(tempOut)

            tempOut.copyTo(outputBitmap)

            return outputBitmap
        }
    }
}