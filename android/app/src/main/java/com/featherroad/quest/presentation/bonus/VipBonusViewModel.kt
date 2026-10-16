package com.featherroad.quest.presentation.bonus

import androidx.lifecycle.ViewModel
import com.featherroad.quest.core.session.SessionStore
import com.featherroad.quest.domain.model.BonusSpin
import com.featherroad.quest.domain.repository.ProfileRepository
import com.featherroad.quest.domain.usecase.SpinVipBonusUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VipBonusViewModel(
    private val spinVipBonusUseCase: SpinVipBonusUseCase,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val mutableSpin = MutableStateFlow<BonusSpin?>(null)
    val spin: StateFlow<BonusSpin?> = mutableSpin.asStateFlow()

    private val mutableBalance = MutableStateFlow(0)
    val balance: StateFlow<Int> = mutableBalance.asStateFlow()

    fun prepare() {
        mutableBalance.value = profileRepository.profile().balance
        if (mutableSpin.value != null) return
        val multiplier = SessionStore.lastRun?.multiplier ?: 1
        val result = spinVipBonusUseCase(multiplier)
        SessionStore.storeBonus(result)
        mutableSpin.value = result
    }

    fun collect() {
        val result = mutableSpin.value ?: return
        SessionStore.applyBonus(result.payout)
    }
}
