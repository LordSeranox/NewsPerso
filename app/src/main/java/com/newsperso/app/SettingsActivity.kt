package com.newsperso.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.newsperso.app.data.FavoritesRepository
import com.newsperso.app.data.Feed
import com.newsperso.app.data.RadioStation
import com.newsperso.app.data.SettingsPrefs
import com.newsperso.app.data.SourcesRepository
import com.newsperso.app.databinding.ActivitySettingsBinding
import com.newsperso.app.databinding.DialogSourceBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: SettingsPrefs
    private lateinit var sources: SourcesRepository
    private lateinit var favorites: FavoritesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SettingsPrefs(this)
        applyTheme(prefs.theme)
        applyLocale(prefs.language)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sources = SourcesRepository(this)
        favorites = FavoritesRepository(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnManageFeeds.setOnClickListener { showFeedManager() }
        binding.btnManageRadios.setOnClickListener { showRadioManager() }

        when (prefs.language) {
            "fr" -> binding.langFr.isChecked = true
            else -> binding.langEn.isChecked = true
        }
        binding.langGroup.setOnCheckedChangeListener { _, id ->
            val lang = if (id == R.id.langFr) "fr" else "en"
            if (lang != prefs.language) {
                prefs.language = lang
                applyLocale(lang)
                recreate()
            }
        }

        when (prefs.theme) {
            "dark" -> binding.themeDark.isChecked = true
            "light" -> binding.themeLight.isChecked = true
            else -> binding.themeSystem.isChecked = true
        }
        binding.themeGroup.setOnCheckedChangeListener { _, _ ->
            val theme = when {
                binding.themeDark.isChecked -> "dark"
                binding.themeLight.isChecked -> "light"
                else -> "system"
            }
            prefs.theme = theme
            applyTheme(theme)
        }

        binding.btnClearFavorites.setOnClickListener {
            favorites.clear()
            Snackbar.make(binding.root, R.string.favorites_cleared, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun applyTheme(theme: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    private fun applyLocale(lang: String) {
        AppCompatDelegate.setApplicationLocales(
            if (lang == "en") LocaleListCompat.forLanguageTags("en")
            else LocaleListCompat.forLanguageTags("fr")
        )
    }

    private fun showFeedManager() {
        val feeds = sources.feeds()
        val names = feeds.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_feeds)
            .setItems(names) { _, which ->
                confirmDeleteFeed(feeds[which])
            }
            .setPositiveButton(R.string.add_feed) { _, _ -> showAddFeedDialog() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun confirmDeleteFeed(feed: Feed) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_delete)
            .setMessage(feed.name)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                val feeds = sources.feeds()
                feeds.removeAll { it.url == feed.url }
                sources.saveFeeds(feeds)
                Snackbar.make(binding.root, R.string.action_delete, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showAddFeedDialog() {
        val dialogBinding = DialogSourceBinding.inflate(LayoutInflater.from(this))
        dialogBinding.genreLayout.visibility = View.GONE
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_feed)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = dialogBinding.inputName.text?.toString()?.trim().orEmpty()
            val url = dialogBinding.inputUrl.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                dialogBinding.inputName.error = getString(R.string.error_name_required)
                return@setOnClickListener
            }
            if (url.isBlank()) {
                dialogBinding.inputUrl.error = getString(R.string.error_url_required)
                return@setOnClickListener
            }
            val feeds = sources.feeds()
            feeds.add(Feed(name, url))
            sources.saveFeeds(feeds)
            dialog.dismiss()
        }
    }

    private fun showRadioManager() {
        val radios = sources.radios()
        val names = radios.map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_radios)
            .setItems(names) { _, which ->
                confirmDeleteRadio(radios[which])
            }
            .setPositiveButton(R.string.add_radio) { _, _ -> showAddRadioDialog() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun confirmDeleteRadio(station: RadioStation) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.action_delete)
            .setMessage(station.name)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                val radios = sources.radios()
                radios.removeAll { it.url == station.url }
                sources.saveRadios(radios)
                Snackbar.make(binding.root, R.string.action_delete, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showAddRadioDialog() {
        val dialogBinding = DialogSourceBinding.inflate(LayoutInflater.from(this))
        dialogBinding.genreLayout.visibility = View.VISIBLE
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_radio)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.action_save, null)
            .setNegativeButton(R.string.action_cancel, null)
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = dialogBinding.inputName.text?.toString()?.trim().orEmpty()
            val url = dialogBinding.inputUrl.text?.toString()?.trim().orEmpty()
            val genre = dialogBinding.inputGenre.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                dialogBinding.inputName.error = getString(R.string.error_name_required)
                return@setOnClickListener
            }
            if (url.isBlank()) {
                dialogBinding.inputUrl.error = getString(R.string.error_url_required)
                return@setOnClickListener
            }
            val radios = sources.radios()
            radios.add(RadioStation(name, url, genre))
            sources.saveRadios(radios)
            dialog.dismiss()
        }
    }
}
