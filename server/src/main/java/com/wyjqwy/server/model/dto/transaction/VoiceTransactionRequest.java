package com.wyjqwy.server.model.dto.transaction;

import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record VoiceTransactionRequest(
        @Size(max = 2000) String voiceText,
        String audioBase64,
        LocalDateTime occurredAt
) {
}
