package com.newsperso.app.rss

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Article(
    val title: String,
    val link: String,
    val dateMillis: Long,
    val description: String,
    val source: String
) {
    fun readingMinutes(): Int {
        val words = description.split(Regex("\\s+")).size
        return (words / 200).coerceIn(1, 15)
    }
}

object RssParser {

    fun parse(inputStream: InputStream, sourceName: String): List<Article> {
        val articles = mutableListOf<Article>()
        val factory = XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = false
        }
        val parser = factory.newPullParser()
        parser.setInput(inputStream, null)

        var inItem = false
        var title = ""
        var link = ""
        var pubDate = ""
        var description = ""

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name.lowercase()
                    when {
                        name == "item" || name == "entry" -> {
                            inItem = true
                            title = ""; link = ""; pubDate = ""; description = ""
                        }
                        inItem && (name == "title") -> title = parser.nextText().trim()
                        inItem && (name == "link") -> {
                            val rel = parser.getAttributeValue(null, "rel")
                            if (rel == null || rel == "alternate") {
                                val href = parser.getAttributeValue(null, "href")
                                link = (if (href != null) href else parser.nextText().trim())
                            }
                        }
                        inItem && (name == "pubdate" || name == "published" || name == "updated") ->
                            pubDate = parser.nextText().trim()
                        inItem && (name == "description" || name == "summary" || name == "content") -> {
                            val text = parser.nextText().trim()
                            if (description.length < text.length) description = text
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    val name = parser.name.lowercase()
                    if (name == "item" || name == "entry") {
                        inItem = false
                        if (title.isNotBlank() && link.isNotBlank()) {
                            articles.add(
                                Article(
                                    title = stripHtml(title),
                                    link = link,
                                    dateMillis = parseDate(pubDate),
                                    description = stripHtml(description),
                                    source = sourceName
                                )
                            )
                        }
                    }
                }
            }
            event = parser.next()
        }
        return articles
    }

    fun parseDate(raw: String): Long {
        if (raw.isBlank()) return System.currentTimeMillis()
        val formats = listOf(
            "EEE, dd MMM yyyy HH:mm:ss Z",
            "EEE, dd MMM yyyy HH:mm:ss 'GMT'",
            "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (f in formats) {
            try {
                val fmt = SimpleDateFormat(f, Locale.US)
                val d: Date = fmt.parse(raw) ?: continue
                return d.time
            } catch (_: Exception) {
            }
        }
        return try {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).parse(raw)?.time
                ?: System.currentTimeMillis()
        } catch (_: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun stripHtml(s: String): String =
        s.replace(Regex("<[^>]*>"), " ")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&rsquo;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
}
