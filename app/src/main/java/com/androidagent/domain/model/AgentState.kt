package com.androidagent.domain.model

enum class AgentState {
    IDLE,
    OBSERVING,
    THINKING,
    PLANNING,
    ACTING,
    VERIFYING,
    WAITING_APPROVAL,
    PAUSED,
    ERROR,
    COMPLETED
}
