package com.example.pantrypal.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.pantrypal.data.model.ExpiryStatus
import com.example.pantrypal.data.model.PantryItem
import com.example.pantrypal.databinding.ItemPantryBinding

class PantryAdapter(
    private val onItemClick: (PantryItem) -> Unit,
    private val onDeleteClick: (PantryItem) -> Unit
) : ListAdapter<PantryItem, PantryAdapter.PantryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PantryViewHolder {
        val binding = ItemPantryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PantryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PantryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PantryViewHolder(
        private val binding: ItemPantryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(item: PantryItem) {
            binding.apply {
                // Item name and emoji
                tvItemName.text = item.name
                tvEmoji.text = item.getCategoryEmoji()

                // Category and quantity
                tvCategoryQuantity.text = "${item.category} • ${item.getQuantityWithUnit()}"

                // Days until expiry
                val daysLeft = item.getDaysUntilExpiry()
                tvDaysLeft.text = when {
                    daysLeft < 0 -> "Expired"
                    daysLeft == 0 -> "Today"
                    daysLeft == 1 -> "1 day"
                    else -> "$daysLeft days"
                }

                // Status color
                val statusColor = when (item.getExpiryStatus()) {
                    ExpiryStatus.EXPIRED -> "#F44336"      // Red
                    ExpiryStatus.EXPIRES_TODAY -> "#FF5722" // Deep Orange
                    ExpiryStatus.CRITICAL -> "#FF9800"      // Orange
                    ExpiryStatus.WARNING -> "#FFC107"       // Amber
                    ExpiryStatus.FRESH -> "#4CAF50"         // Green
                }

                statusIndicator.setBackgroundColor(Color.parseColor(statusColor))
                tvDaysLeft.setTextColor(Color.parseColor(statusColor))

                // Expiry date
                tvExpiryDate.text = "Exp: ${item.getFormattedExpiryDate()}"
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PantryItem>() {
        override fun areItemsTheSame(oldItem: PantryItem, newItem: PantryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PantryItem, newItem: PantryItem): Boolean {
            return oldItem == newItem
        }
    }
}
