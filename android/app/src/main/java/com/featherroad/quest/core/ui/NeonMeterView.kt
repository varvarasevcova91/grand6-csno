package com.featherroad.quest.core.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.featherroad.quest.R
import com.featherroad.quest.databinding.ViewNeonMeterBinding

class NeonMeterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewNeonMeterBinding.inflate(LayoutInflater.from(context), this)

    private val segments = mutableListOf<View>()

    private var accentColor: Int = ContextCompat.getColor(context, R.color.cyber_cyan)
    private var progressValue: Int = 0

    init {
        orientation = VERTICAL

        val typed = context.obtainStyledAttributes(attrs, R.styleable.NeonMeterView)
        val label = typed.getString(R.styleable.NeonMeterView_meterLabel).orEmpty()
        accentColor = typed.getColor(R.styleable.NeonMeterView_meterAccent, accentColor)
        progressValue = typed.getInt(R.styleable.NeonMeterView_meterProgress, 0)
        typed.recycle()

        binding.meterLabel.text = label
        buildSegments()
        applyProgress()
    }

    fun setLabel(label: String) {
        binding.meterLabel.text = label
    }

    fun setAccent(color: Int) {
        accentColor = color
        applyProgress()
    }

    fun setProgress(value: Int) {
        progressValue = value.coerceIn(0, 100)
        applyProgress()
    }

    private fun buildSegments() {
        val gap = resources.getDimensionPixelSize(R.dimen.meter_segment_gap)
        for (index in 0 until SEGMENT_COUNT) {
            val segment = View(context)
            val params = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
            if (index > 0) params.marginStart = gap
            segment.layoutParams = params
            segment.setBackgroundResource(R.drawable.bg_segment_off)
            binding.meterSegments.addView(segment)
            segments.add(segment)
        }
    }

    private fun applyProgress() {
        val filled = (segments.size * progressValue) / 100
        segments.forEachIndexed { index, segment ->
            if (index < filled) {
                segment.setBackgroundResource(R.drawable.bg_segment_on)
                segment.backgroundTintList = ColorStateList.valueOf(accentColor)
                segment.alpha = 1f
            } else {
                segment.setBackgroundResource(R.drawable.bg_segment_off)
                segment.backgroundTintList = null
                segment.alpha = 0.55f
            }
        }
        ViewCompat.setStateDescription(
            this,
            binding.meterLabel.text.toString() + " " + progressValue
        )
    }

    companion object {
        private const val SEGMENT_COUNT = 12
    }
}
