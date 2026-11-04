package com.newsperso.app

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.newsperso.app.data.RadioStation
import com.newsperso.app.data.SettingsPrefs
import com.newsperso.app.data.SourcesRepository
import com.newsperso.app.databinding.ActivityRadioBinding
import com.newsperso.app.ui.StationAdapter

class RadioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRadioBinding
    private lateinit var sources: SourcesRepository
    private lateinit var adapter: StationAdapter

    private var player: MediaPlayer? = null
    private var currentStation: RadioStation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = SettingsPrefs(this)
        AppCompatDelegate.setDefaultNightMode(
            when (prefs.theme) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
        AppCompatDelegate.setApplicationLocales(
            if (prefs.language == "en") LocaleListCompat.forLanguageTags("en")
            else LocaleListCompat.forLanguageTags("fr")
        )
        binding = ActivityRadioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sources = SourcesRepository(this)
        adapter = StationAdapter(sources.radios(), currentStation?.url, ::toggleStation)
        binding.recyclerStations.layoutManager = LinearLayoutManager(this)
        binding.recyclerStations.adapter = adapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnStop.setOnClickListener { stopPlayback() }

        refreshList()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlayback()
    }

    private fun refreshList() {
        val radios = sources.radios()
        binding.emptyRadios.visibility = if (radios.isEmpty()) View.VISIBLE else View.GONE
        adapter.submit(radios, currentStation?.url)
    }

    private fun toggleStation(station: RadioStation) {
        if (station.url == currentStation?.url) {
            stopPlayback()
            return
        }
        play(station)
    }

    private fun play(station: RadioStation) {
        stopPlayback()
        currentStation = station
        binding.nowPlayingCard.visibility = View.VISIBLE
        binding.txtStation.text = station.name
        binding.txtPlayerState.text = getString(R.string.radio_buffering, station.name)

        val mp = MediaPlayer()
        mp.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        mp.setDataSource(station.url)
        mp.setOnPreparedListener {
            it.start()
            binding.txtPlayerState.text = getString(R.string.radio_playing, station.name)
            refreshList()
        }
        mp.setOnErrorListener { _, _, _ ->
            runOnUiThread {
                Toast.makeText(this, R.string.radio_error, Toast.LENGTH_LONG).show()
                stopPlayback()
            }
            true
        }
        player = mp
        Thread {
            try {
                mp.prepare()
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, R.string.radio_error, Toast.LENGTH_LONG).show()
                    stopPlayback()
                }
            }
        }.start()
        refreshList()
    }

    private fun stopPlayback() {
        player?.let {
            try {
                if (it.isPlaying) it.stop()
                it.release()
            } catch (_: Exception) {
            }
        }
        player = null
        currentStation = null
        binding.nowPlayingCard.visibility = View.GONE
        refreshList()
    }
}
