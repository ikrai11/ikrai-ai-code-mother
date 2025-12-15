package com.aistd.ikraiaicodemother.service;

import com.aistd.ikraiaicodemother.model.dto.app.AppQueryRequest;
import com.aistd.ikraiaicodemother.model.vo.AppVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.aistd.ikraiaicodemother.model.entity.App;

import java.util.List;

/**
 * 应用 服务层。
 *
 */
public interface AppService extends IService<App> {


    /**
     * 获取应用封装类
     *
     * @param app
     * @return
     */
    AppVO getAppVO(App app);

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
     * @param appQueryRequest
     * @return
     */
    QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest);



}
