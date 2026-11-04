package com.newsperso.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.newsperso.app.data.FavoritesRepository
import com.newsperso.app.data.SettingsPrefs
import com.newsperso.app.data.SourcesRepository
import com.newsperso.app.databinding.ActivityMainBinding
import com.newsperso.app.rss.Article
import com.newsperso.app.rss.RssFetcher
import com.newsperso.app.ui.ArticleAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sources: SourcesRepository
    private lateinit var favorites: FavoritesRepository
    private lateinit var prefs: SettingsPrefs
    private lateinit var adapter: ArticleAdapter

    private var articles = mutableListOf<Article>()
    private var loading = false
    private var showFavoritesOnly = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = SettingsPrefs(this)
        applyLocale(prefs.language)
        AppCompatDelegate.setDefaultNightMode(
            when (prefs.theme) {
                "dark" -> AppCompatDelegate.MODE_NIGHT_YES
                "light" -> AppCompatDelegate.MODE_NIGHT_NO
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sources = SourcesRepository(this)
        favorites = FavoritesRepository(this)

        adapter = ArticleAdapter(
            articles,
            favorites,
            onOpen = { openArticle(it) },
            onShare = { shareArticle(it) },
            onToggleFavorite = { toggleFavorite(it) }
        )
        binding.recyclerArticles.layoutManager = LinearLayoutManager(this)
        binding.recyclerArticles.adapter = adapter

        binding.btnRadio.setOnClickListener {
            startActivity(Intent(this, RadioActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.chipAll.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showFavoritesOnly = false
                refreshDisplay()
            }
        }
        binding.chipFavorites.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showFavoritesOnly = true
                refreshDisplay()
            }
        }

        binding.searchInput.doAfterTextChanged { refreshDisplay() }

        binding.swipeRefresh.setOnRefreshListener { loadArticles() }

        refreshDisplay()
        loadArticles()
    }

    override fun onResume() {
        super.onResume()
        if (articles.isEmpty()) loadArticles()
    }

    private fun loadArticles() {
        if (loading) return
        loading = true
        binding.swipeRefresh.isRefreshing = true
        Thread {
            val fetched = RssFetcher.fetchAll(sources.feeds())
            runOnUiThread {
                loading = false
                binding.swipeRefresh.isRefreshing = false
                articles = fetched.toMutableList()
                if (articles.isEmpty()) {
                    binding.emptyView.setText(R.string.refresh_failed)
                }
                refreshDisplay()
            }
        }.start()
    }

    private fun refreshDisplay() {
        val list = if (showFavoritesOnly) favorites.all() else articles
        adapter.submit(list, binding.searchInput.text?.toString().orEmpty())
        binding.emptyView.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        binding.emptyView.setText(
            if (showFavoritesOnly) R.string.empty_favorites else R.string.empty_news
        )
    }

    private fun openArticle(a: Article) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(a.link)))
        } catch (_: Exception) {
            Snackbar.make(binding.root, R.string.refresh_failed, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun shareArticle(a: Article) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, a.title)
            putExtra(Intent.EXTRA_TEXT, "${a.title}\n${a.link}")
        }
        startActivity(Intent.createChooser(send, getString(R.string.share_via)))
    }

    private fun toggleFavorite(a: Article) {
        val added = favorites.toggle(a)
        Snackbar.make(
            binding.root,
            if (added) R.string.favorite_add else R.string.favorite_remove,
            Snackbar.LENGTH_SHORT
        ).show()
        refreshDisplay()
    }

    private fun applyLocale(lang: String) {
        AppCompatDelegate.setApplicationLocales(
            if (lang == "en") LocaleListCompat.forLanguageTags("en")
            else LocaleListCompat.forLanguageTags("fr")
        )
    }
}
