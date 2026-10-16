package com.featherroad.quest.core.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import com.featherroad.quest.R
import com.featherroad.quest.databinding.ViewReelStripBinding

class ReelStripView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewReelStripBinding.inflate(LayoutInflater.from(context), this)

    private var spinAnimator: ValueAnimator? = null

    init {
        setBackgroundResource(R.drawable.bg_reel_strip)
        binding.reelSymbol.setImageResource(R.drawable.sprite_seven_gold)
    }

    fun setSymbol(resId: Int) {
        binding.reelSymbol.setImageResource(resId)
    }

    fun spin(durationMs: Long, cycle: List<Int>, finalRes: Int, onSettled: () -> Unit) {
        if (cycle.isEmpty()) {
            setSymbol(finalRes)
            onSettled()
            return
        }
        spinAnimator?.cancel()
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = durationMs
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation ->
            try {
                if (!isAttachedToWindow) return@addUpdateListener
                val progress = animation.animatedValue as Float
                val index = ((progress * cycle.size * SPIN_CYCLES).toInt()) % cycle.size
                binding.reelSymbol.setImageResource(cycle[index])
                binding.reelSymbol.translationY = (1f - progress) * SHAKE_DP * resources.displayMetrics.density
            } catch (e: Exception) {
            }
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                try {
                    if (!isAttachedToWindow) return
                    binding.reelSymbol.translationY = 0f
                    binding.reelSymbol.setImageResource(finalRes)
                    bounce()
                    flash()
                    onSettled()
                } catch (e: Exception) {
                }
            }
        })
        spinAnimator = animator
        animator.start()
    }

    private fun bounce() {
        binding.reelSymbol.animate().cancel()
        binding.reelSymbol.scaleX = 0.76f
        binding.reelSymbol.scaleY = 0.76f
        binding.reelSymbol.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(BOUNCE_MS)
            .setInterpolator(OvershootInterpolator(2.2f))
            .start()
    }

    private fun flash() {
        binding.reelFlash.animate().cancel()
        binding.reelFlash.alpha = 0.9f
        binding.reelFlash.animate()
            .alpha(0f)
            .setDuration(FLASH_MS)
            .start()
    }

    fun cancelSpin() {
        spinAnimator?.cancel()
        spinAnimator = null
        binding.reelSymbol.animate().cancel()
        binding.reelFlash.animate().cancel()
    }

    override fun onDetachedFromWindow() {
        cancelSpin()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val SPIN_CYCLES = 6
        private const val BOUNCE_MS = 220L
        private const val FLASH_MS = 320L
        private const val SHAKE_DP = 10f
    }
}
