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
            CoachBackendMode.Auto -> "Backend Only"
            CoachBackendMode.BackendOnly -> "Use Local"
            CoachBackendMode.LocalOnly -> "Use Backend"
        }

    val statusText: String
        get() = when {
            isChecking -> "Checking with backend API..."
            lastSource == CoachFeedbackSource.BackendApi -> "Backend API feedback active."
            lastSource == CoachFeedbackSource.LocalFallback -> "Backend unavailable; using local fallback."
            lastSource == CoachFeedbackSource.BackendError -> "Backend error: ${lastError.orEmpty()}"
            mode == CoachBackendMode.LocalOnly -> "Local fallback only."
            mode == CoachBackendMode.BackendOnly -> "Backend API only."
            else -> "Auto backend with local fallback."
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
