package com.xengineer.aienglishpractice.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoachEndpointConfigTest {
    @Test
    fun defaultEndpointUsesUsbDeviceLoopbackForAdbReverse() {
        val config = CoachEndpointConfig.default()

        assertEquals(CoachEndpointMode.UsbDevice, config.mode)
        assertEquals("http://127.0.0.1:8000", config.baseUrl)
        assertTrue(config.setupHint.contains("adb reverse"))
    }

    @Test
    fun emulatorEndpointUsesAndroidEmulatorHostAlias() {
        val config = CoachEndpointConfig.default().useEmulator()

        assertEquals(CoachEndpointMode.Emulator, config.mode)
        assertEquals("http://10.0.2.2:8000", config.baseUrl)
    }

    @Test
    fun customEndpointNormalizesWhitespaceAndTrailingSlash() {
        val config = CoachEndpointConfig.default().useCustom("  http://192.168.1.23:8000/  ")

        assertEquals(CoachEndpointMode.Custom, config.mode)
        assertEquals("http://192.168.1.23:8000", config.baseUrl)
    }
}
