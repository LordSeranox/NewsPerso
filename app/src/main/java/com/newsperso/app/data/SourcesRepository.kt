package com.newsperso.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Feed(val name: String, val url: String, val logoUrl: String = "")
data class RadioStation(val name: String, val url: String, val genre: String = "", val logoUrl: String = "")

class SourcesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("sources_prefs", Context.MODE_PRIVATE)

    private val defaultFeeds = listOf(
        Feed("Le Monde", "https://www.lemonde.fr/rss/une.xml", "https://www.lemonde.fr/favicon.ico"),
        Feed("France Info", "https://www.francetvinfo.fr/titres.rss", "https://www.francetvinfo.fr/favicon.ico"),
        Feed("France 24", "https://www.france24.com/fr/rss", "https://www.france24.com/favicon.ico"),
        Feed("BFM TV", "https://www.bfm.tv/rss/rss-actualites.xml", "https://www.bfm.tv/favicon.ico"),
        Feed("Courrier International", "https://www.courrierinternational.com/feed.xml", "https://www.courrierinternational.com/favicon.ico")
    )

    private val defaultRadios = listOf(
        RadioStation("FIP", "https://icecast.radiofrance.fr/fip-midfi.mp3", "Éclectique", "https://www.fip.fr/favicon.ico"),
        RadioStation("France Inter", "https://icecast.radiofrance.fr/franceinter-midfi.mp3", "Généraliste", "https://www.franceinter.fr/favicon.ico"),
        RadioStation("France Info", "https://icecast.radiofrance.fr/franceinfo-midfi.mp3", "Info continue", "https://www.franceinfo.fr/favicon.ico"),
        RadioStation("France Culture", "https://icecast.radiofrance.fr/franceculture-midfi.mp3", "Culture", "https://www.franceculture.fr/favicon.ico"),
        RadioStation("Mouv'", "https://icecast.radiofrance.fr/mouv-midfi.mp3", "Rap / R&B", "https://www.mouv.fr/favicon.ico"),
        RadioStation("Radio Nova", "https://novazz.ice.infomaniak.ch/novazz-128.mp3", "Éclectique", "https://www.nova.fr/favicon.ico")
    )

    fun feeds(): MutableList<Feed> {
        val raw = prefs.getString(KEY_FEEDS, null) ?: run {
            saveFeeds(defaultFeeds)
            return defaultFeeds.toMutableList()
        }
        val arr = JSONArray(raw)
        return List(arr.length()) {
            val o = arr.getJSONObject(it)
            Feed(o.getString("name"), o.getString("url"), o.optString("logo"))
        }.toMutableList()
    }

    fun saveFeeds(list: List<Feed>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("name", it.name).put("url", it.url).put("logo", it.logoUrl))
        }
        prefs.edit().putString(KEY_FEEDS, arr.toString()).apply()
    }

    fun radios(): MutableList<RadioStation> {
        val raw = prefs.getString(KEY_RADIOS, null) ?: run {
            saveRadios(defaultRadios)
            return defaultRadios.toMutableList()
        }
        val arr = JSONArray(raw)
        return List(arr.length()) {
            val o = arr.getJSONObject(it)
            RadioStation(o.getString("name"), o.getString("url"), o.optString("genre"), o.optString("logo"))
        }.toMutableList()
    }

    fun saveRadios(list: List<RadioStation>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject().put("name", it.name).put("url", it.url)
                    .put("genre", it.genre).put("logo", it.logoUrl)
            )
        }
        prefs.edit().putString(KEY_RADIOS, arr.toString()).apply()
    }

    companion object {
        private const val KEY_FEEDS = "feeds"
        private const val KEY_RADIOS = "radios"
    }
}
