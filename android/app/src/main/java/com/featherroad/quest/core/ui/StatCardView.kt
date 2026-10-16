package com.featherroad.quest.core.ui

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import com.featherroad.quest.R
import com.featherroad.quest.core.ext.formatGrouped
import com.featherroad.quest.databinding.ViewStatCardBinding
import android.widget.LinearLayout

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    private var accentColor: Int = ContextCompat.getColor(context, R.color.cyber_cyan)

    init {
        orientation = VERTICAL
        gravity = Gravity.START
        setBackgroundResource(R.drawable.bg_stat_card)
        val horizontal = resources.getDimensionPixelSize(R.dimen.gap_m)
        val vertical = resources.getDimensionPixelSize(R.dimen.gap_s)
        updatePadding(left = horizontal, top = vertical, right = horizontal, bottom = vertical)

        val typed = context.obtainStyledAttributes(attrs, R.styleable.StatCardView)
        val label = typed.getString(R.styleable.StatCardView_statLabel).orEmpty()
        val value = typed.getString(R.styleable.StatCardView_statValue).orEmpty()
        accentColor = typed.getColor(R.styleable.StatCardView_statAccent, accentColor)
        typed.recycle()

        binding.statLabel.text = label
        binding.statValue.text = value
        applyAccent()
    }

    fun setLabel(label: String) {
        binding.statLabel.text = label
        updateStateDescription()
    }

    fun setValue(value: String) {
        binding.statValue.text = value
        updateStateDescription()
    }

    fun setAccent(color: Int) {
        accentColor = color
        applyAccent()
    }

    fun bindCount(value: Int) {
        if (value <= 0) {
            visibility = View.GONE
            return
        }
        visibility = View.VISIBLE
        setValue(formatGrouped(value))
    }

    private fun applyAccent() {
        binding.statValue.setTextColor(accentColor)
        binding.statDot.backgroundTintList = ColorStateList.valueOf(accentColor)
        updateStateDescription()
    }

    private fun updateStateDescription() {
        ViewCompat.setStateDescription(
            this,
            binding.statLabel.text.toString() + " " + binding.statValue.text.toString()
        )
    }
}
