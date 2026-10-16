package com.featherroad.quest.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.featherroad.quest.R
import com.featherroad.quest.databinding.DialogPauseBinding

class PauseDialog : DialogFragment() {

    private var binding: DialogPauseBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.Theme_Grand6Csno_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogPauseBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.pauseResume.setOnClickListener { finishWith(ACTION_RESUME) }
        bound.pauseRestart.setOnClickListener { finishWith(ACTION_RESTART) }
        bound.pauseMenu.setOnClickListener { finishWith(ACTION_MENU) }
    }

    private fun finishWith(action: String) {
        val host = parentFragment as? DialogActionListener
        dismissAllowingStateLoss()
        host?.onDialogAction(action)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "pause_dialog"
        const val ACTION_RESUME = "resume"
        const val ACTION_RESTART = "restart"
        const val ACTION_MENU = "menu"
    }
}
