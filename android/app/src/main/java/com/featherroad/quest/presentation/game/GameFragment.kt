package com.featherroad.quest.presentation.game

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.featherroad.quest.R
import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.core.di.ServiceLocator
import com.featherroad.quest.core.di.ViewModelFactory
import com.featherroad.quest.core.ext.applyBottomBarInset
import com.featherroad.quest.core.ext.formatGrouped
import com.featherroad.quest.core.navigation.Navigator
import com.featherroad.quest.core.ui.SpriteCatalog
import com.featherroad.quest.databinding.FragmentGameBinding
import com.featherroad.quest.presentation.bonus.VipBonusFragment
import com.featherroad.quest.presentation.dialog.DialogActionListener
import com.featherroad.quest.presentation.dialog.PauseDialog
import com.featherroad.quest.presentation.gameover.ResultFragment
import kotlinx.coroutines.launch

class GameFragment : Fragment(), DialogActionListener {

    private var binding: FragmentGameBinding? = null
    private var exitRunnable: Runnable? = null
    private var lastCrashTicket = 0L
    private var exitScheduled = false

    private val viewModel: GameViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.root.applyBottomBarInset()

        val profile = ServiceLocator.profileRepository.profile()
        val car = ServiceLocator.garageRepository.carById(profile.selectedCarId)
        bound.gameRoad.setPlayerSprite(SpriteCatalog.car(car.spriteKey))

        bound.gameRoad.onTap = { viewModel.onTap() }
        bound.gameRoad.onLaneSwipe = { direction -> viewModel.onSwipe(direction) }
        bound.gameBoost.setOnClickListener { viewModel.onBoost() }
        bound.gamePause.setOnClickListener {
            viewModel.setPaused(true)
            PauseDialog().show(childFragmentManager, PauseDialog.TAG)
        }

        viewModel.start()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    override fun onDialogAction(action: String) {
        when (action) {
            PauseDialog.ACTION_RESUME -> viewModel.setPaused(false)
            PauseDialog.ACTION_RESTART -> {
                lastCrashTicket = 0L
                exitScheduled = false
                binding?.gameRoad?.clearEntities()
                viewModel.restart()
            }

            PauseDialog.ACTION_MENU -> Navigator.popToRoot(parentFragmentManager)
            else -> viewModel.setPaused(false)
        }
    }

    private fun render(state: GameUiState) {
        val bound = binding ?: return

        bound.gameMissionName.text = state.missionName
        bound.gameDistance.text = getString(R.string.fmt_distance, formatGrouped(state.distance))
        bound.gameCash.text = getString(R.string.fmt_cash, formatGrouped(state.cash))
        bound.gameDiamonds.text =
            getString(R.string.fmt_diamonds, state.diamonds, state.diamondTarget)
        bound.gameMultiplier.text = getString(R.string.fmt_multiplier, state.multiplier)
        bound.gameSpeedMeter.setProgress(state.speedPercent)
        bound.gameRoad.setBoosting(state.boosting)
        bound.gameRoad.renderFrame(state.entities, state.lane, state.scrollPhase)
        bound.gameHint.visibility = if (state.phase == GamePhase.READY) View.VISIBLE else View.GONE

        renderStrikes(state.strikes)
        renderBoost(state.boostReady, state.boosting)

        if (state.crashTicket != lastCrashTicket) {
            lastCrashTicket = state.crashTicket
            if (state.crashTicket > 0L) playCrash()
        }

        if (state.phase == GamePhase.FINISHED) scheduleExit(state)
    }

    private fun renderStrikes(strikes: Int) {
        val bound = binding ?: return
        val pips = listOf(bound.gameStrike1, bound.gameStrike2, bound.gameStrike3)
        val hot = ContextCompat.getColor(requireContext(), R.color.siren_red)
        pips.forEachIndexed { index, pip ->
            if (index < strikes) {
                pip.backgroundTintList = ColorStateList.valueOf(hot)
                pip.alpha = 1f
            } else {
                pip.backgroundTintList = null
                pip.alpha = 0.6f
            }
        }
    }

    private fun renderBoost(ready: Boolean, boosting: Boolean) {
        val bound = binding ?: return
        bound.gameBoost.isEnabled = ready && !boosting
        bound.gameBoostFrame.alpha = if (ready && !boosting) 1f else DISABLED_ALPHA
        ViewCompat.setStateDescription(
            bound.gameBoost,
            getString(if (ready) R.string.state_boost_ready else R.string.state_boost_cooling)
        )
    }

    private fun playCrash() {
        val bound = binding ?: return
        bound.gameCrashFlash.animate().cancel()
        bound.gameCrashFlash.alpha = 1f
        bound.gameCrashFlash.animate()
            .alpha(0f)
            .setDuration(CRASH_FLASH_MS)
            .start()

        val shake = resources.displayMetrics.density * SHAKE_DP
        bound.gameRoad.animate().cancel()
        bound.gameRoad.translationX = 0f
        bound.gameRoad.animate()
            .translationX(shake)
            .setDuration(SHAKE_MS)
            .withEndAction {
                try {
                    if (!isAdded || binding == null) return@withEndAction
                    binding?.gameRoad?.animate()
                        ?.translationX(-shake)
                        ?.setDuration(SHAKE_MS)
                        ?.withEndAction {
                            try {
                                if (!isAdded || binding == null) return@withEndAction
                                binding?.gameRoad?.animate()
                                    ?.translationX(0f)
                                    ?.setDuration(SHAKE_MS)
                                    ?.start()
                            } catch (e: Exception) {
                            }
                        }
                        ?.start()
                } catch (e: Exception) {
                }
            }
            .start()
    }

    private fun scheduleExit(state: GameUiState) {
        if (exitScheduled) return
        val bound = binding ?: return
        exitScheduled = true
        val toBonus = state.diamonds >= state.diamondTarget
        val runnable = Runnable {
            try {
                if (!isAdded || binding == null) return@Runnable
                if (toBonus) {
                    Navigator.swap(
                        parentFragmentManager,
                        VipBonusFragment(),
                        Navigator.TAG_BONUS
                    )
                } else {
                    Navigator.swap(
                        parentFragmentManager,
                        ResultFragment(),
                        Navigator.TAG_RESULT
                    )
                }
            } catch (e: Exception) {
            }
        }
        exitRunnable = runnable
        bound.root.postDelayed(runnable, GameConfig.CRASH_FREEZE_MS + GameConfig.FINISH_WIPE_MS)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setPaused(true)
    }

    override fun onResume() {
        super.onResume()
        if (childFragmentManager.findFragmentByTag(PauseDialog.TAG) == null) {
            viewModel.setPaused(false)
        }
    }

    override fun onDestroyView() {
        val pending = exitRunnable
        if (pending != null) {
            binding?.root?.removeCallbacks(pending)
            exitRunnable = null
        }
        binding?.let { bound ->
            bound.gameCrashFlash.animate().cancel()
            bound.gameRoad.animate().cancel()
            bound.gameRoad.onTap = null
            bound.gameRoad.onLaneSwipe = null
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val CRASH_FLASH_MS = 180L
        private const val SHAKE_MS = 60L
        private const val SHAKE_DP = 10f
        private const val DISABLED_ALPHA = 0.55f
    }
}
