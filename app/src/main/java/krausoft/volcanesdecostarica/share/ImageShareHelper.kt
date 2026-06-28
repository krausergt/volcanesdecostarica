package krausoft.volcanesdecostarica.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val AUTHORITY = "krausoft.volcanesdecostarica.fileprovider"
private const val SHARE_FILE = "share_image.jpg"
private const val DATE_FORMAT = "dd-MM-yyyy, HH:mm:ss"

/**
 * Guarda el bitmap actual en el directorio de caché de la app y lanza un Intent
 * de compartir con la imagen adjunta y el texto formateado como:
 * "{shareMessage} [dd-MM-yyyy, HH:mm:ss]"
 *
 * Se re-encodea siempre a JPEG en vez de depender del archivo interno de Coil,
 * cuya ruta/nombre no está garantizada.
 *
 * @param context contexto de la aplicación (no de la Activity, para evitar leaks).
 * @param bitmap imagen a compartir (obtenida del painter state de Coil).
 * @param shareMessage texto base del mensaje (p. ej. "Volcán Turrialba").
 */
fun shareImage(context: Context, bitmap: Bitmap, shareMessage: String): Intent {
    val file = File(context.cacheDir, SHARE_FILE).also { f ->
        f.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
    }
    val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
    val date = SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(Date())
    val text = "$shareMessage [$date]"
    return Intent(Intent.ACTION_SEND).apply {
        type = "image/jpeg"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
}
