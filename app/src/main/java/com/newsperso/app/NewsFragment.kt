package com.newsperso.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.newsperso.app.data.FavoritesRepository
import com.newsperso.app.data.SourcesRepository
import com.newsperso.app.databinding.FragmentNewsBinding
import com.newsperso.app.rss.Article
import com.newsperso.app.rss.RssFetcher
import com.newsperso.app.ui.ArticleAdapter

class NewsFragment : Fragment() {

    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sources: SourcesRepository
    private lateinit var favorites: FavoritesRepository
    private lateinit var adapter: ArticleAdapter

    private var articles = mutableListOf<Article>()
    private var loading = false
    private var showFavoritesOnly = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sources = SourcesRepository(requireContext())
        favorites = FavoritesRepository(requireContext())

        adapter = ArticleAdapter(
            articles,
            favorites,
            onOpen = { openArticle(it) },
            onShare = { shareArticle(it) },
            onToggleFavorite = { toggleFavorite(it) }
        )
        binding.recyclerArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerArticles.adapter = adapter
        adapter.setLogoLookup { article ->
            sources.feeds().firstOrNull { it.name == article.source }?.logoUrl ?: ""
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
        refreshDisplay()
    }

    private fun loadArticles() {
        if (loading) return
        loading = true
        binding.swipeRefresh.isRefreshing = true
        val context = requireContext()
        Thread {
            val feeds = sources.feeds()
            val fetched = RssFetcher.fetchAll(feeds)
            requireActivity().runOnUiThread {
                loading = false
                binding.swipeRefresh.isRefreshing = false
                articles = fetched.toMutableList()
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
            _binding?.let { Snackbar.make(it.root, R.string.refresh_failed, Snackbar.LENGTH_SHORT).show() }
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
        _binding?.let {
            Snackbar.make(
                it.root,
                if (added) R.string.favorite_add else R.string.favorite_remove,
                Snackbar.LENGTH_SHORT
            ).show()
        }
        refreshDisplay()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
