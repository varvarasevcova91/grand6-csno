package com.featherroad.quest.presentation.garage

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.featherroad.quest.R
import com.featherroad.quest.core.config.GameConfig
import com.featherroad.quest.core.di.ViewModelFactory
import com.featherroad.quest.core.ext.applyBottomBarInset
import com.featherroad.quest.core.navigation.Navigator
import com.featherroad.quest.databinding.FragmentGarageBinding
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class GarageFragment : Fragment() {

    private var binding: FragmentGarageBinding? = null
    private var carAdapter: CarAdapter? = null
    private var outfitAdapter: OutfitAdapter? = null

    private val viewModel: GarageViewModel by viewModels { ViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGarageBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = binding ?: return
        bound.root.applyBottomBarInset()

        val cars = CarAdapter { row -> viewModel.selectCar(row) }
        val outfits = OutfitAdapter { row -> viewModel.selectOutfit(row) }
        carAdapter = cars
        outfitAdapter = outfits

        bound.garageCars.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        bound.garageCars.adapter = cars
        bound.garageOutfits.layoutManager = GridLayoutManager(requireContext(), GRID_COLUMNS)
        bound.garageOutfits.adapter = outfits

        bound.garageBack.setOnClickListener { Navigator.back(parentFragmentManager) }
        bound.garageTabCars.setOnClickListener { viewModel.selectTab(true) }
        bound.garageTabStyle.setOnClickListener { viewModel.selectTab(false) }
        bound.garageUpgrade.setOnClickListener { viewModel.upgradeSelected() }

        buildSwatches(bound.garageSwatches)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state -> render(state) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun render(state: GarageUiState) {
        val bound = binding ?: return
        bound.garageBalance.setBalance(state.balance)
        carAdapter?.submit(state.cars)
        outfitAdapter?.submit(state.outfits)

        bound.garageCarsSection.visibility =
            if (state.carsTabActive) View.VISIBLE else View.GONE
        bound.garageOutfits.visibility =
            if (state.carsTabActive) View.GONE else View.VISIBLE
        bound.garageUpgradeFrame.visibility =
            if (state.carsTabActive) View.VISIBLE else View.GONE

        styleTab(bound.garageTabCars, state.carsTabActive)
        styleTab(bound.garageTabStyle, !state.carsTabActive)
        highlightSwatch(bound.garageSwatches, state.paintIndex)
    }

    private fun styleTab(button: MaterialButton, active: Boolean) {
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

    private fun buildSwatches(host: LinearLayout) {
        if (host.childCount > 0) return
        val size = resources.getDimensionPixelSize(R.dimen.swatch_size)
        val gap = resources.getDimensionPixelSize(R.dimen.gap_s)
        GameConfig.PAINT_COLORS.forEachIndexed { index, hex ->
            val swatch = View(host.context)
            val params = LinearLayout.LayoutParams(size, size)
            if (index > 0) params.marginStart = gap
            swatch.layoutParams = params
            swatch.setBackgroundResource(R.drawable.bg_swatch)
            swatch.backgroundTintList = ColorStateList.valueOf(Color.parseColor(hex))
            swatch.minimumWidth = size
            swatch.minimumHeight = size
            swatch.setOnClickListener { viewModel.selectPaint(index) }
            host.addView(swatch)
        }
    }

    private fun highlightSwatch(host: LinearLayout, index: Int) {
        for (position in 0 until host.childCount) {
            val child = host.getChildAt(position)
            child.alpha = if (position == index) 1f else INACTIVE_ALPHA
            child.scaleX = if (position == index) ACTIVE_SCALE else 1f
            child.scaleY = if (position == index) ACTIVE_SCALE else 1f
        }
    }

    override fun onDestroyView() {
        binding?.let { bound ->
            bound.garageCars.adapter = null
            bound.garageOutfits.adapter = null
        }
        carAdapter = null
        outfitAdapter = null
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val GRID_COLUMNS = 2
        private const val INACTIVE_ALPHA = 0.5f
        private const val ACTIVE_SCALE = 1.15f
    }
}
