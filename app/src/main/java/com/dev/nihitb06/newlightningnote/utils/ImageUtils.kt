package com.dev.nihitb06.newlightningnote.utils

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImageUtils {

    companion object {
        private const val ATTACHMENT_HEIGHT = 600
        private const val ATTACHMENT_WIDTH_GRID = 550
        private const val ATTACHMENT_WIDTH_LIST = 1100

        fun createImageFile(context: Context): File? {
            try {
                return File.createTempFile(
                        "Attachment_" + SimpleDateFormat.getDateTimeInstance().format(Date()),
                        ".jpg",
                        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                )
            } catch (e: FileAlreadyExistsException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return null
        }

        fun setImage(context: Context, imageView: ImageView?, filePath: String, linearList: Boolean) {
            val targetWidth = imageView?.measuredWidth ?: 0
            val targetHeight = imageView?.measuredHeight ?: 0

            val bmOptions = BitmapFactory.Options()
            bmOptions.inJustDecodeBounds = true

            try {
                BitmapFactory.decodeFile(filePath, bmOptions)
                val photoWidth = bmOptions.outWidth
                val photoHeight = bmOptions.outHeight

                val scaleFactor = try {
                    Math.min(photoWidth/targetWidth, photoHeight/targetHeight)
                } catch (e: ArithmeticException) {
                    Math.min(photoWidth/(if(linearList) ATTACHMENT_WIDTH_LIST else ATTACHMENT_WIDTH_GRID), photoHeight/ ATTACHMENT_HEIGHT)
                }

                bmOptions.inJustDecodeBounds = false
                bmOptions.inSampleSize = scaleFactor

                (context as Activity).runOnUiThread { imageView?.setImageBitmap(BitmapFactory.decodeFile(filePath, bmOptions)) }
            } catch (e: FileNotFoundException) {
                Toast.makeText(context, "One or more Attachments seem to be missing", Toast.LENGTH_SHORT).show()
                (context as Activity).runOnUiThread { imageView?.visibility = View.GONE }
            }
        }

        fun compressImageFile(filePath: String) {
            val bmp = decodeScaledBitmap(filePath)
            val fileOutputStream = FileOutputStream(filePath)
            bmp.compress(Bitmap.CompressFormat.JPEG, 70, fileOutputStream)

            fileOutputStream.flush()
            fileOutputStream.close()
        }

        private fun decodeScaledBitmap(filePath: String): Bitmap {
            val bitmapFactoryOptions = BitmapFactory.Options()
            bitmapFactoryOptions.inJustDecodeBounds = true

            BitmapFactory.decodeFile(filePath, bitmapFactoryOptions)
            bitmapFactoryOptions.inSampleSize = calculateSampleSize(bitmapFactoryOptions)

            bitmapFactoryOptions.inJustDecodeBounds = false
            var bitmap = BitmapFactory.decodeFile(filePath, bitmapFactoryOptions)

            val orientation = ExifInterface(filePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)
            val matrix = Matrix()
            when (orientation) {
                6 -> matrix.postRotate(90.0f)
                3 -> matrix.postRotate(180.0f)
                8 -> matrix.postRotate(270.0f)
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            return bitmap
        }

        private fun calculateSampleSize(options: BitmapFactory.Options): Int {
            val height = options.outHeight
            val width = options.outWidth
            var sampleSize = 1

            if(height > 816.0f || width > 612.0f) {
                val halfHeight = height / 2
                val halfWidth = width / 2

                while(halfHeight/sampleSize >= 816.0f && halfWidth/sampleSize >= 612.0f)
                    sampleSize *= 2
            }

            return sampleSize
        }
    }
}