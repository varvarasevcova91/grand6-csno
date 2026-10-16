package com.featherroad.quest.presentation.bonus

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.featherroad.quest.R
import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.core.di.ViewModelFactory
import com.featherroad.quest.core.ext.applyBottomBarInset
import com.featherroad.quest.core.ext.formatGrouped
import com.featherroad.quest.core.navigation.Navigator
import com.featherroad.quest.core.ui.ReelStripView
import com.featherroad.quest.core.ui.SpriteCatalog
import com.featherroad.quest.databinding.FragmentVipBonusBinding
import com.featherroad.quest.domain.model.BonusSpin
import com.featherroad.quest.presentation.gameover.ResultFragment
import kotlin.random.Random
import kotlinx.coroutines.launch

class VipBonusFragment : Fragment() {

    private var binding: FragmentVipBonusBinding? = null
    private val decorAnimators = mutableListOf<ValueAnimator>()
    private var payoutAnimator: ValueAnimator? = null
    private var settledReels = 0
    private var spinStarted = false

    private val viewModel: VipBonusViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentVipBonusBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.root.applyBottomBarInset()

        bound.bonusPayout.text = getString(R.string.fmt_payout, "0")
        bound.bonusCollect.isEnabled = false
        bound.bonusCollectFrame.alpha = DISABLED_ALPHA
        bound.bonusCollect.setOnClickListener {
            viewModel.collect()
            Navigator.swap(parentFragmentManager, ResultFragment(), Navigator.TAG_RESULT)
        }

        buildParticles(bound.bonusParticles)
        viewModel.prepare()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.spin.collect { spin ->
                    if (spin != null) startSpin(spin)
                }
            }
        }
    }

    private fun startSpin(spin: BonusSpin) {
        if (spinStarted) return
        spinStarted = true
        val bound = binding ?: return
        val cycle = SpriteCatalog.bonusReelOrder()
        val reels = listOf(bound.bonusReel1, bound.bonusReel2, bound.bonusReel3)
        val durations = listOf(
            GameConfig.BONUS_REEL_ONE_MS,
            GameConfig.BONUS_REEL_TWO_MS,
            GameConfig.BONUS_REEL_THREE_MS
        )
        reels.forEachIndexed { index, reel ->
            reel.spin(durations[index], cycle, SpriteCatalog.bonus(spin.symbols[index])) {
                onReelSettled(spin)
            }
        }
    }

    private fun onReelSettled(spin: BonusSpin) {
        settledReels += 1
        if (settledReels < REEL_COUNT) return
        countPayout(spin.payout)
        if (spin.tripleMatch) playShockwave()
        val bound = binding ?: return
        bound.bonusCollect.isEnabled = true
        bound.bonusCollectFrame.alpha = 1f
    }

    private fun countPayout(payout: Int) {
        payoutAnimator?.cancel()
        val animator = ValueAnimator.ofInt(0, payout)
        animator.duration = COUNT_MS
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            try {
                val bound = binding ?: return@addUpdateListener
                val value = animation.animatedValue as Int
                bound.bonusPayout.text = getString(R.string.fmt_payout, formatGrouped(value))
            } catch (e: Exception) {
            }
        }
        payoutAnimator = animator
        animator.start()

        val bound = binding ?: return
        bound.bonusPayout.animate().cancel()
        bound.bonusPayout.scaleX = 0.84f
        bound.bonusPayout.scaleY = 0.84f
        bound.bonusPayout.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(PULSE_MS)
            .start()
    }

    private fun playShockwave() {
        val bound = binding ?: return
        bound.bonusShockwave.animate().cancel()
        bound.bonusShockwave.alpha = 1f
        bound.bonusShockwave.scaleX = 1f
        bound.bonusShockwave.scaleY = 1f
        bound.bonusShockwave.animate()
            .alpha(0f)
            .scaleX(SHOCK_SCALE)
            .scaleY(SHOCK_SCALE)
            .setDuration(SHOCK_MS)
            .start()
    }

    private fun buildParticles(host: FrameLayout) {
        val density = resources.displayMetrics.density
        val size = (density * PARTICLE_DP).toInt()
        for (index in 0 until PARTICLE_COUNT) {
            val star = ImageView(host.context)
            star.setImageResource(R.drawable.sprite_star_gold)
            star.scaleType = ImageView.ScaleType.FIT_CENTER
            star.importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            val params = FrameLayout.LayoutParams(size, size)
            params.leftMargin = (density * (24 + Random.nextInt(300))).toInt()
            params.topMargin = (density * (120 + Random.nextInt(520))).toInt()
            host.addView(star, params)

            val drift = ValueAnimator.ofFloat(PARTICLE_LOW, PARTICLE_HIGH)
            drift.duration = PARTICLE_MS + index * PARTICLE_STEP_MS
            drift.repeatCount = ValueAnimator.INFINITE
            drift.repeatMode = ValueAnimator.REVERSE
            drift.addUpdateListener { animation ->
                try {
                    if (!star.isAttachedToWindow) return@addUpdateListener
                    val value = animation.animatedValue as Float
                    star.alpha = value
                    star.translationY = -(value * density * PARTICLE_SHIFT_DP)
                } catch (e: Exception) {
                }
            }
            drift.start()
            decorAnimators.add(drift)
        }
    }

    override fun onDestroyView() {
        decorAnimators.forEach { animator ->
            try {
                animator.cancel()
            } catch (e: Exception) {
            }
        }
        decorAnimators.clear()
        payoutAnimator?.cancel()
        payoutAnimator = null
        binding?.let { bound ->
            listOf<ReelStripView>(bound.bonusReel1, bound.bonusReel2, bound.bonusReel3)
                .forEach { reel -> reel.cancelSpin() }
            bound.bonusPayout.animate().cancel()
            bound.bonusShockwave.animate().cancel()
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val REEL_COUNT = 3
        private const val COUNT_MS = 900L
        private const val PULSE_MS = 260L
        private const val SHOCK_MS = 520L
        private const val SHOCK_SCALE = 1.6f
        private const val DISABLED_ALPHA = 0.55f
        private const val PARTICLE_COUNT = 12
        private const val PARTICLE_DP = 18f
        private const val PARTICLE_SHIFT_DP = 18f
        private const val PARTICLE_LOW = 0.3f
        private const val PARTICLE_HIGH = 0.9f
        private const val PARTICLE_MS = 2200L
        private const val PARTICLE_STEP_MS = 160L
    }
}
