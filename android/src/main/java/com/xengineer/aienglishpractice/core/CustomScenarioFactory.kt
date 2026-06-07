package com.xengineer.aienglishpractice.core

object CustomScenarioFactory {
    fun fromPrompt(prompt: String): PracticeScenario {
        val normalizedPrompt = prompt.trim().ifBlank { "自定义场景" }
        val descriptor = descriptorFor(normalizedPrompt)
        val theme = descriptor.theme
        val safeIdSuffix = normalizedPrompt
            .lowercase()
            .filter { it.isLetterOrDigit() }
            .take(16)
            .ifBlank { "scene" }

        return PracticeScenario(
            id = "custom-$theme-$safeIdSuffix",
            name = normalizedPrompt,
            role = roleLabelFor(descriptor),
            opening = openingFor(descriptor, normalizedPrompt),
            openingTranslation = openingTranslationFor(descriptor),
            description = "根据“$normalizedPrompt”即时生成的英语口语练习场景。",
            level = "A2-B1",
            estimatedMinutes = 8,
            sceneTone = toneFor(descriptor),
            sceneDescriptor = descriptor,
            goals = goalsFor(descriptor),
            keywords = keywordsFor(descriptor),
            turns = turnsFor(descriptor, normalizedPrompt),
            fallbackReplies = listOf(
                "Could you explain what you need in the $normalizedPrompt situation?",
                "Please say the key request for $normalizedPrompt again, politely."
            ),
            fallbackReplyTranslations = listOf(
                "请说明你在“$normalizedPrompt”这个场景里的需求。",
                "请礼貌地再说一次“$normalizedPrompt”的关键请求。"
            )
        )
    }

    fun descriptorFor(prompt: String): SceneDescriptor {
        val text = prompt.lowercase()
        return when {
            listOf("机场", "改签", "航班", "登机", "airport", "flight", "rebook").any { text.contains(it) } ->
                SceneDescriptor.airport()

            listOf("购物", "商场", "砍价", "讨价", "shopping", "mall", "price", "bargain").any { text.contains(it) } ->
                SceneDescriptor.shopping()

            listOf("医院", "挂号", "门诊", "看病", "hospital", "clinic", "doctor").any { text.contains(it) } ->
                SceneDescriptor(
                    theme = "hospital",
                    backgroundColor = "#E7F4F1",
                    objects = listOf("counter", "screen", "chair", "window"),
                    role = "clinic receptionist"
                )

            listOf("图书馆", "借书", "还书", "library", "book", "borrow", "return").any { text.contains(it) } ->
                SceneDescriptor.library()

            else -> SceneDescriptor.generic(prompt)
        }
    }

    private fun roleLabelFor(descriptor: SceneDescriptor): String = when (descriptor.theme) {
        "airport" -> "航空工作人员"
        "shopping" -> "店员"
        "hospital" -> "医院工作人员"
        "library" -> "图书馆工作人员"
        else -> "场景教练"
    }

    private fun openingFor(descriptor: SceneDescriptor, prompt: String): String = when (descriptor.theme) {
        "airport" -> "Hello. How can I help you with your flight today?"
        "shopping" -> "Hello. What are you looking for today?"
        "hospital" -> "Hello. How can I help you at the clinic today?"
        "library" -> "Hello. What kind of book would you like to borrow today?"
        else -> "Hello. Let's practice this situation: $prompt. What would you like to say first?"
    }

    private fun openingTranslationFor(descriptor: SceneDescriptor): String = when (descriptor.theme) {
        "airport" -> "您好。今天我能为您的航班提供什么帮助？"
        "shopping" -> "您好。今天您想买什么？"
        "hospital" -> "您好。今天在诊所我能帮您什么？"
        "library" -> "您好。今天您想借哪类书？"
        else -> "你好。我们来练习这个场景。你想先说什么？"
    }

