package noid.simplify.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.math.roundToInt

object ImageRotator {

    private const val MAX_HEIGHT = 1024
    private const val MAX_WIDTH = 1024


    /**
     * This method is responsible for solving the rotation issue if exist. Also scale the images to
     * 1024x1024 resolution
     *
     * @throws IOException
     * @throws SecurityException
     */
    @Throws(IOException::class, SecurityException::class)
    fun handleBitmapDownSamplingAndRotation(imageFile: File): Bitmap? {

        // First decode with inJustDecodeBounds=true to check dimensions
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        FileInputStream(imageFile).use { imageStream ->
            BitmapFactory.decodeStream(imageStream, null, options)
        }

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options)

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false
        val bitmap = FileInputStream(imageFile).use { imageStream ->
            BitmapFactory.decodeStream(imageStream, null, options)
        }

        return bitmap?.let { rotateImageIfRequired(it, imageFile) }
    }

    /**
     * Calculate an inSampleSize for use in a [BitmapFactory.Options] object when decoding
     * bitmaps using the decode* methods from [BitmapFactory]. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     * method with inJustDecodeBounds==true
     * @return The value to be used for inSampleSize
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth

        return if (height > MAX_HEIGHT || width > MAX_WIDTH) {
            val heightRatio = (height.toFloat() / MAX_HEIGHT.toFloat()).roundToInt()
            val widthRatio = (width.toFloat() / MAX_WIDTH.toFloat()).roundToInt()
            val inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
            val totalPixels = (width * height).toFloat()
            val totalReqPixelsCap = (MAX_WIDTH * MAX_HEIGHT * 2).toFloat()
            calculateInSampleSize(totalPixels, inSampleSize, totalReqPixelsCap)
        } else 1
    }

    private tailrec fun calculateInSampleSize(
            totalPixels: Float,
            inSampleSize: Int,
            totalRequiredPixelsCap: Float
    ): Int {
        val tooBig = totalRequiredPixelsCap < totalPixels / (inSampleSize * inSampleSize)
        return if (tooBig) calculateInSampleSize(
                totalPixels,
                inSampleSize + 1,
                totalRequiredPixelsCap
        ) else inSampleSize
    }

    @Throws(IOException::class, SecurityException::class)
    private fun rotateImageIfRequired(img: Bitmap, imageFile: File): Bitmap {
        val ei = FileInputStream(imageFile).use { ExifInterface(it) }
        val orientation = ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
        )

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }
}