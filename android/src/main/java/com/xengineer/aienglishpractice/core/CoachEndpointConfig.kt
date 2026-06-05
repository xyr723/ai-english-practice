package com.xengineer.aienglishpractice.core

enum class CoachEndpointMode {
    UsbDevice,
    Emulator,
    Custom
}

data class CoachEndpointConfig(
    val mode: CoachEndpointMode,
    val customBaseUrl: String = CUSTOM_DEFAULT_BASE_URL
) {
    val baseUrl: String
        get() = when (mode) {
            CoachEndpointMode.UsbDevice -> USB_DEVICE_BASE_URL
            CoachEndpointMode.Emulator -> EMULATOR_BASE_URL
            CoachEndpointMode.Custom -> customBaseUrl
        }

    val title: String
        get() = when (mode) {
            CoachEndpointMode.UsbDevice -> "USB 真机"
            CoachEndpointMode.Emulator -> "Android 模拟器"
            CoachEndpointMode.Custom -> "自定义地址"
        }

    val setupHint: String
        get() = when (mode) {
            CoachEndpointMode.UsbDevice -> "先启动 FastAPI，再执行 adb reverse tcp:8000 tcp:8000。"
            CoachEndpointMode.Emulator -> "模拟器使用 10.0.2.2 访问宿主机 FastAPI。"
            CoachEndpointMode.Custom -> "适合局域网 IP 或隧道地址，后端需允许设备访问。"
        }

    fun useUsbDevice(): CoachEndpointConfig = copy(mode = CoachEndpointMode.UsbDevice)

    fun useEmulator(): CoachEndpointConfig = copy(mode = CoachEndpointMode.Emulator)

    fun useCustom(baseUrl: String): CoachEndpointConfig = copy(
        mode = CoachEndpointMode.Custom,
        customBaseUrl = normalizeBaseUrl(baseUrl)
    )

    companion object {
        const val USB_DEVICE_BASE_URL = "http://127.0.0.1:8000"
        const val EMULATOR_BASE_URL = "http://10.0.2.2:8000"
        const val CUSTOM_DEFAULT_BASE_URL = "http://192.168.1.100:8000"

        fun default(): CoachEndpointConfig = CoachEndpointConfig(
            mode = CoachEndpointMode.UsbDevice,
            customBaseUrl = CUSTOM_DEFAULT_BASE_URL
        )
    }
}

private fun normalizeBaseUrl(baseUrl: String): String {
    val trimmed = baseUrl.trim().trimEnd('/')
    return trimmed.ifBlank { CoachEndpointConfig.CUSTOM_DEFAULT_BASE_URL }
}