    private fun toneFor(descriptor: SceneDescriptor): String = when (descriptor.theme) {
        "airport" -> "机场服务场景"
        "shopping" -> "购物服务场景"
        "hospital" -> "医院挂号场景"
        "library" -> "图书馆借书场景"
        else -> "自定义对话场景"
    }

    private fun goalsFor(descriptor: SceneDescriptor): List<String> = when (descriptor.theme) {
        "airport" -> listOf("explain_request", "provide_flight_info", "confirm_next_step")
        "shopping" -> listOf("explain_request", "use_polite_expression", "confirm_next_step")
        "library" -> listOf("explain_request", "provide_book_info", "confirm_next_step")
        else -> listOf("explain_request", "use_polite_expression", "answer_follow_up_question")
    }

    private fun keywordsFor(descriptor: SceneDescriptor): List<String> = when (descriptor.theme) {
        "airport" -> listOf("flight", "change", "ticket", "time", "please", "rebook")
        "shopping" -> listOf("price", "size", "discount", "bag", "please", "buy")
        "hospital" -> listOf("appointment", "doctor", "clinic", "register", "please", "sick")
        "library" -> listOf("book", "borrow", "return", "library", "card", "please")
        else -> listOf("please", "help", "need", "question", "confirm")
    }

    private fun turnsFor(descriptor: SceneDescriptor, prompt: String): List<ScenarioTurn> = when (descriptor.theme) {
        "airport" -> listOf(
            ScenarioTurn(
                id = "custom-airport-turn-1",
                expectedIntent = "explain_request",
                reply = "Sure. Which flight would you like to change?",
                replyTranslation = "当然。您想改签哪一班航班？"
            ),
            ScenarioTurn(
                id = "custom-airport-turn-2",
                expectedIntent = "provide_flight_info",
                reply = "I can check that. Do you prefer an earlier or later flight?",
                replyTranslation = "我可以帮您查询。您更想改到更早还是更晚的航班？"
            ),
            ScenarioTurn(
                id = "custom-airport-turn-3",
                expectedIntent = "confirm_next_step",
                reply = "Great. Please confirm the new time before I update the ticket.",
                replyTranslation = "好的。更新机票前，请确认新的时间。"
            )
        )

        "shopping" -> listOf(
            ScenarioTurn(
                id = "custom-shopping-turn-1",
                expectedIntent = "explain_request",
                reply = "Sure. What size or color do you prefer?",
                replyTranslation = "当然。您更喜欢什么尺码或颜色？"
            ),
            ScenarioTurn(
                id = "custom-shopping-turn-2",
                expectedIntent = "confirm_next_step",
                reply = "I can check the price. Would you like to try it first?",
                replyTranslation = "我可以查一下价格。您想先试一下吗？"
            )
        )

        "library" -> listOf(
            ScenarioTurn(
                id = "custom-library-turn-1",
                expectedIntent = "explain_request",
                reply = "Sure. Which book or topic are you looking for?",
                replyTranslation = "当然。您想找哪本书或哪个主题？"
            ),
            ScenarioTurn(
                id = "custom-library-turn-2",
                expectedIntent = "provide_book_info",
                reply = "I can check the shelf. Do you have a library card?",
                replyTranslation = "我可以查一下书架。您有借书证吗？"
            ),
            ScenarioTurn(
                id = "custom-library-turn-3",
                expectedIntent = "confirm_next_step",
                reply = "Great. Please confirm the return date before you borrow it.",
                replyTranslation = "好的。借走前请确认归还日期。"
            )
        )

        else -> listOf(
            ScenarioTurn(
                id = "custom-turn-1",
                expectedIntent = "explain_request",
                reply = "Thanks. In the $prompt situation, what do you need first?",
                replyTranslation = "谢谢。在“$prompt”这个场景里，你首先需要什么？"
            ),
            ScenarioTurn(
                id = "custom-turn-2",
                expectedIntent = "answer_follow_up_question",
                reply = "Good. What detail about $prompt should we confirm next?",
                replyTranslation = "很好。接下来我们要确认“$prompt”的哪个细节？"
            )
        )
    }
}
