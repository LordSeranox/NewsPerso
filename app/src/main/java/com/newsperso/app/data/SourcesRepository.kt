package com.newsperso.app.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Feed(val name: String, val url: String)
data class RadioStation(val name: String, val url: String, val genre: String = "")

class SourcesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("sources_prefs", Context.MODE_PRIVATE)

    private val defaultFeeds = listOf(
        Feed("Le Monde", "https://www.lemonde.fr/rss/une.xml"),
        Feed("France Info", "https://www.francetvinfo.fr/titres.rss"),
        Feed("France 24", "https://www.france24.com/fr/rss"),
        Feed("BFM TV", "https://www.bfm.tv/rss/rss-actualites.xml"),
        Feed("Courrier International", "https://www.courrierinternational.com/feed.xml")
    )

    private val defaultRadios = listOf(
        RadioStation("FIP", "https://icecast.radiofrance.fr/fip-midfi.mp3", "Éclectique"),
        RadioStation("France Inter", "https://icecast.radiofrance.fr/franceinter-midfi.mp3", "Généraliste"),
        RadioStation("France Info", "https://icecast.radiofrance.fr/franceinfo-midfi.mp3", "Info continue"),
        RadioStation("France Culture", "https://icecast.radiofrance.fr/franceculture-midfi.mp3", "Culture"),
        RadioStation("Mouv'", "https://icecast.radiofrance.fr/mouv-midfi.mp3", "Rap / R&B"),
        RadioStation("Radio Nova", "https://novazz.ice.infomaniak.ch/novazz-128.mp3", "Éclectique")
    )

    fun feeds(): MutableList<Feed> {
        val raw = prefs.getString(KEY_FEEDS, null) ?: run {
            saveFeeds(defaultFeeds)
            return defaultFeeds.toMutableList()
        }
        val arr = JSONArray(raw)
        return List(arr.length()) {
            val o = arr.getJSONObject(it)
            Feed(o.getString("name"), o.getString("url"))
        }.toMutableList()
    }

    fun saveFeeds(list: List<Feed>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("name", it.name).put("url", it.url)) }
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
            RadioStation(o.getString("name"), o.getString("url"), o.optString("genre"))
        }.toMutableList()
    }

    fun saveRadios(list: List<RadioStation>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(
                JSONObject().put("name", it.name).put("url", it.url).put("genre", it.genre)
            )
        }
        prefs.edit().putString(KEY_RADIOS, arr.toString()).apply()
    }

    companion object {
        private const val KEY_FEEDS = "feeds"
        private const val KEY_RADIOS = "radios"
    }
}
