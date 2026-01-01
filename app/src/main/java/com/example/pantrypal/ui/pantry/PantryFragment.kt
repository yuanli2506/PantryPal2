package com.example.pantrypal.ui.pantry

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pantrypal.PantryPalApplication
import com.example.pantrypal.adapter.PantryAdapter
import com.example.pantrypal.data.model.Category
import com.example.pantrypal.databinding.FragmentPantryBinding
import com.example.pantrypal.ui.detail.ItemDetailActivity
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PantryFragment : Fragment() {

    private var _binding: FragmentPantryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PantryViewModel by viewModels {
        PantryViewModelFactory(
            (requireActivity().application as PantryPalApplication).database.pantryDao()
        )
    }

    private lateinit var pantryAdapter: PantryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPantryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterChips()
        setupSearchView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        pantryAdapter = PantryAdapter(
            onItemClick = { item ->
                val intent = Intent(requireContext(), ItemDetailActivity::class.java)
                intent.putExtra(ItemDetailActivity.EXTRA_ITEM_ID, item.id)
                startActivity(intent)
            },
            onDeleteClick = { item ->
                showDeleteConfirmation(item.id, item.name)
            }
        )

        binding.rvPantryItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pantryAdapter
            setHasFixedSize(true)
        }

        // Swipe refresh
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupFilterChips() {
        // All items chip
        binding.chipAll.setOnClickListener {
            viewModel.setFilter(PantryFilter.ALL)
            updateChipSelection(binding.chipAll)
        }

        // Fresh items chip
        binding.chipFresh.setOnClickListener {
            viewModel.setFilter(PantryFilter.FRESH)
            updateChipSelection(binding.chipFresh)
        }

        // Expiring soon chip
        binding.chipExpiring.setOnClickListener {
            viewModel.setFilter(PantryFilter.EXPIRING_SOON)
            updateChipSelection(binding.chipExpiring)
        }

        // Expired chip
        binding.chipExpired.setOnClickListener {
            viewModel.setFilter(PantryFilter.EXPIRED)
            updateChipSelection(binding.chipExpired)
        }

        // Category filter chip
        binding.chipCategory.setOnClickListener {
            showCategoryDialog()
        }
    }

    private fun updateChipSelection(selectedChip: Chip) {
        binding.chipAll.isChecked = selectedChip == binding.chipAll
        binding.chipFresh.isChecked = selectedChip == binding.chipFresh
        binding.chipExpiring.isChecked = selectedChip == binding.chipExpiring
        binding.chipExpired.isChecked = selectedChip == binding.chipExpired
    }

    private fun showCategoryDialog() {
        val categories = Category.getCategoryNames().toTypedArray()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Select Category")
            .setItems(categories) { _, which ->
                viewModel.setCategory(categories[which])
                binding.chipCategory.text = categories[which]
            }
            .setNegativeButton("Clear") { _, _ ->
                viewModel.setCategory(null)
                binding.chipCategory.text = "Category"
            }
            .show()
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.setSearchQuery(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText ?: "")
                return true
            }
        })
    }

    private fun showDeleteConfirmation(itemId: Int, itemName: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete \"$itemName\"?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteItem(itemId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.filteredItems.observe(viewLifecycleOwner) { items ->
            pantryAdapter.submitList(items)
            
            // Show/hide empty state
            binding.layoutEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvPantryItems.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
            
            // Update count
            binding.tvItemCount.text = "${items.size} items"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

enum class PantryFilter {
    ALL, FRESH, EXPIRING_SOON, EXPIRED
}
