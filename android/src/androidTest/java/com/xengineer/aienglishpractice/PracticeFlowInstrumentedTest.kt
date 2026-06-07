package com.xengineer.aienglishpractice

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PracticeFlowInstrumentedTest {
    private lateinit var device: UiDevice

    @Before
    fun launchApp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        device = UiDevice.getInstance(instrumentation)

        device.pressHome()
        assertNotNull(
            "App launch intent should exist",
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        )

        val launchResult = device.executeShellCommand(
            "am start -W ${context.packageName}/.MainActivity"
        )
        assertTrue(
            "MainActivity should start through the same shell path used by adb smoke: $launchResult",
            launchResult.contains("Status: ok") || launchResult.contains("Warning: Activity not started")
        )

        assertTrue(
            "Home screen should render after app launch",
            device.wait(Until.hasObject(By.textContains("今天想练习什么场景")), 15_000)
        )
    }

    @Test
    fun appLaunchesAndOpensRestaurantPractice() {
        val restaurantTile = device.wait(Until.findObject(By.text("点餐")), 10_000)
        assertNotNull("Restaurant scenario tile should be visible", restaurantTile)
        restaurantTile.click()

        assertTrue(
            "Restaurant practice screen should open from the real app home screen",
            device.wait(Until.hasObject(By.text("餐厅点餐")), 10_000) ||
                device.wait(Until.hasObject(By.textContains("点击开始录音")), 10_000)
        )
    }
}
