package com.example.sachin.heart

import android.graphics.*
import com.example.sachin.heart.ImageProcessing
import java.io.ByteArrayOutputStream

/**
 * This abstract class is used to process images.
 *
 * @author Justin Wetherell <phishman3579></phishman3579>@gmail.com>
 */
object ImageProcessing {
    private fun decodeYUV420SPtoRedSum(yuv420sp: ByteArray?, width: Int, height: Int): Int {
        if (yuv420sp == null) return 0
        val frameSize = width * height
        var sum = 0
        var j = 0
        var yp = 0
        while (j < height) {
            var uvp = frameSize + (j shr 1) * width
            var u = 0
            var v = 0
            var i = 0
            while (i < width) {
                var y = (0xff and yuv420sp[yp].toInt()) - 16
                if (y < 0) y = 0
                if (i and 1 == 0) {
                    v = (0xff and yuv420sp[uvp++].toInt()) - 128
                    u = (0xff and yuv420sp[uvp++].toInt()) - 128
                }
                val y1192 = 1192 * y
                var r = y1192 + 1634 * v
                var g = y1192 - 833 * v - 400 * u
                var b = y1192 + 2066 * u
                if (r < 0) r = 0 else if (r > 262143) r = 262143
                if (g < 0) g = 0 else if (g > 262143) g = 262143
                if (b < 0) b = 0 else if (b > 262143) b = 262143
                val pixel =
                    -0x1000000 or (r shl 6 and 0xff0000) or (g shr 2 and 0xff00) or (b shr 10 and 0xff)
                val red = pixel shr 16 and 0xff
                sum += red
                i++
                yp++
            }
            j++
        }
        return sum
    }

    /**
     * Given a byte array representing a yuv420sp image, determine the average
     * amount of red in the image. Note: returns 0 if the byte array is NULL.
     *
     * @param yuv420sp
     * Byte array representing a yuv420sp image
     * @param width
     * Width of the image.
     * @param height
     * Height of the image.
     * @return int representing the average amount of red in the image.
     */
    fun decodeYUV420SPtoRedAvg(yuv420sp: ByteArray?, height: Int, width: Int): Int {
        if (yuv420sp == null) return 0
        val frameSize = width * height
        val sum = decodeYUV420SPtoRedSum(yuv420sp, width, height)
        return sum / frameSize
    }

    fun decodeRedFromRGBBitmap(data: ByteArray?, height: Int, width: Int): Int {
        if (data == null) return 0

        val frameSize = width * height
        var summaryRedLevel: Long = 0

        var yuvimage = YuvImage(data, ImageFormat.NV21, width, height, null)
        val baos = ByteArrayOutputStream()
        yuvimage.compressToJpeg(Rect(0, 0, width, height), 80, baos)
        val jdata: ByteArray = baos.toByteArray()

        var img = BitmapFactory.decodeByteArray(jdata, 0, jdata.size);

        for(y in 0 until height) {
            for(x in 0 until width) {
                var colours = img.getPixel(x, y)
                var red = Color.red(colours)
                summaryRedLevel += red
            }
        }
        return (summaryRedLevel/(width*height)).toInt()
    }
}