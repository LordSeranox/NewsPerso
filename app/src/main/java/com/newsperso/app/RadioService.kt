package com.newsperso.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class RadioService : Service() {

    private var player: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notif_channel_radio),
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopPlayback()
                stopSelf()
            }
            ACTION_PLAY -> {
                val name = intent.getStringExtra(EXTRA_NAME)
                val url = intent.getStringExtra(EXTRA_URL)
                if (name != null && url != null) play(name, url)
            }
        }
        return START_NOT_STICKY
    }

    private fun play(name: String, url: String) {
        stopPlayback()
        isBuffering = true
        isPlaying = false
        stationName = name
        stationUrl = url
        startForegroundInternal(buildNotification(name))

        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        try {
            mp.setDataSource(url)
        } catch (e: Exception) {
            stopPlayback()
            stopSelf()
            return
        }
        mp.setOnPreparedListener {
            if (stationUrl == url) {
                it.start()
                isBuffering = false
                isPlaying = true
                updateNotification()
            }
        }
        mp.setOnErrorListener { _, _, _ ->
            stopPlayback()
            stopSelf()
            true
        }
        player = mp
        Thread {
            try {
                mp.prepare()
            } catch (_: Exception) {
                if (player === mp) {
                    stopPlayback()
                    stopSelf()
                }
            }
        }.start()
    }

    private fun stopPlayback() {
        player?.let {
            try {
                if (it.isPlaying) it.stop()
                it.reset()
                it.release()
            } catch (_: Exception) {
            }
        }
        player = null
        isPlaying = false
        isBuffering = false
        stationName = null
        stationUrl = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
    }

    private fun buildNotification(name: String): Notification {
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, RadioService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val openIntent = PendingIntent.getActivity(
            this, 2,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_radio)
            .setContentTitle(getString(R.string.notif_radio_title, name))
            .setContentText(getString(R.string.notif_radio_text))
            .setOngoing(true)
            .setContentIntent(openIntent)
            .addAction(R.drawable.ic_stop, getString(R.string.radio_stopped), stopIntent)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun startForegroundInternal(notification: Notification) {
        if (Build.VERSION.SDK_INT >= 29) {
            startForeground(NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(NOTIF_ID, notification)
        }
    }

    private fun updateNotification() {
        val name = stationName ?: return
        getSystemService(NotificationManager::class.java)
            .notify(NOTIF_ID, buildNotification(name))
    }

    companion object {
        var isPlaying = false
        var isBuffering = false
        var stationName: String? = null
        var stationUrl: String? = null

        private const val CHANNEL_ID = "radio_playback"
        private const val NOTIF_ID = 42
        private const val ACTION_PLAY = "com.newsperso.app.PLAY"
        private const val ACTION_STOP = "com.newsperso.app.STOP"
        private const val EXTRA_NAME = "name"
        private const val EXTRA_URL = "url"

        fun start(context: Context, name: String, url: String) {
            val intent = Intent(context, RadioService::class.java)
                .setAction(ACTION_PLAY)
                .putExtra(EXTRA_NAME, name)
                .putExtra(EXTRA_URL, url)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, RadioService::class.java).setAction(ACTION_STOP)
            )
        }
    }
}
