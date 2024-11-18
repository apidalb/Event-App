package com.example.dicodingevent.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dicodingevent.adapterviewmodel.MainViewModel
import com.example.dicodingevent.adapterviewmodel.MainViewModelFactory
import com.example.dicodingevent.adapterviewmodel.ReviewHorizontalAdapter
import com.example.dicodingevent.adapterviewmodel.ReviewVerticalAdapter
import com.example.dicodingevent.databinding.FragmentHomeBinding
import com.example.dicodingevent.ui.detail.DetailEventActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: MainViewModel by viewModels {
        MainViewModelFactory.getInstance(requireContext()) //for FragmentBinding
    }

    private lateinit var upcomingAdapter: ReviewHorizontalAdapter
    private lateinit var finishedAdapter: ReviewVerticalAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapters intent to detailEvent.kt
        upcomingAdapter = ReviewHorizontalAdapter { eventId ->
            val intent = Intent(context, DetailEventActivity::class.java)
            intent.putExtra("EXTRA_EVENT_ID", eventId)
            context?.startActivity(intent)
        }
        finishedAdapter = ReviewVerticalAdapter { eventId ->
            val intent = Intent(context, DetailEventActivity::class.java)
            intent.putExtra("EXTRA_EVENT_ID", eventId)
            context?.startActivity(intent)
        }

        // Setup RecyclerView
        binding.rvUpcomingEvent.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvUpcomingEvent.adapter = upcomingAdapter
        binding.rvFinishedEvent.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFinishedEvent.adapter = finishedAdapter

        //LiveData
        observeViewModel()

        // Fetch data
        homeViewModel.getActiveEvents()
        homeViewModel.getFinishedEvents()

        binding.btnRefresh.setOnClickListener {
            binding.tvErrorMessage.visibility = View.GONE
            binding.btnRefresh.visibility = View.GONE
            homeViewModel.getActiveEvents()
            homeViewModel.getFinishedEvents()
        }
    }

    override fun onResume() {
        super.onResume()
        resetErrorMessage()
    }

    private fun resetErrorMessage() {
        homeViewModel.clearErrorMessage()
        binding.tvErrorMessage.visibility = View.GONE
        binding.btnRefresh.visibility = View.GONE
    }

    private fun observeViewModel() {
        homeViewModel.activeEvents.observe(viewLifecycleOwner) { events ->
            val upcomingEvents = events.take(5)
            upcomingAdapter.submitList(upcomingEvents)
        }

        homeViewModel.finishedEvents.observe(viewLifecycleOwner) { finishedEvents ->
            finishedEvents?.let {
                val limitedFinishedEvents = it.take(5)
                finishedAdapter.submitList(limitedFinishedEvents)
            }
        }

        homeViewModel.isLoadingActive.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarUpcoming.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        homeViewModel.isLoadingFinished.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarFinished.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        homeViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                binding.tvErrorMessage.text = errorMessage
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.btnRefresh.visibility = View.VISIBLE
            } else {
                binding.tvErrorMessage.visibility = View.GONE
                binding.btnRefresh.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
