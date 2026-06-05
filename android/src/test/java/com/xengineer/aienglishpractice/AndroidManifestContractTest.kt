package com.xengineer.aienglishpractice

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidManifestContractTest {
    @Test
    fun declaresSystemSpeechServicesForAndroidPackageVisibility() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("android.speech.RecognitionService"))
        assertTrue(manifest.contains("android.intent.action.TTS_SERVICE"))
    }

    @Test
    fun allowsLocalHttpForDeviceFastApiBridge() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("android:usesCleartextTraffic=\"true\""))
    }
}
