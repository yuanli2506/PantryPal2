package com.example.pantrypal.ui.shopping

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pantrypal.PantryPalApplication
import com.example.pantrypal.adapter.ShoppingAdapter
import com.example.pantrypal.data.model.ShoppingItem
import com.example.pantrypal.databinding.FragmentShoppingBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class ShoppingFragment : Fragment() {

    private var _binding: FragmentShoppingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ShoppingViewModel by viewModels {
        ShoppingViewModelFactory(
            (requireActivity().application as PantryPalApplication).database.shoppingDao()
        )
    }

    private lateinit var shoppingAdapter: ShoppingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        shoppingAdapter = ShoppingAdapter(
            onItemClick = { item ->
                viewModel.toggleChecked(item.id)
            },
            onDeleteClick = { item ->
                viewModel.deleteItem(item)
            }
        )

        binding.rvShoppingItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = shoppingAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        // Add item FAB
        binding.fabAddShoppingItem.setOnClickListener {
            showAddItemDialog()
        }

        // Clear checked items
        binding.btnClearChecked.setOnClickListener {
            showClearCheckedConfirmation()
        }
    }

    private fun showAddItemDialog() {
        val dialogView = layoutInflater.inflate(
            com.example.pantrypal.R.layout.dialog_add_shopping_item,
            null
        )
        val etItemName = dialogView.findViewById<TextInputEditText>(com.example.pantrypal.R.id.etItemName)
        val etQuantity = dialogView.findViewById<TextInputEditText>(com.example.pantrypal.R.id.etQuantity)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Shopping Item")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etItemName.text.toString().trim()
                val quantity = etQuantity.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    viewModel.addItem(
                        ShoppingItem(
                            name = name,
                            quantity = quantity
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showClearCheckedConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clear Checked Items")
            .setMessage("Remove all checked items from the shopping list?")
            .setPositiveButton("Clear") { _, _ ->
                viewModel.deleteAllChecked()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            shoppingAdapter.submitList(items)
            
            // Show/hide empty state
            binding.layoutEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
            binding.rvShoppingItems.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.uncheckedCount.observe(viewLifecycleOwner) { count ->
            binding.tvItemCount.text = "$count items remaining"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
