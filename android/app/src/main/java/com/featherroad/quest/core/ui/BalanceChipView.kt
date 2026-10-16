package com.featherroad.quest.core.ui

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.featherroad.quest.R
import com.featherroad.quest.core.ext.formatGrouped
import com.featherroad.quest.databinding.ViewBalanceChipBinding

class BalanceChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewBalanceChipBinding.inflate(LayoutInflater.from(context), this)

    private var pulseAnimator: ValueAnimator? = null
    private var currentValue: Int = 0

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setBackgroundResource(R.drawable.chip_accent_bg)
        val horizontal = resources.getDimensionPixelSize(R.dimen.gap_m)
        val vertical = resources.getDimensionPixelSize(R.dimen.gap_xs)
        updatePadding(left = horizontal, top = vertical, right = horizontal, bottom = vertical)

        val typed = context.obtainStyledAttributes(attrs, R.styleable.BalanceChipView)
        val value = typed.getString(R.styleable.BalanceChipView_balanceValue)
        val icon = typed.getResourceId(R.styleable.BalanceChipView_balanceIcon, R.drawable.ic_cash)
        typed.recycle()

        binding.balanceIcon.setImageResource(icon)
        binding.balanceValue.text = value ?: context.getString(R.string.fmt_cash, "0")
    }

    fun setBalance(value: Int) {
        currentValue = value
        binding.balanceValue.text = context.getString(R.string.fmt_cash, formatGrouped(value))
        ViewCompat.setStateDescription(this, binding.balanceValue.text)
    }

    fun countTo(value: Int) {
        val from = currentValue
        if (from == value) {
            setBalance(value)
            return
        }
        pulseAnimator?.cancel()
        val animator = ValueAnimator.ofInt(from, value)
        animator.duration = COUNT_MS
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            try {
                if (!isAttachedToWindow) return@addUpdateListener
                val step = animation.animatedValue as Int
                binding.balanceValue.text =
                    context.getString(R.string.fmt_cash, formatGrouped(step))
            } catch (e: Exception) {
            }
        }
        pulseAnimator = animator
        currentValue = value
        animator.start()
        pulse()
    }

    fun pulse() {
        animate().cancel()
        animate()
            .scaleX(PULSE_SCALE)
            .scaleY(PULSE_SCALE)
            .setDuration(PULSE_MS)
            .withEndAction {
                try {
                    if (!isAttachedToWindow) return@withEndAction
                    animate().scaleX(1f).scaleY(1f).setDuration(PULSE_MS).start()
                } catch (e: Exception) {
                }
            }
            .start()
    }

    override fun onDetachedFromWindow() {
        pulseAnimator?.cancel()
        pulseAnimator = null
        animate().cancel()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val PULSE_SCALE = 1.12f
        private const val PULSE_MS = 130L
        private const val COUNT_MS = 600L
    }
}
