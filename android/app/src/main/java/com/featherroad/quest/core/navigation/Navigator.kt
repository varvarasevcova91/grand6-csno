package com.featherroad.quest.core.navigation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.featherroad.quest.R

object Navigator {

    const val TAG_GAME = "game"
    const val TAG_MAP = "map"
    const val TAG_GARAGE = "garage"
    const val TAG_BONUS = "bonus"
    const val TAG_RESULT = "result"

    fun replaceRoot(manager: FragmentManager, fragment: Fragment) {
        if (manager.isStateSaved) return
        manager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in_scale, R.anim.fade_out_scale)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    fun push(manager: FragmentManager, fragment: Fragment, tag: String) {
        if (manager.isStateSaved) return
        manager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_up,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_down
            )
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    fun swap(manager: FragmentManager, fragment: Fragment, tag: String) {
        if (manager.isStateSaved) return
        manager.popBackStackImmediate()
        push(manager, fragment, tag)
    }

    fun popToRoot(manager: FragmentManager) {
        if (manager.isStateSaved) return
        manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun back(manager: FragmentManager) {
        if (manager.isStateSaved) return
        manager.popBackStack()
    }
}
