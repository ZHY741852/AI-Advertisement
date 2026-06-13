package com.aiadvertisement.domain.usecase

import com.aiadvertisement.core.model.AIAdEnhance
import com.aiadvertisement.core.model.AIChatMessage
import com.aiadvertisement.data.repository.AIRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenerateAdSummaryUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(title: String, description: String): AIAdEnhance {
        return aiRepository.generateAdSummary(title, description)
    }
}

@Singleton
class ChatWithAIUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(messages: List<AIChatMessage>): String {
        return aiRepository.chatWithAI(messages)
    }
}
