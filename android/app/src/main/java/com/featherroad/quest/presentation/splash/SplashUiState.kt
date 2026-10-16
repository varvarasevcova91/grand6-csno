package com.featherroad.quest.presentation.splash

data class SplashUiState(
    val loadingLabel: String,
    val readyToNavigate: Boolean
) {
    companion object {
        fun initial(): SplashUiState = SplashUiState(
            loadingLabel = "",
            readyToNavigate = false
        )
    }
}
