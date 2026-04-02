package com.aistd.ikraiaicodemother.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aistd.ikraiaicodemother.constant.UserConstant;
import com.aistd.ikraiaicodemother.exception.ErrorCode;
import com.aistd.ikraiaicodemother.exception.ThrowUtils;
import com.aistd.ikraiaicodemother.model.dto.chathistory.ChatHistoryExportRequest;
import com.aistd.ikraiaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import com.aistd.ikraiaicodemother.model.entity.App;
import com.aistd.ikraiaicodemother.model.entity.User;
import com.aistd.ikraiaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.aistd.ikraiaicodemother.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.aistd.ikraiaicodemother.model.entity.ChatHistory;
import com.aistd.ikraiaicodemother.mapper.ChatHistoryMapper;
import com.aistd.ikraiaicodemother.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;

/**
 * 对话历史 服务层实现。
 *
 */
@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>  implements ChatHistoryService{

    @Resource
    @Lazy
    private AppService appService;

    /**
     * 添加对话历史
     * @param appId
     * @param message
     * @param messageType
     * @param userId
     * @return
     */
    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        //基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        //验证消息类型是否有效
        ChatHistoryMessageTypeEnum messageTypeEnum = ChatHistoryMessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "消息类型无效"+messageType);
        ChatHistory chatHistory=ChatHistory.builder()
                .appId(appId)
                .userId(userId)
                .message(message)
                .messageType(messageType)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryQueryRequest queryRequest = new ChatHistoryQueryRequest();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    /**
     * 根据应用ID删除对话历史
     * @param appId
     * @return
     */
    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            List<ChatHistory> reversedHistoryList = new ArrayList<>(historyList);
            Collections.reverse(reversedHistoryList);
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : reversedHistoryList) {
                if (ChatHistoryMessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (ChatHistoryMessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
}


    /**
     * 获取查询包装类
     *
     * @param chatHistoryQueryRequest
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryQueryRequest == null) {
            return queryWrapper;
        }
        Long id = chatHistoryQueryRequest.getId();
        String message = chatHistoryQueryRequest.getMessage();
        String messageType = chatHistoryQueryRequest.getMessageType();
        Long appId = chatHistoryQueryRequest.getAppId();
        Long userId = chatHistoryQueryRequest.getUserId();
        LocalDateTime lastCreateTime = chatHistoryQueryRequest.getLastCreateTime();
        String sortField = chatHistoryQueryRequest.getSortField();
        String sortOrder = chatHistoryQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    /**
     * 导出对话历史为Markdown格式
     * @param exportRequest 导出请求
     * @param loginUser 登录用户
     * @return Markdown格式的对话历史内容
     */
    @Override
    public String exportChatHistoryAsMarkdown(ChatHistoryExportRequest exportRequest, User loginUser) {
        ThrowUtils.throwIf(exportRequest == null, ErrorCode.PARAMS_ERROR, "导出请求不能为空");
        Long appId = exportRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");

        // 验证权限：只有应用创建者和管理员可以导出
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权导出该应用的对话历史");

        // 查询对话历史
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatHistory::getAppId, appId)
                .orderBy(ChatHistory::getCreateTime, true); // 按时间正序排列

        // 添加时间范围过滤
        if (exportRequest.getStartTime() != null) {
            queryWrapper.ge(ChatHistory::getCreateTime, exportRequest.getStartTime());
        }
        if (exportRequest.getEndTime() != null) {
            queryWrapper.le(ChatHistory::getCreateTime, exportRequest.getEndTime());
        }

        List<ChatHistory> chatHistoryList = this.list(queryWrapper);
        if (CollUtil.isEmpty(chatHistoryList)) {
            return generateEmptyMarkdown(app);
        }

        // 按时间排序
        chatHistoryList.sort(Comparator.comparing(ChatHistory::getCreateTime));

        return generateMarkdownContent(app, chatHistoryList);
    }

    /**
     * 生成空的Markdown内容
     */
    private String generateEmptyMarkdown(App app) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# ").append(app.getAppName()).append(" - 对话历史\n\n");
        markdown.append("**应用ID：** ").append(app.getId()).append("\n");
        markdown.append("**导出时间：** ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");
        markdown.append("---\n\n");
        markdown.append("暂无对话记录\n");
        return markdown.toString();
    }

    /**
     * 生成Markdown格式的对话历史内容
     */
    private String generateMarkdownContent(App app, List<ChatHistory> chatHistoryList) {
        StringBuilder markdown = new StringBuilder();

        // 标题和基本信息
        markdown.append("# ").append(app.getAppName()).append(" - 对话历史\n\n");
        markdown.append("**应用ID：** ").append(app.getId()).append("\n");
        markdown.append("**应用描述：** ").append(StrUtil.isNotBlank(app.getInitPrompt()) ? app.getInitPrompt() : "无").append("\n");
        markdown.append("**导出时间：** ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        markdown.append("**对话总数：** ").append(chatHistoryList.size()).append(" 条\n");
        markdown.append("**时间范围：** ").append(chatHistoryList.get(0).getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .append(" 至 ").append(chatHistoryList.get(chatHistoryList.size() - 1).getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        markdown.append("---\n\n");

        // 对话记录
        markdown.append("## 对话记录\n\n");

        for (ChatHistory chat : chatHistoryList) {
            String role = ChatHistoryMessageTypeEnum.USER.getValue().equals(chat.getMessageType()) ? "用户" : "AI助手";
            String timeStr = chat.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            markdown.append("### ").append(role).append(" - ").append(timeStr).append("\n\n");
            markdown.append(formatMessage(chat.getMessage())).append("\n\n");
            markdown.append("---\n\n");
        }

        return markdown.toString();
    }

    /**
     * 格式化消息内容，支持代码块等格式
     */
    private String formatMessage(String message) {
        if (StrUtil.isBlank(message)) {
            return "*空消息*";
        }

        // 简单处理代码块（如果消息包含```）
        if (message.contains("```")) {
            return message;
        }

        // 处理换行
        message = message.replace("\n", "\n\n");

        return message;
    }


}
