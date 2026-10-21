package com.finmojarra.app.data.goals

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max

/**
 * Prepara la imagen de una meta: la lee de la galería, la endereza según el EXIF, la reduce
 * y la comprime a JPEG para que quepa en un documento de Firestore (límite de 1 MiB).
 * Llamar siempre desde un hilo de fondo.
 */
object GoalImageCodec {
    private const val MAX_SIDE = 900
    private const val MAX_BYTES = 400_000

    /** Imagen reducida a [maxSide] px por el lado más largo y con la orientación correcta. */
    fun decode(context: Context, source: Uri, maxSide: Int = MAX_SIDE): Bitmap? {
        val resolver = context.applicationContext.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > maxSide * 2 || bounds.outHeight / sample > maxSide * 2) {
            sample *= 2
        }

        val decoded = resolver.openInputStream(source)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null

        val orientation = try {
            resolver.openInputStream(source)?.use {
                ExifInterface(it).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (e: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
        val degrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

        val matrix = Matrix()
        if (degrees != 0f) matrix.postRotate(degrees)
        val longest = max(decoded.width, decoded.height)
        if (longest > maxSide) {
            val scale = maxSide.toFloat() / longest
            matrix.postScale(scale, scale)
        }
        return Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
    }

    /** JPEG en Base64 listo para guardar en Firestore, o null si no se pudo leer la imagen. */
    fun toBase64(context: Context, source: Uri): String? {
        val bitmap = decode(context, source) ?: return null
        var quality = 85
        var bytes: ByteArray
        do {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            bytes = out.toByteArray()
            quality -= 10
        } while (bytes.size > MAX_BYTES && quality >= 35)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}
