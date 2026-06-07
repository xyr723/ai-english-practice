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
            fallbackReplies = fallbackRepliesFor(descriptor, normalizedPrompt),
            fallbackReplyTranslations = fallbackReplyTranslationsFor(descriptor, normalizedPrompt)
        )
    }

    fun descriptorFor(prompt: String): SceneDescriptor {
        val text = prompt.lowercase()
        return when {
            listOf("餐厅", "点餐", "咖啡", "饮品", "restaurant", "cafe", "coffee", "order").any { text.contains(it) } ->
                SceneDescriptor.restaurant()

            listOf("面试", "求职", "简历", "interview", "job", "resume").any { text.contains(it) } ->
                SceneDescriptor.interview()

            listOf("会议", "讨论", "汇报", "项目", "meeting", "presentation", "project").any { text.contains(it) } ->
                SceneDescriptor.meeting()

            listOf("机场", "改签", "航班", "登机", "airport", "flight", "rebook").any { text.contains(it) } ->
                SceneDescriptor.airport()

            listOf("购物", "商场", "砍价", "讨价", "shopping", "mall", "price", "bargain").any { text.contains(it) } ->
                SceneDescriptor.shopping()

            listOf("健身房", "健身", "办卡", "会员", "gym", "fitness", "membership").any { text.contains(it) } ->
                SceneDescriptor(
                    theme = "gym",
                    backgroundColor = "#EAF1EC",
                    objects = listOf("counter", "card", "screen", "chair"),
                    role = "gym membership advisor",
                    visualTheme = "office"
                )

            listOf("医院", "挂号", "门诊", "看病", "hospital", "clinic", "doctor").any { text.contains(it) } ->
                SceneDescriptor(
                    theme = "hospital",
                    backgroundColor = "#E7F4F1",
                    objects = listOf("counter", "screen", "chair", "window"),
                    role = "clinic receptionist",
                    visualTheme = "office"
                )

            listOf("图书馆", "借书", "还书", "library", "book", "borrow", "return").any { text.contains(it) } ->
                SceneDescriptor.library()

            else -> SceneDescriptor.generic(prompt)
        }
    }

    private fun roleLabelFor(descriptor: SceneDescriptor): String = when (descriptor.theme) {
        "restaurant" -> "服务员"
        "office" -> "面试官"
        "meeting" -> "项目负责人"
        "airport" -> "航空工作人员"
        "shopping" -> "店员"
        "gym" -> "健身房顾问"
        "hospital" -> "医院工作人员"
        "library" -> "图书馆工作人员"
        else -> "场景教练"
    }

    private fun openingFor(descriptor: SceneDescriptor, prompt: String): String = when (descriptor.theme) {
        "restaurant" -> "Welcome! What would you like to order today?"
        "office" -> "Thanks for joining today. Could you briefly introduce yourself?"
        "meeting" -> "Let's review the plan. What should we discuss first?"
        "airport" -> "Hello. How can I help you with your flight today?"
        "shopping" -> "Hello. What are you looking for today?"
        "gym" -> "Hello. Are you interested in a gym membership plan today?"
        "hospital" -> "Hello. How can I help you at the clinic today?"
        "library" -> "Hello. What kind of book would you like to borrow today?"
        else -> "Hello. Let's practice this situation: $prompt. What would you like to say first?"
    }

    private fun openingTranslationFor(descriptor: SceneDescriptor): String = when (descriptor.theme) {
        "restaurant" -> "欢迎光临！今天您想点什么？"
        "office" -> "感谢你今天来参加面试。可以简要介绍一下自己吗？"
        "meeting" -> "我们来回顾一下计划。应该先讨论什么？"
        "airport" -> "您好。今天我能为您的航班提供什么帮助？"
        "shopping" -> "您好。今天您想买什么？"
        "gym" -> "您好。今天想了解健身房会员套餐吗？"
        "hospital" -> "您好。今天在诊所我能帮您什么？"
        "library" -> "您好。今天您想借哪类书？"
        else -> "你好。我们来练习这个场景。你想先说什么？"
    }

    private fun toneFor(descriptor: SceneDescriptor): String = when (descriptor.theme) {
        "restaurant" -> "餐厅点餐场景"
        "office" -> "正式面试训练"
        "meeting" -> "协作会议场景"
        "airport" -> "机场服务场景"
        "shopping" -> "购物服务场景"
        "gym" -> "健身房办卡场景"
        "hospital" -> "医院挂号场景"
        "library" -> "图书馆借书场景"
        else -> "自定义对话场景"
    }

    private fun goalsFor(descriptor: SceneDescriptor): List<String> = when (descriptor.theme) {
        "restaurant" -> listOf("order_food_or_drink", "use_polite_expression", "answer_follow_up_question")
        "office" -> listOf("introduce_self", "describe_experience", "answer_follow_up_question")
        "meeting" -> listOf("give_opinion", "ask_clarifying_question", "confirm_next_step")
        "airport" -> listOf("explain_request", "provide_flight_info", "confirm_next_step")
        "shopping" -> listOf("explain_request", "use_polite_expression", "confirm_next_step")
        "gym" -> listOf("explain_membership_need", "compare_plan", "confirm_next_step")
        "hospital" -> listOf("explain_symptom_or_request", "provide_basic_info", "confirm_next_step")
        "library" -> listOf("explain_request", "provide_book_info", "confirm_next_step")
        else -> listOf("explain_request", "use_polite_expression", "answer_follow_up_question")
    }

    private fun keywordsFor(descriptor: SceneDescriptor): List<String> = when (descriptor.theme) {
        "restaurant" -> listOf("coffee", "tea", "sandwich", "takeaway", "please", "order")
        "office" -> listOf("experience", "project", "team", "strength", "learn", "interview")
        "meeting" -> listOf("timeline", "risk", "plan", "next step", "agree", "meeting")
        "airport" -> listOf("flight", "change", "ticket", "time", "please", "rebook")
        "shopping" -> listOf("price", "size", "discount", "bag", "please", "buy")
        "gym" -> listOf("membership", "plan", "gym", "card", "monthly", "please")
        "hospital" -> listOf("appointment", "doctor", "clinic", "register", "please", "sick")
        "library" -> listOf("book", "borrow", "return", "library", "card", "please")
        else -> listOf("please", "help", "need", "question", "confirm")
    }

    private fun turnsFor(descriptor: SceneDescriptor, prompt: String): List<ScenarioTurn> = when (descriptor.theme) {
        "restaurant" -> listOf(
            ScenarioTurn(
                id = "custom-restaurant-turn-1",
                expectedIntent = "order_food_or_drink",
                reply = "Sure. Would you like anything to drink?",
                replyTranslation = "当然。你想喝点什么吗？"
            ),
            ScenarioTurn(
                id = "custom-restaurant-turn-2",
                expectedIntent = "answer_follow_up_question",
                reply = "Great. Is that for here or takeaway?",
                replyTranslation = "好的。是在这里吃还是外带？"
            )
        )

        "office" -> listOf(
            ScenarioTurn(
                id = "custom-office-turn-1",
                expectedIntent = "introduce_self",
                reply = "Good. Could you tell me about one project you worked on?",
                replyTranslation = "很好。你能讲一个你参与过的项目吗？"
            ),
            ScenarioTurn(
                id = "custom-office-turn-2",
                expectedIntent = "describe_experience",
                reply = "What was your main contribution to that project?",
                replyTranslation = "你在那个项目中的主要贡献是什么？"
            )
        )

        "meeting" -> listOf(
            ScenarioTurn(
                id = "custom-meeting-turn-1",
                expectedIntent = "give_opinion",
                reply = "Thanks. What is the main timeline risk?",
                replyTranslation = "谢谢。主要的时间风险是什么？"
            ),
            ScenarioTurn(
                id = "custom-meeting-turn-2",
                expectedIntent = "confirm_next_step",
                reply = "That makes sense. What next step should we assign?",
                replyTranslation = "有道理。我们应该安排什么下一步？"
            )
        )

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

        "gym" -> listOf(
            ScenarioTurn(
                id = "custom-gym-turn-1",
                expectedIntent = "explain_membership_need",
                reply = "Sure. Which membership plan are you interested in: monthly or yearly?",
                replyTranslation = "当然。您想了解月卡还是年卡会员套餐？"
            ),
            ScenarioTurn(
                id = "custom-gym-turn-2",
                expectedIntent = "compare_plan",
                reply = "I can explain the price and facilities. How often do you plan to visit the gym?",
                replyTranslation = "我可以说明价格和设施。您计划多久来一次健身房？"
            ),
            ScenarioTurn(
                id = "custom-gym-turn-3",
                expectedIntent = "confirm_next_step",
                reply = "Great. Please confirm your preferred membership card before I register it.",
                replyTranslation = "好的。登记前请确认您想办理的会员卡。"
            )
        )

        "hospital" -> listOf(
            ScenarioTurn(
                id = "custom-hospital-turn-1",
                expectedIntent = "explain_symptom_or_request",
                reply = "Sure. Would you like to make an appointment or register for today's clinic?",
                replyTranslation = "当然。您想预约还是挂今天的门诊？"
            ),
            ScenarioTurn(
                id = "custom-hospital-turn-2",
                expectedIntent = "provide_basic_info",
                reply = "Please tell me your name and which department you need.",
                replyTranslation = "请告诉我您的姓名和需要挂哪个科室。"
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

    private fun fallbackRepliesFor(descriptor: SceneDescriptor, prompt: String): List<String> = when (descriptor.theme) {
        "restaurant" -> listOf(
            "Please tell me what you would like to order.",
            "Could you repeat your order politely?"
        )
        "office" -> listOf(
            "Please answer with one concrete work example.",
            "Could you make your interview answer more specific?"
        )
        "meeting" -> listOf(
            "Could you share one concrete point for the meeting?",
            "Please explain the risk or next step in one sentence."
        )
        "airport" -> listOf(
            "Could you explain which flight you need help with?",
            "Please say the ticket change request again, politely."
        )
        "shopping" -> listOf(
            "Could you explain what you want to buy?",
            "Please ask about the price, size, or discount again."
        )
        "gym" -> listOf(
            "Could you explain which membership plan you need?",
            "Please ask about the gym card or monthly price again."
        )
        "hospital" -> listOf(
            "Could you explain whether you need an appointment or registration?",
            "Please say the clinic request again, politely."
        )
        "library" -> listOf(
            "Could you explain which book or topic you need?",
            "Please ask about borrowing or returning the book again."
        )
        else -> listOf(
            "Could you explain what you need in the $prompt situation?",
            "Please say the key request for $prompt again, politely."
        )
    }

    private fun fallbackReplyTranslationsFor(descriptor: SceneDescriptor, prompt: String): List<String> = when (descriptor.theme) {
        "restaurant" -> listOf("请告诉我你想点什么。", "请礼貌地再说一次你的点单。")
        "office" -> listOf("请用一个具体工作例子来回答。", "你能把面试回答说得更具体一点吗？")
        "meeting" -> listOf("你能分享一个会议中的具体观点吗？", "请用一句话说明风险或下一步。")
        "airport" -> listOf("请说明您需要帮助的是哪一班航班。", "请礼貌地再说一次改签请求。")
        "shopping" -> listOf("请说明你想买什么。", "请再次询问价格、尺码或折扣。")
        "gym" -> listOf("请说明您想了解哪种会员套餐。", "请再次询问健身卡或月费。")
        "hospital" -> listOf("请说明您需要预约还是挂号。", "请礼貌地再说一次门诊需求。")
        "library" -> listOf("请说明您需要哪本书或哪个主题。", "请再次询问借书或还书。")
        else -> listOf(
            "请说明你在“$prompt”这个场景里的需求。",
            "请礼貌地再说一次“$prompt”的关键请求。"
        )
    }
}
