package com.featherroad.quest.presentation.gameover

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.core.content.ContextCompat
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
import com.featherroad.quest.databinding.FragmentResultBinding
import com.featherroad.quest.presentation.game.GameFragment
import kotlinx.coroutines.launch

class ResultFragment : Fragment() {

    private var binding: FragmentResultBinding? = null
    private var totalAnimator: ValueAnimator? = null
    private var glowAnimator: ValueAnimator? = null

    private val viewModel: ResultViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentResultBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.root.applyBottomBarInset()

        bound.resultAgain.setOnClickListener {
            Navigator.swap(parentFragmentManager, GameFragment(), Navigator.TAG_GAME)
        }
        bound.resultMenu.setOnClickListener {
            Navigator.popToRoot(parentFragmentManager)
        }

        startGlow()
        viewModel.load()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: ResultUiState) {
        val bound = binding ?: return
        val context = requireContext()

        val headlineColor = ContextCompat.getColor(
            context,
            if (state.success) R.color.luxury_gold else R.color.sunset_orange
        )
        bound.resultHeadline.setText(
            if (state.success) R.string.result_complete else R.string.result_busted
        )
        bound.resultHeadline.setTextColor(headlineColor)
        bound.resultHeadline.setShadowLayer(18f, 0f, 0f, headlineColor)

        bound.resultCash.text = getString(R.string.fmt_cash, formatGrouped(state.cash))
        bound.resultDiamonds.text = state.diamonds.toString()
        bound.resultMultiplier.text = getString(R.string.fmt_multiplier, state.multiplier)

        bound.resultStatBest.setValue(formatGrouped(state.bestRun))
        bound.resultStatPoints.setValue(formatGrouped(state.vipPoints))
        val showStats = state.bestRun > 0 && state.vipPoints > 0
        bound.resultStatRow.visibility = if (showStats) View.VISIBLE else View.GONE

        animateStars(state.stars)
        animatePanel()
        countTotal(state.total)
    }

    private fun animateStars(stars: Int) {
        val bound = binding ?: return
        val views = listOf(bound.resultStar1, bound.resultStar2, bound.resultStar3)
        views.forEachIndexed { index, star -> bindStar(star, index < stars, index) }
    }

    private fun bindStar(star: ImageView, filled: Boolean, index: Int) {
        star.animate().cancel()
        star.alpha = 0f
        star.scaleX = 0.6f
        star.scaleY = 0.6f
        star.animate()
            .alpha(if (filled) 1f else DIM_ALPHA)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(index * STAR_STAGGER_MS)
            .setDuration(STAR_MS)
            .setInterpolator(OvershootInterpolator(2f))
            .start()
    }

    private fun animatePanel() {
        val bound = binding ?: return
        bound.resultPanel.animate().cancel()
        bound.resultPanel.alpha = 0f
        bound.resultPanel.translationY = resources.displayMetrics.density * PANEL_SHIFT_DP
        bound.resultPanel.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(PANEL_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    private fun countTotal(total: Int) {
        totalAnimator?.cancel()
        val animator = ValueAnimator.ofInt(0, total)
        animator.duration = TOTAL_MS
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            try {
                val bound = binding ?: return@addUpdateListener
                val value = animation.animatedValue as Int
                bound.resultTotal.text = getString(R.string.fmt_cash, formatGrouped(value))
            } catch (e: Exception) {
            }
        }
        totalAnimator = animator
        animator.start()
    }

    private fun startGlow() {
        val bound = binding ?: return
        val animator = ValueAnimator.ofFloat(GLOW_LOW, GLOW_HIGH)
        animator.duration = GLOW_MS
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { animation ->
            try {
                val frame = binding ?: return@addUpdateListener
                frame.resultGlow.alpha = animation.animatedValue as Float
            } catch (e: Exception) {
            }
        }
        glowAnimator = animator
        animator.start()
        bound.resultGlow.alpha = GLOW_LOW
    }

    override fun onDestroyView() {
        totalAnimator?.cancel()
        totalAnimator = null
        glowAnimator?.cancel()
        glowAnimator = null
        binding?.let { bound ->
            bound.resultPanel.animate().cancel()
            bound.resultStar1.animate().cancel()
            bound.resultStar2.animate().cancel()
            bound.resultStar3.animate().cancel()
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val DIM_ALPHA = 0.25f
        private const val STAR_MS = 220L
        private const val STAR_STAGGER_MS = 180L
        private const val PANEL_MS = 420L
        private const val PANEL_SHIFT_DP = 40f
        private const val TOTAL_MS = 1000L
        private const val GLOW_LOW = 0.35f
        private const val GLOW_HIGH = 0.75f
        private const val GLOW_MS = 1600L
    }
}
