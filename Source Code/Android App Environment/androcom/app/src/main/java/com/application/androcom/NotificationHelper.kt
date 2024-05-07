package com.application.androcom


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.*


class NotificationHelper(
    private val context: Context,
    private val channelId: String,
    private val channelName: String,
    private val notificationIcon: Int
) {

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "AndroCom Notification Channel"
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(notificationIcon)
            .setContentTitle(title)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            ) // Handles long strings properly
            .setContentText(
                message.replace(
                    "\\n",
                    "\n"
                )
            ) // Replace "\\n" with actual newline character
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(/* Notification ID */ 1, notificationBuilder.build())
    }
    fun showNotificationx(context: Context, title: String, message: String, letter: Char) {
        val notificationIcon = createCircularLetterIcon(context, letter)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)

            .setSmallIcon(R.drawable.notification)
            .setLargeIcon(notificationIcon)
            .setContentTitle(title)
            .setContentText(message.replace("\\n", "\n")) // Replace "\\n" with actual newline character

            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // Handles long strings properly
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)


        notificationManager.notify(/* Notification ID */ 1, notificationBuilder.build())
    }

    fun createCircularLetterIcon(context: Context, letter: Char): Bitmap {
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw circular background
        val backgroundPaint = Paint().apply {
            color = Color.parseColor("#FF6650a4")
            isAntiAlias = true
        }
        val radius = Math.min(bitmap.width, bitmap.height) / 2f
        canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, radius, backgroundPaint)

        // Draw letter text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 90f // Adjust text size as needed
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val textBounds = Rect()
        textPaint.getTextBounds(letter.toString(), 0, 1, textBounds)
        val xPos = bitmap.width / 2f
        val yPos = (bitmap.height / 2f - (textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(letter.toString(), xPos, yPos, textPaint)

        return bitmap
    }
}
