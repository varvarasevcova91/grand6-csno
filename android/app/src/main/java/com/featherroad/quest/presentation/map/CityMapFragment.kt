package com.featherroad.quest.presentation.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.featherroad.quest.core.di.ViewModelFactory
import com.featherroad.quest.core.ext.applyBottomBarInset
import com.featherroad.quest.core.navigation.Navigator
import com.featherroad.quest.databinding.FragmentCityMapBinding
import com.featherroad.quest.presentation.game.GameFragment
import kotlinx.coroutines.launch

class CityMapFragment : Fragment() {

    private var binding: FragmentCityMapBinding? = null
    private var adapter: MissionAdapter? = null
    private var staggerRunnable: Runnable? = null

    private val viewModel: CityMapViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentCityMapBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.root.applyBottomBarInset()

        val listAdapter = MissionAdapter { row -> viewModel.select(row) }
        adapter = listAdapter
        bound.mapList.layoutManager = LinearLayoutManager(requireContext())
        bound.mapList.adapter = listAdapter

        bound.mapBack.setOnClickListener { Navigator.back(parentFragmentManager) }
        bound.mapStart.setOnClickListener {
            viewModel.commitSelection()
            Navigator.swap(parentFragmentManager, GameFragment(), Navigator.TAG_GAME)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rows.collect { rows ->
                    adapter?.submit(rows)
                    val bind = binding ?: return@collect
                    bind.mapStart.isEnabled = viewModel.canStart()
                    bind.mapStartFrame.alpha = if (viewModel.canStart()) 1f else DISABLED_ALPHA
                    animateList()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.balance.collect { value -> binding?.mapBalance?.setBalance(value) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun animateList() {
        val bound = binding ?: return
        val shift = resources.displayMetrics.density * ROW_SHIFT_DP
        val pending = staggerRunnable
        if (pending != null) bound.mapList.removeCallbacks(pending)
        val runnable = Runnable {
            try {
                if (!isAdded || binding == null) return@Runnable
                val list = binding?.mapList ?: return@Runnable
                for (index in 0 until list.childCount) {
                    val child = list.getChildAt(index)
                    child.alpha = 0f
                    child.translationX = shift
                    child.animate()
                        .alpha(1f)
                        .translationX(0f)
                        .setStartDelay(index * STAGGER_MS)
                        .setDuration(ROW_MS)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
            } catch (e: Exception) {
            }
        }
        staggerRunnable = runnable
        bound.mapList.post(runnable)
    }

    override fun onDestroyView() {
        val pending = staggerRunnable
        if (pending != null) {
            binding?.mapList?.removeCallbacks(pending)
            staggerRunnable = null
        }
        binding?.mapList?.adapter = null
        adapter = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ROW_SHIFT_DP = 24f
        private const val ROW_MS = 280L
        private const val STAGGER_MS = 60L
        private const val DISABLED_ALPHA = 0.55f
    }
}
