package com.example.pantrypal.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pantrypal.PantryPalApplication
import com.example.pantrypal.adapter.PantryAdapter
import com.example.pantrypal.databinding.FragmentHomeBinding
import com.example.pantrypal.ui.detail.ItemDetailActivity
import com.example.pantrypal.util.PreferenceManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            (requireActivity().application as PantryPalApplication).database.pantryDao()
        )
    }

    private lateinit var expiringAdapter: PantryAdapter
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        
        setupGreeting()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupGreeting() {
        val userName = preferenceManager.getUserName()
        binding.tvGreeting.text = "Hello, ${userName ?: "Guest"}!"
    }

    private fun setupRecyclerView() {
        expiringAdapter = PantryAdapter(
            onItemClick = { item ->
                val intent = Intent(requireContext(), ItemDetailActivity::class.java)
                intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, item.id)
                startActivity(intent)
            },
            onDeleteClick = { item ->
                viewModel.deleteItem(item)
            }
        )

        binding.rvExpiringItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expiringAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        // Observe expiring items (within 7 days)
        viewModel.expiringItems.observe(viewLifecycleOwner) { items ->
            expiringAdapter.submitList(items)
            binding.tvNoItems.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvExpiringItems.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }

        // Observe statistics
        viewModel.totalCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalItems.text = count.toString()
        }

        viewModel.expiringTodayCount.observe(viewLifecycleOwner) { count ->
            binding.tvExpiringToday.text = count.toString()
        }

        viewModel.expiringWeekCount.observe(viewLifecycleOwner) { count ->
            binding.tvExpiringWeek.text = count.toString()
        }

        viewModel.expiredCount.observe(viewLifecycleOwner) { count ->
            binding.tvExpiredCount.text = count.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
