package com.featherroad.quest.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.featherroad.quest.core.di.ViewModelFactory
import com.featherroad.quest.core.ext.applyBottomBarInset
import com.featherroad.quest.core.navigation.Navigator
import com.featherroad.quest.databinding.FragmentSplashBinding
import com.featherroad.quest.presentation.menu.MenuFragment
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var animator: SplashAnimator? = null
    private var loopRunnable: Runnable? = null

    private val viewModel: SplashViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentSplashBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.root.applyBottomBarInset()

        val splashAnimator = SplashAnimator(bound)
        animator = splashAnimator
        splashAnimator.prepare()
        splashAnimator.playEntry()

        val runnable = Runnable {
            try {
                if (!isAdded || binding == null) return@Runnable
                animator?.startLoops()
            } catch (e: Exception) {
            }
        }
        loopRunnable = runnable
        bound.root.postDelayed(runnable, LOOP_START_DELAY_MS)

        viewModel.startTimer()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    if (state.readyToNavigate) {
                        viewModel.consumeNavigation()
                        goToMenu()
                    }
                }
            }
        }
    }

    private fun goToMenu() {
        if (!isAdded) return
        Navigator.replaceRoot(parentFragmentManager, MenuFragment())
    }

    override fun onDestroyView() {
        val pending = loopRunnable
        if (pending != null) {
            binding?.root?.removeCallbacks(pending)
            loopRunnable = null
        }
        animator?.cancel()
        animator = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val LOOP_START_DELAY_MS = 1200L
    }
}
