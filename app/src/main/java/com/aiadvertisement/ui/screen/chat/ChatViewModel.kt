package com.aiadvertisement.ui.screen.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiadvertisement.core.model.AIChatMessage
import com.aiadvertisement.domain.usecase.ChatWithAIUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatWithAIUseCase: ChatWithAIUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        val welcomeMessage = AIChatMessage(
            role = "assistant",
            content = "你好！我是智投AI助手。我可以帮你：\n1. 分析广告内容和营销策略\n2. 提供创意文案建议\n3. 分析目标受众和投放场景\n\n请问有什么可以帮你的？",
            timestamp = System.currentTimeMillis()
        )
        _uiState.value = _uiState.value.copy(messages = listOf(welcomeMessage))
    }

    fun sendMessage(userInput: String) {
        if (userInput.isBlank()) return

        val userMessage = AIChatMessage(
            role = "user",
            content = userInput,
            timestamp = System.currentTimeMillis()
        )

        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(userMessage)

        _uiState.value = _uiState.value.copy(
            messages = currentMessages,
            isLoading = true,
            inputText = ""
        )

        viewModelScope.launch {
            val response = chatWithAIUseCase(currentMessages)
            val aiMessage = AIChatMessage(
                role = "assistant",
                content = response,
                timestamp = System.currentTimeMillis()
            )
            val updatedMessages = _uiState.value.messages.toMutableList()
            updatedMessages.add(aiMessage)
            _uiState.value = _uiState.value.copy(
                messages = updatedMessages,
                isLoading = false
            )
        }
    }

    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(
            messages = listOf(
                AIChatMessage(
                    role = "assistant",
                    content = "对话已清空。有什么新问题想问我吗？",
                    timestamp = System.currentTimeMillis()
                )
            ),
            inputText = "",
            isLoading = false
        )
    }
}

data class ChatUiState(
    val messages: List<AIChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false
)
