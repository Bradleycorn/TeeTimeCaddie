package net.bradball.teetimecaddie.android.feature.auth.createAccount

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

/**
 * Turns a photo the person picked into JPEG bytes small enough to upload.
 *
 * Exists so `ContentResolver` and `Bitmap` stay out of the ViewModel and out of the KMP SDK: the
 * SDK's `completeSignUp` takes bytes, deliberately knowing nothing about how a platform produces
 * them.
 *
 * A camera photo off a modern phone is several megabytes and thousands of pixels square, for an
 * image the app only ever draws at 88dp. Downscaling here keeps that off the wire and out of
 * storage.
 */
@Singleton
class ProfilePhotoReader @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    /**
     * Reads [uri] and returns it as a downscaled JPEG, or null if it could not be read.
     *
     * Null rather than an exception: a photo is optional, so a picture that won't decode should
     * cost the person their photo, not their sign-up.
     */
    suspend fun read(uri: Uri): ByteArray? = withContext(Dispatchers.IO) {
        runCatching {
            val bitmap = decodeDownsampled(uri) ?: return@runCatching null
            ByteArrayOutputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
                bitmap.recycle()
                out.toByteArray()
            }
        }.getOrNull()
    }

    /**
     * Decodes [uri] at roughly [MAX_DIMENSION], never larger than the source.
     *
     * Two passes: the first reads only the header for the dimensions, so the full image is never
     * held at full size. `inSampleSize` halves, so this lands within a factor of two of the target
     * and the result is then scaled exactly.
     */
    private fun decodeDownsampled(uri: Uri): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        val sourceSize = max(bounds.outWidth, bounds.outHeight)
        if (sourceSize <= 0) return null

        val options = BitmapFactory.Options().apply {
            inSampleSize = generateSequence(1) { it * 2 }
                .first { sourceSize / it <= MAX_DIMENSION * 2 }
        }

        val decoded = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return null

        val decodedSize = max(decoded.width, decoded.height)
        if (decodedSize <= MAX_DIMENSION) return decoded

        val scale = MAX_DIMENSION.toFloat() / decodedSize
        val scaled = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true,
        )
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }

    private companion object {
        /** Comfortably more than the 88dp the avatar is ever drawn at, even on a 4x density screen. */
        const val MAX_DIMENSION = 512

        const val JPEG_QUALITY = 85
    }
}
