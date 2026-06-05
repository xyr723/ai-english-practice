package com.xengineer.aienglishpractice.core

enum class CoachBackendMode {
    Auto,
    BackendOnly,
    LocalOnly
}

enum class CoachFeedbackSource {
    BackendApi,
    LocalFallback,
    BackendError
}

data class CoachBackendUiState(
    val mode: CoachBackendMode,
    val baseUrl: String,
    val isChecking: Boolean = false,
    val lastSource: CoachFeedbackSource? = null,
    val lastError: String? = null
) {
    val shouldTryBackend: Boolean
        get() = mode == CoachBackendMode.Auto || mode == CoachBackendMode.BackendOnly

    val shouldUseLocalFallback: Boolean
        get() = mode == CoachBackendMode.LocalOnly ||
            (mode == CoachBackendMode.Auto && lastSource == CoachFeedbackSource.LocalFallback)

    val modeAction: String
        get() = when (mode) {
            CoachBackendMode.Auto -> "仅云端"
            CoachBackendMode.BackendOnly -> "本地模式"
            CoachBackendMode.LocalOnly -> "自动模式"
        }

    val statusText: String
        get() = when {
            isChecking -> "正在连接云端。"
            lastSource == CoachFeedbackSource.BackendApi -> "云端教练已启用。"
            lastSource == CoachFeedbackSource.LocalFallback -> "云端不可用，已用本地分析。"
            lastSource == CoachFeedbackSource.BackendError -> "云端错误：${lastError.orEmpty()}"
            mode == CoachBackendMode.LocalOnly -> "仅使用本地分析。"
            mode == CoachBackendMode.BackendOnly -> "仅连接云端教练。"
            else -> "云端优先，本地备用。"
        }

    fun useAuto(): CoachBackendUiState = copy(
        mode = CoachBackendMode.Auto,
        isChecking = false,
        lastError = null
    )

    fun useBackendOnly(): CoachBackendUiState = copy(
        mode = CoachBackendMode.BackendOnly,
        isChecking = false,
        lastError = null,
        lastSource = null
    )

    fun useLocalOnly(): CoachBackendUiState = copy(
        mode = CoachBackendMode.LocalOnly,
        isChecking = false,
        lastError = null,
        lastSource = CoachFeedbackSource.LocalFallback
    )

    fun nextMode(): CoachBackendUiState = when (mode) {
        CoachBackendMode.Auto -> useBackendOnly()
        CoachBackendMode.BackendOnly -> useLocalOnly()
        CoachBackendMode.LocalOnly -> useAuto()
    }

    fun withChecking(): CoachBackendUiState = copy(
        isChecking = true,
        lastError = null
    )

    fun withBackendSuccess(): CoachBackendUiState = copy(
        isChecking = false,
        lastSource = CoachFeedbackSource.BackendApi,
        lastError = null
    )

    fun withBackendFailure(message: String): CoachBackendUiState = if (mode == CoachBackendMode.Auto) {
        copy(
            isChecking = false,
            lastSource = CoachFeedbackSource.LocalFallback,
            lastError = message
        )
    } else {
        copy(
            isChecking = false,
            lastSource = CoachFeedbackSource.BackendError,
            lastError = message
        )
    }

    companion object {
        const val DEFAULT_BASE_URL = "http://10.0.2.2:8000"

        fun initial(baseUrl: String = DEFAULT_BASE_URL): CoachBackendUiState = CoachBackendUiState(
            mode = CoachBackendMode.Auto,
            baseUrl = baseUrl
        )
    }
}
