package com.featherroad.quest.presentation.dialog

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.featherroad.quest.R
import com.featherroad.quest.core.di.ServiceLocator
import com.featherroad.quest.databinding.DialogSettingsBinding
import com.google.android.material.button.MaterialButton

class SettingsDialog : DialogFragment() {

    private var binding: DialogSettingsBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_Grand6Csno_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogSettingsBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        val repository = ServiceLocator.profileRepository
        val profile = repository.profile()

        bound.settingsSound.isChecked = profile.soundOn
        bound.settingsMusic.isChecked = profile.musicOn
        bound.settingsVibration.isChecked = profile.vibrationOn
        applyQuality(profile.quality)

        bound.settingsSound.setOnCheckedChangeListener { _, checked ->
            repository.save(repository.profile().copy(soundOn = checked))
        }
        bound.settingsMusic.setOnCheckedChangeListener { _, checked ->
            repository.save(repository.profile().copy(musicOn = checked))
        }
        bound.settingsVibration.setOnCheckedChangeListener { _, checked ->
            repository.save(repository.profile().copy(vibrationOn = checked))
        }

        bound.settingsQualityLow.setOnClickListener { selectQuality(QUALITY_LOW) }
        bound.settingsQualityMed.setOnClickListener { selectQuality(QUALITY_MED) }
        bound.settingsQualityHigh.setOnClickListener { selectQuality(QUALITY_HIGH) }

        bound.settingsReset.setOnClickListener {
            repository.reset()
            notifyHost(ACTION_RESET)
            dismissAllowingStateLoss()
        }
        bound.settingsClose.setOnClickListener {
            notifyHost(ACTION_CLOSE)
            dismissAllowingStateLoss()
        }
    }

    private fun selectQuality(level: Int) {
        val repository = ServiceLocator.profileRepository
        repository.save(repository.profile().copy(quality = level))
        applyQuality(level)
    }

    private fun applyQuality(level: Int) {
        val bound = binding ?: return
        styleChip(bound.settingsQualityLow, level == QUALITY_LOW)
        styleChip(bound.settingsQualityMed, level == QUALITY_MED)
        styleChip(bound.settingsQualityHigh, level == QUALITY_HIGH)
    }

    private fun styleChip(button: MaterialButton, active: Boolean) {
        val context = button.context
        val accent = ContextCompat.getColor(context, R.color.cyber_cyan)
        val muted = ContextCompat.getColor(context, R.color.stroke_white_soft)
        val fill = ContextCompat.getColor(
            context,
            if (active) R.color.fill_cyan_soft else R.color.fill_white_soft
        )
        button.strokeColor = ColorStateList.valueOf(if (active) accent else muted)
        button.backgroundTintList = ColorStateList.valueOf(fill)
        button.setTextColor(
            ContextCompat.getColor(
                context,
                if (active) R.color.text_primary else R.color.text_secondary
            )
        )
    }

    private fun notifyHost(action: String) {
        val host = parentFragment as? DialogActionListener ?: return
        host.onDialogAction(action)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "settings_dialog"
        const val ACTION_RESET = "reset"
        const val ACTION_CLOSE = "close"

        private const val QUALITY_LOW = 0
        private const val QUALITY_MED = 1
        private const val QUALITY_HIGH = 2
    }
}
