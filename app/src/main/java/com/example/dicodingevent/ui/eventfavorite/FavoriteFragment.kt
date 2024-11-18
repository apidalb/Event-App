package com.example.dicodingevent.ui.eventfavorite

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dicodingevent.R
import com.example.dicodingevent.adapterviewmodel.FavoriteEventAdapter
import com.example.dicodingevent.adapterviewmodel.MainViewModel
import com.example.dicodingevent.adapterviewmodel.MainViewModelFactory
import com.example.dicodingevent.ui.detail.DetailEventActivity

class FavoriteFragment : Fragment() {

    private val favoriteViewModel: MainViewModel by viewModels {
        MainViewModelFactory.getInstance(requireContext())
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FavoriteEventAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoEventsMessage: TextView
    private lateinit var tvErrorMessage: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_favorite, container, false)

        // Initialize UI components
        recyclerView = view.findViewById(R.id.rvUpcomingEvents)
        progressBar = view.findViewById(R.id.progressBar)
        tvNoEventsMessage = view.findViewById(R.id.tvNoEventsMessage)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FavoriteEventAdapter { eventId ->
            val intent = Intent(context, DetailEventActivity::class.java)
            intent.putExtra("EXTRA_EVENT_ID", eventId)
            context?.startActivity(intent)
        }

        recyclerView.adapter = adapter
        favoriteViewModel.getFavoriteEvents()
        observeViewModel()

        return view
    }

    override fun onResume() {
        super.onResume()
        if (isVisible) {
            favoriteViewModel.getFavoriteEvents()
        }
    }

    private fun observeViewModel() {
        favoriteViewModel.favoriteEvents.observe(viewLifecycleOwner) { favoriteEvents ->
            progressBar.visibility = View.GONE
            if (favoriteEvents.isEmpty()) {
                tvNoEventsMessage.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                adapter.submitList(favoriteEvents)
                recyclerView.visibility = View.VISIBLE
                tvNoEventsMessage.visibility = View.GONE
            }
        }

        favoriteViewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                tvErrorMessage.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.GONE
                tvNoEventsMessage.visibility = View.GONE
            } else {
                tvErrorMessage.visibility = View.GONE
            }
        }

        favoriteViewModel.isLoadingFavorite.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
                tvNoEventsMessage.visibility = View.GONE
                tvErrorMessage.visibility = View.GONE
                recyclerView.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
            }
        }
    }
}