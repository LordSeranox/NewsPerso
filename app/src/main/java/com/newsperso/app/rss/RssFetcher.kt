package com.newsperso.app.rss

import com.newsperso.app.data.Feed
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object RssFetcher {

    fun fetchAll(feeds: List<Feed>, maxPerFeed: Int = 15): List<Article> {
        val all = mutableListOf<Article>()
        for (feed in feeds) {
            try {
                fetch(feed.url, feed.name)?.let { items ->
                    all.addAll(items.take(maxPerFeed))
                }
            } catch (_: Exception) {
            }
        }
        return all.sortedByDescending { it.dateMillis }
    }

    fun fetch(url: String, sourceName: String): List<Article>? {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        conn.instanceFollowRedirects = true
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Android)")
        return try {
            val code = conn.responseCode
            if (code !in 200..299) return null
            conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val content = reader.readText()
                RssParser.parse(content.byteInputStream(), sourceName)
            }
        } finally {
            conn.disconnect()
        }
    }
}
