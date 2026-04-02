package com.aistd.ikraiaicodeuser.model.dto.chathistory;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史导出请求
 */
@Data
public class ChatHistoryExportRequest implements Serializable {

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 导出开始时间（可选）
     */
    private LocalDateTime startTime;

    /**
     * 导出结束时间（可选）
     */
    private LocalDateTime endTime;

    private static final long serialVersionUID = 1L;
}