package com.newsperso.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.newsperso.app.data.RadioStation
import com.newsperso.app.data.SourcesRepository
import com.newsperso.app.databinding.FragmentRadioBinding
import com.newsperso.app.ui.StationAdapter

class RadioFragment : Fragment() {

    private var _binding: FragmentRadioBinding? = null
    private val binding get() = _binding!!
    private lateinit var sources: SourcesRepository
    private lateinit var adapter: StationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRadioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sources = SourcesRepository(requireContext())
        adapter = StationAdapter(sources.radios(), ::toggleStation)
        binding.recyclerStations.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerStations.adapter = adapter

        refreshList()
    }

    override fun onResume() {
        super.onResume()
        refreshList()
    }

    private fun refreshList() {
        val radios = sources.radios()
        _binding?.let { b ->
            b.emptyRadios.visibility = if (radios.isEmpty()) View.VISIBLE else View.GONE
            adapter.submit(radios, RadioService.stationUrl)
        }
    }

    private fun toggleStation(station: RadioStation) {
        val context = requireContext()
        if (station.url == RadioService.stationUrl) {
            RadioService.stop(context)
        } else {
            RadioService.start(context, station.name, station.url)
        }
        refreshList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
