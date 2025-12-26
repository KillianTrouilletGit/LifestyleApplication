package com.example.personallevelingsystem.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream

fun createImageFile(context: Context): Uri {
    val imagePath = File(context.cacheDir, "images")
    imagePath.mkdirs()
    val file = File(imagePath, "food_image_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
