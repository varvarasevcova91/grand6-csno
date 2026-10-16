package com.featherroad.quest.presentation.garage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.featherroad.quest.R
import com.featherroad.quest.core.ext.formatGrouped
import com.featherroad.quest.databinding.ItemOutfitBinding

class OutfitAdapter(
    private val onSelect: (OutfitRow) -> Unit
) : RecyclerView.Adapter<OutfitAdapter.OutfitViewHolder>() {

    private val items = mutableListOf<OutfitRow>()

    fun submit(rows: List<OutfitRow>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OutfitViewHolder {
        val binding = ItemOutfitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OutfitViewHolder(binding, onSelect)
    }

    override fun onBindViewHolder(holder: OutfitViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class OutfitViewHolder(
        private val binding: ItemOutfitBinding,
        private val onSelect: (OutfitRow) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: OutfitRow) {
            val context = binding.root.context
            binding.outfitName.text = row.outfit.name
            binding.outfitRoot.setBackgroundResource(
                if (row.selected) R.drawable.card_neon_stroke_active else R.drawable.card_neon_stroke
            )
            binding.outfitRoot.alpha = if (row.owned) 1f else LOCKED_ALPHA
            binding.outfitOwnedIcon.visibility = if (row.owned) View.VISIBLE else View.GONE
            binding.outfitPrice.text = if (row.owned) {
                context.getString(R.string.owned)
            } else {
                context.getString(R.string.fmt_price, formatGrouped(row.outfit.price))
            }
            ViewCompat.setStateDescription(
                binding.outfitRoot,
                context.getString(
                    if (row.owned) R.string.state_unlocked else R.string.state_locked
                )
            )
            binding.outfitRoot.setOnClickListener { onSelect(row) }
        }

        companion object {
            private const val LOCKED_ALPHA = 0.55f
        }
    }
}
