package com.xengineer.aienglishpractice.core

object SpeechTranscriptFormatter {
    fun normalize(text: String): String {
        val trimmed = text.trim()
        if (!trimmed.hasLetters() || !trimmed.isAllCapsSpeech()) {
            return trimmed
        }

        return trimmed
            .lowercase()
            .capitalizeSentences()
            .fixEnglishPronounI()
    }

    private fun String.hasLetters(): Boolean = any { it.isLetter() }

    private fun String.isAllCapsSpeech(): Boolean = filter { it.isLetter() }.all { it.isUpperCase() }

    private fun String.capitalizeSentences(): String {
        val chars = toCharArray()
        var shouldCapitalize = true
        for (index in chars.indices) {
            val char = chars[index]
            when {
                char.isLetter() && shouldCapitalize -> {
                    chars[index] = char.uppercaseChar()
                    shouldCapitalize = false
                }
                char in ".!?" -> shouldCapitalize = true
                char.isLetter() -> shouldCapitalize = false
            }
        }
        return String(chars)
    }

    private fun String.fixEnglishPronounI(): String = replace(
        Regex("\\bi('m|'d|'ll|'ve)?\\b"),
    ) { match ->
        "I" + match.value.drop(1)
    }
}
