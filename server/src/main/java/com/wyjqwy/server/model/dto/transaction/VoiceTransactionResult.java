package com.wyjqwy.server.model.dto.transaction;

import java.util.List;

/**
 * 语音可一次记多条，返回本批已插入的交易 id 列表（顺序与请求解析顺序一致）。
 */
public record VoiceTransactionResult(
        boolean saved,
        List<Long> transactionIds
) {
}