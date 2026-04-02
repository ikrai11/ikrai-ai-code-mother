package com.aistd.ikraiaicodemother.service;

import com.aistd.ikraiaicodemother.model.dto.app.AppAddRequest;
import com.aistd.ikraiaicodemother.model.dto.app.AppQueryRequest;
import com.aistd.ikraiaicodemother.model.entity.User;
import com.aistd.ikraiaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.aistd.ikraiaicodemother.model.entity.App;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.List;

/**
 * 应用 服务层。
 *
 */
public interface AppService extends IService<App> {

    /**
     * 异步生成应用截图
     *
     * @param appId 应用ID
     * @param appUrl 应用URL
     */
    void generateAppScreenshotAsync(Long appId, String appUrl);

    /**
     * 获取应用封装类
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

    /**
     * 聊天生成代码
     *
     * @param appId     应用ID
     * @param message   聊天消息
     * @param loginUser 登录用户
     * @return 代码生成结果流
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    /**
     * 创建应用
     *
     * @param appAddRequest 应用添加请求
     * @param loginUser     登录用户
     * @return 应用ID
     */
    Long createApp(AppAddRequest appAddRequest, User loginUser);

    boolean removeByAppId(Serializable id);

    /**
     * 部署应用
     *
     * @param appId     应用ID
     * @param loginUser 登录用户
     * @return 部署结果
     */
    String deployApp( Long appId, User loginUser);

    /**
     * 获取应用封装类列表
     *
     * @param appList
     * @return
     */
    List<AppVO> getAppVOList(List<App> appList);

    /**
     * 获取查询包装器
     *
     * @param appQueryRequest 查询参数
     * @return 查询包装器
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);
}
