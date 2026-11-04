package com.newsperso.app.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.newsperso.app.R
import com.newsperso.app.data.RadioStation
import com.newsperso.app.databinding.ItemStationBinding

class StationAdapter(
    private var stations: List<RadioStation>,
    private val onToggle: (RadioStation) -> Unit
) : RecyclerView.Adapter<StationAdapter.VH>() {

    private var playingUrl: String? = null

    fun submit(list: List<RadioStation>, url: String?) {
        stations = list
        playingUrl = url
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemStationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = stations.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(stations[position])

    inner class VH(private val b: ItemStationBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(s: RadioStation) {
            val ctx: Context = b.root.context
            b.txtStationName.text = s.name
            b.txtStationGenre.text = s.genre.ifBlank { s.url }

            val fallback = LogoHelper.letterDrawable(ctx, s.name)
            if (s.logoUrl.isBlank()) {
                b.imgLogo.setImageDrawable(fallback)
            } else {
                b.imgLogo.load(s.logoUrl) {
                    placeholder(fallback)
                    error(fallback)
                }
            }

            val playing = s.url == playingUrl
            b.imgPlay.setImageResource(if (playing) R.drawable.ic_stop else R.drawable.ic_play)
            b.imgPlay.setColorFilter(
                if (playing) ContextCompat.getColor(ctx, R.color.np_error)
                else ContextCompat.getColor(ctx, R.color.np_accent)
            )
            b.root.setOnClickListener { onToggle(s) }
        }
    }
}
