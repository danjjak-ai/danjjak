package com.danjjak.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danjjak.data.remote.DanjjakApi
import com.danjjak.data.remote.dto.FeedbackRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val api: DanjjakApi
) : ViewModel() {
    private val _advice = MutableStateFlow<String?>(null)
    val advice: StateFlow<String?> = _advice.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadAdvice()
    }
    
    fun loadAdvice() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.getAdvice()
                if (response.success && response.advice != null) {
                    _advice.value = response.advice
                } else {
                    _advice.value = "단짝 AI 조언을 가져오는데 실패했습니다."
                }
            } catch (e: Exception) {
                // 백엔드가 꺼져있거나 통신 실패 시 모의 응답 표시 (Fallback)
                _advice.value = "지금은 단짝이 잠깐 쉬고 있어요.\n내부 기록 분석: 3시간 뒤 정기적인 스트레칭을 추천드려요. 잠시 후 다시 시도해주세요."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun sendFeedback(reaction: String) {
        viewModelScope.launch {
            try {
                api.sendFeedback(FeedbackRequest(reaction))
            } catch (e: Exception) {
                // 실패해도 UI의 흐름을 방해하지 않음
            }
        }
    }
}
