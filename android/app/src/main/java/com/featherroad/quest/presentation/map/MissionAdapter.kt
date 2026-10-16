package com.featherroad.quest.presentation.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.featherroad.quest.R
import com.featherroad.quest.core.ext.formatGrouped
import com.featherroad.quest.databinding.ItemMissionBinding

class MissionAdapter(
    private val onSelect: (MissionRow) -> Unit
) : RecyclerView.Adapter<MissionAdapter.MissionViewHolder>() {

    private val items = mutableListOf<MissionRow>()

    fun submit(rows: List<MissionRow>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val binding = ItemMissionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MissionViewHolder(binding, onSelect)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class MissionViewHolder(
        private val binding: ItemMissionBinding,
        private val onSelect: (MissionRow) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(row: MissionRow) {
            val context = binding.root.context
            binding.missionName.text = row.mission.name
            binding.missionReward.text = context.getString(
                R.string.fmt_reward_risk,
                formatGrouped(row.mission.reward),
                row.mission.risk
            )
            binding.missionRoot.setBackgroundResource(
                if (row.selected && row.unlocked) {
                    R.drawable.card_neon_stroke_active
                } else {
                    R.drawable.card_neon_stroke
                }
            )
            binding.missionRoot.alpha = if (row.unlocked) 1f else LOCKED_ALPHA
            binding.missionRoot.scaleX = if (row.selected && row.unlocked) SELECTED_SCALE else 1f
            binding.missionRoot.scaleY = if (row.selected && row.unlocked) SELECTED_SCALE else 1f
            binding.missionLocked.visibility = if (row.unlocked) View.GONE else View.VISIBLE

            bindPips(row.mission.difficulty)

            ViewCompat.setStateDescription(
                binding.missionRoot,
                context.getString(
                    if (row.unlocked) R.string.state_unlocked else R.string.state_locked
                )
            )

            binding.missionRoot.isClickable = row.unlocked
            binding.missionRoot.setOnClickListener {
                if (row.unlocked) onSelect(row)
            }
        }

        private fun bindPips(difficulty: Int) {
            val pips = listOf(binding.missionPip1, binding.missionPip2, binding.missionPip3)
            pips.forEachIndexed { index, pip ->
                pip.setBackgroundResource(
                    if (index < difficulty) R.drawable.bg_pip_on else R.drawable.bg_pip_off
                )
            }
        }

        companion object {
            private const val LOCKED_ALPHA = 0.55f
            private const val SELECTED_SCALE = 1.02f
        }
    }
}
