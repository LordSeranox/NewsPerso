package com.newsperso.app.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.newsperso.app.R
import com.newsperso.app.data.FavoritesRepository
import com.newsperso.app.rss.Article
import com.newsperso.app.databinding.ItemArticleBinding

class ArticleAdapter(
    private var articles: List<Article>,
    private val favorites: FavoritesRepository,
    private val onOpen: (Article) -> Unit,
    private val onShare: (Article) -> Unit,
    private val onToggleFavorite: (Article) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.VH>() {

    fun submit(list: List<Article>, query: String = "") {
        val q = query.trim().lowercase()
        articles = list.filter {
            q.isBlank() || it.title.lowercase().contains(q) ||
                it.source.lowercase().contains(q)
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = articles.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(articles[position])

    inner class VH(private val b: ItemArticleBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(a: Article) {
            val ctx: Context = b.root.context
            b.txtTitle.text = a.title
            b.txtMeta.text = buildMetaText(ctx, a)
            val fav = favorites.isFavorite(a)
            b.btnFavorite.setImageResource(if (fav) R.drawable.ic_star else R.drawable.ic_star_border)
            b.btnFavorite.setColorFilter(
                if (fav) ContextCompat.getColor(ctx, R.color.np_accent)
                else ContextCompat.getColor(ctx, R.color.np_text_secondary)
            )
            b.btnOpen.setOnClickListener { onOpen(a) }
            b.btnShare.setOnClickListener { onShare(a) }
            b.btnFavorite.setOnClickListener { onToggleFavorite(a) }
            b.root.setOnClickListener { onOpen(a) }
        }

        private fun buildMetaText(ctx: Context, a: Article): String {
            val date = relativeDate(ctx, a.dateMillis)
            val minutes = ctx.getString(R.string.article_reading_time, a.readingMinutes())
            return "${a.source} · $date · $minutes"
        }

        private fun relativeDate(ctx: Context, millis: Long): String {
            val diff = (System.currentTimeMillis() - millis) / 1000L
            return when {
                diff < 60 -> ctx.getString(R.string.just_now)
                diff < 3600 -> ctx.getString(R.string.minutes_ago, (diff / 60).toInt())
                diff < 86_400 -> ctx.getString(R.string.hours_ago, (diff / 3600).toInt())
                else -> ctx.getString(R.string.days_ago, (diff / 86_400).toInt())
            }
        }
    }
}
