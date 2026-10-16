package com.featherroad.quest.presentation.menu

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.featherroad.quest.R
import com.featherroad.quest.core.di.ViewModelFactory
import com.featherroad.quest.core.ext.applyBottomBarInset
import com.featherroad.quest.core.ext.formatGrouped
import com.featherroad.quest.core.navigation.Navigator
import com.featherroad.quest.databinding.FragmentMenuBinding
import com.featherroad.quest.presentation.dialog.DialogActionListener
import com.featherroad.quest.presentation.dialog.SettingsDialog
import com.featherroad.quest.presentation.game.GameFragment
import com.featherroad.quest.presentation.garage.GarageFragment
import com.featherroad.quest.presentation.map.CityMapFragment
import kotlinx.coroutines.launch

class MenuFragment : Fragment(), DialogActionListener {

    private var binding: FragmentMenuBinding? = null
    private val loops = mutableListOf<ValueAnimator>()

    private val viewModel: MenuViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentMenuBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.root.applyBottomBarInset()

        bound.menuPlay.setOnClickListener {
            Navigator.push(parentFragmentManager, GameFragment(), Navigator.TAG_GAME)
        }
        bound.menuCityMap.setOnClickListener {
            Navigator.push(parentFragmentManager, CityMapFragment(), Navigator.TAG_MAP)
        }
        bound.menuGarage.setOnClickListener {
            Navigator.push(parentFragmentManager, GarageFragment(), Navigator.TAG_GARAGE)
        }
        bound.menuSettings.setOnClickListener {
            SettingsDialog().show(childFragmentManager, SettingsDialog.TAG)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }

        playEntry(bound)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDialogAction(action: String) {
        viewModel.refresh()
    }

    private fun render(state: MenuUiState) {
        val bound = binding ?: return
        bound.menuBalance.setBalance(state.balance)
        bound.menuRank.text = getString(R.string.fmt_rank, state.vipRank)
        bound.menuStatBest.setValue(formatGrouped(state.bestRun))
        bound.menuStatMissions.setValue(formatGrouped(state.missionsDone))
        val showStats = state.bestRun > 0 && state.missionsDone > 0
        bound.menuStatRow.visibility = if (showStats) View.VISIBLE else View.GONE
    }

    private fun playEntry(bound: FragmentMenuBinding) {
        val density = resources.displayMetrics.density
        bound.menuSheet.translationY = density * SHEET_SHIFT_DP
        bound.menuSheet.alpha = 0f
        bound.menuSheet.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(SHEET_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()

        staggerIn(bound.menuStatBest, 0L)
        staggerIn(bound.menuStatMissions, STAGGER_MS)

        val pulse = ValueAnimator.ofFloat(1f, CTA_PULSE)
        pulse.duration = CTA_PULSE_MS
        pulse.repeatCount = ValueAnimator.INFINITE
        pulse.repeatMode = ValueAnimator.REVERSE
        pulse.addUpdateListener { animation ->
            try {
                val frame = binding ?: return@addUpdateListener
                val value = animation.animatedValue as Float
                frame.menuPlayFrame.scaleX = value
                frame.menuPlayFrame.scaleY = value
            } catch (e: Exception) {
            }
        }
        pulse.start()
        loops.add(pulse)

        val float = ObjectAnimator.ofFloat(
            bound.menuHeroCar,
            View.TRANSLATION_Y,
            0f,
            -density * CAR_FLOAT_DP
        )
        float.duration = CAR_FLOAT_MS
        float.repeatCount = ValueAnimator.INFINITE
        float.repeatMode = ValueAnimator.REVERSE
        float.interpolator = DecelerateInterpolator()
        float.start()
        loops.add(float)

        val palm = ObjectAnimator.ofFloat(bound.menuPalm, View.ALPHA, PALM_LOW, PALM_HIGH)
        palm.duration = PALM_MS
        palm.repeatCount = ValueAnimator.INFINITE
        palm.repeatMode = ValueAnimator.REVERSE
        palm.start()
        loops.add(palm)
    }

    private fun staggerIn(view: View, delay: Long) {
        view.alpha = 0f
        view.translationY = resources.displayMetrics.density * STAT_SHIFT_DP
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(SHEET_MS + delay)
            .setDuration(STAT_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    override fun onDestroyView() {
        loops.forEach { animator ->
            try {
                animator.cancel()
            } catch (e: Exception) {
            }
        }
        loops.clear()
        binding?.let { bound ->
            bound.menuSheet.animate().cancel()
            bound.menuStatBest.animate().cancel()
            bound.menuStatMissions.animate().cancel()
            bound.menuHeroCar.animate().cancel()
            bound.menuPalm.animate().cancel()
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val SHEET_SHIFT_DP = 64f
        private const val STAT_SHIFT_DP = 16f
        private const val CAR_FLOAT_DP = 6f
        private const val CTA_PULSE = 1.03f
        private const val PALM_LOW = 0.6f
        private const val PALM_HIGH = 0.9f

        private const val SHEET_MS = 480L
        private const val STAT_MS = 320L
        private const val STAGGER_MS = 90L
        private const val CTA_PULSE_MS = 2200L
        private const val CAR_FLOAT_MS = 2600L
        private const val PALM_MS = 3200L
    }
}
