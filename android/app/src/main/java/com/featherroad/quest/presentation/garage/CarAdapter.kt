package com.featherroad.quest.presentation.garage

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.featherroad.quest.R
import com.featherroad.quest.core.ui.SpriteCatalog
import com.featherroad.quest.databinding.ItemCarBinding
import com.featherroad.quest.domain.usecase.UpgradeCarUseCase

class CarAdapter(
    private val onSelect: (CarRow) -> Unit
) : RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    private val items = mutableListOf<CarRow>()

    fun submit(rows: List<CarRow>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val binding = ItemCarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarViewHolder(binding, onSelect)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class CarViewHolder(
        private val binding: ItemCarBinding,
        private val onSelect: (CarRow) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: CarRow) {
            val context = binding.root.context
            binding.carName.text = row.car.name
            binding.carPreview.setImageResource(SpriteCatalog.car(row.car.spriteKey))
            binding.carRoot.setBackgroundResource(
                if (row.selected) R.drawable.card_neon_stroke_active else R.drawable.card_neon_stroke
            )
            val gain = row.level * LEVEL_GAIN
            binding.carMeterSpeed.setProgress(row.car.speed + gain)
            binding.carMeterGrip.setProgress(row.car.grip + gain)
            binding.carMeterNitro.setProgress(row.car.nitro + gain)
            ViewCompat.setStateDescription(
                binding.carRoot,
                context.getString(
                    if (row.selected) R.string.state_selected else R.string.state_inactive
                )
            )
            binding.carRoot.setOnClickListener { onSelect(row) }
        }

        companion object {
            private const val LEVEL_GAIN = 100 / (UpgradeCarUseCase.MAX_LEVEL * 4)
        }
    }
}
