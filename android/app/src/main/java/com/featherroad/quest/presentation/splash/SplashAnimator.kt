package com.featherroad.quest.presentation.splash

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import com.featherroad.quest.databinding.FragmentSplashBinding

class SplashAnimator(
    private val binding: FragmentSplashBinding
) {

    private val loops = mutableListOf<Animator>()

    fun prepare() {
        binding.splashEmblem.alpha = 0f
        binding.splashEmblem.scaleX = ENTRY_SCALE
        binding.splashEmblem.scaleY = ENTRY_SCALE
        binding.splashRing.alpha = 0f
        binding.splashTitleTop.alpha = 0f
        binding.splashTitleBottom.alpha = 0f
        binding.splashTagline.alpha = 0f
        binding.splashUnderline.alpha = 0f
        binding.splashProgress.alpha = 0f
        binding.splashLoadingLabel.alpha = 0f

        val shift = binding.root.resources.displayMetrics.density * TITLE_SHIFT_DP
        binding.splashTitleTop.translationY = shift
        binding.splashTitleBottom.translationY = shift
        binding.splashTagline.translationY = shift
    }

    fun playEntry() {
        binding.splashEmblem.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(EMBLEM_MS)
            .setInterpolator(OvershootInterpolator(1.4f))
            .start()

        binding.splashRing.animate()
            .alpha(1f)
            .setStartDelay(RING_DELAY_MS)
            .setDuration(FADE_MS)
            .start()

        fadeUp(binding.splashTitleTop, TITLE_TOP_DELAY_MS)
        fadeUp(binding.splashTitleBottom, TITLE_BOTTOM_DELAY_MS)
        fadeUp(binding.splashTagline, TAGLINE_DELAY_MS)

        binding.splashUnderline.animate()
            .alpha(1f)
            .setStartDelay(TITLE_BOTTOM_DELAY_MS)
            .setDuration(FADE_MS)
            .start()

        binding.splashProgress.animate()
            .alpha(1f)
            .setStartDelay(PROGRESS_DELAY_MS)
            .setDuration(FADE_MS)
            .start()

        binding.splashLoadingLabel.animate()
            .alpha(1f)
            .setStartDelay(PROGRESS_DELAY_MS)
            .setDuration(FADE_MS)
            .start()
    }

    fun startLoops() {
        cancel()

        val breathe = ValueAnimator.ofFloat(1f, BREATHE_SCALE)
        breathe.duration = BREATHE_MS
        breathe.repeatCount = ValueAnimator.INFINITE
        breathe.repeatMode = ValueAnimator.REVERSE
        breathe.interpolator = DecelerateInterpolator()
        breathe.addUpdateListener { animation ->
            try {
                val card = binding.splashEmblem
                if (!card.isAttachedToWindow) return@addUpdateListener
                val value = animation.animatedValue as Float
                card.scaleX = value
                card.scaleY = value
            } catch (e: Exception) {
            }
        }
        breathe.start()
        loops.add(breathe)

        val spin = ObjectAnimator.ofFloat(binding.splashRing, View.ROTATION, 0f, 360f)
        spin.duration = RING_SPIN_MS
        spin.repeatCount = ValueAnimator.INFINITE
        spin.repeatMode = ValueAnimator.RESTART
        spin.interpolator = LinearInterpolator()
        spin.start()
        loops.add(spin)

        val glow = ObjectAnimator.ofFloat(binding.splashHorizonGlow, View.ALPHA, GLOW_LOW, GLOW_HIGH)
        glow.duration = GLOW_MS
        glow.repeatCount = ValueAnimator.INFINITE
        glow.repeatMode = ValueAnimator.REVERSE
        glow.interpolator = DecelerateInterpolator()
        glow.start()
        loops.add(glow)

        val drift = ObjectAnimator.ofFloat(
            binding.splashEmblemSprite,
            View.TRANSLATION_Y,
            0f,
            -binding.root.resources.displayMetrics.density * DRIFT_DP
        )
        drift.duration = DRIFT_MS
        drift.repeatCount = ValueAnimator.INFINITE
        drift.repeatMode = ValueAnimator.REVERSE
        drift.interpolator = DecelerateInterpolator()
        drift.start()
        loops.add(drift)
    }

    fun cancel() {
        loops.forEach { animator ->
            try {
                animator.cancel()
            } catch (e: Exception) {
            }
        }
        loops.clear()
        binding.splashEmblem.animate().cancel()
        binding.splashRing.animate().cancel()
        binding.splashTitleTop.animate().cancel()
        binding.splashTitleBottom.animate().cancel()
        binding.splashTagline.animate().cancel()
        binding.splashUnderline.animate().cancel()
        binding.splashProgress.animate().cancel()
        binding.splashLoadingLabel.animate().cancel()
        binding.splashEmblemSprite.animate().cancel()
    }

    private fun fadeUp(view: View, delay: Long) {
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(delay)
            .setDuration(TITLE_MS)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    companion object {
        private const val ENTRY_SCALE = 0.72f
        private const val BREATHE_SCALE = 1.04f
        private const val TITLE_SHIFT_DP = 28f
        private const val DRIFT_DP = 6f
        private const val GLOW_LOW = 0.45f
        private const val GLOW_HIGH = 1f

        private const val EMBLEM_MS = 620L
        private const val TITLE_MS = 520L
        private const val FADE_MS = 420L
        private const val BREATHE_MS = 1800L
        private const val RING_SPIN_MS = 6000L
        private const val GLOW_MS = 1400L
        private const val DRIFT_MS = 2200L

        private const val RING_DELAY_MS = 320L
        private const val TITLE_TOP_DELAY_MS = 420L
        private const val TITLE_BOTTOM_DELAY_MS = 580L
        private const val TAGLINE_DELAY_MS = 900L
        private const val PROGRESS_DELAY_MS = 1040L
    }
}
