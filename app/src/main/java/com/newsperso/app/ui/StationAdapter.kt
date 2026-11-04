package com.newsperso.app.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.newsperso.app.R
import com.newsperso.app.data.RadioStation
import com.newsperso.app.databinding.ItemStationBinding

class StationAdapter(
    private var stations: List<RadioStation>,
    private val currentUrl: String?,
    private val onToggle: (RadioStation) -> Unit
) : RecyclerView.Adapter<StationAdapter.VH>() {

    fun submit(list: List<RadioStation>, playingUrl: String?) {
        stations = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemStationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = stations.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(stations[position])

    inner class VH(private val b: ItemStationBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: RadioStation) {
            b.txtStationName.text = s.name
            b.txtStationGenre.text = s.genre.ifBlank { s.url }
            val playing = s.url == currentUrl
            b.imgPlay.setImageResource(if (playing) R.drawable.ic_stop else R.drawable.ic_play)
            b.imgPlay.setColorFilter(
                if (playing) ContextCompat.getColor(b.root.context, R.color.np_error)
                else ContextCompat.getColor(b.root.context, R.color.np_accent)
            )
            b.root.setOnClickListener { onToggle(s) }
        }
    }
}
