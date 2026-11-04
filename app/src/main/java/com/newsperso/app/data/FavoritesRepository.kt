package com.newsperso.app.data

import android.content.Context
import com.newsperso.app.rss.Article
import org.json.JSONArray
import org.json.JSONObject

class FavoritesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)

    fun all(): MutableList<Article> {
        val raw = prefs.getString(KEY, null) ?: return mutableListOf()
        val arr = JSONArray(raw)
        return List(arr.length()) {
            val o = arr.getJSONObject(it)
            Article(
                title = o.getString("title"),
                link = o.getString("link"),
                dateMillis = o.optLong("date"),
                description = o.optString("description"),
                source = o.optString("source")
            )
        }.toMutableList()
    }

    fun isFavorite(article: Article): Boolean =
        all().any { it.link == article.link }

    fun toggle(article: Article): Boolean {
        val list = all()
        val existing = list.indexOfFirst { it.link == article.link }
        return if (existing >= 0) {
            list.removeAt(existing)
            save(list)
            false
        } else {
            list.add(article)
            save(list)
            true
        }
    }

    fun clear() = prefs.edit().remove(KEY).apply()

    private fun save(list: List<Article>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject()
                    .put("title", it.title)
                    .put("link", it.link)
                    .put("date", it.dateMillis)
                    .put("description", it.description)
                    .put("source", it.source)
            )
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    companion object {
        private const val KEY = "favorites"
    }
}
