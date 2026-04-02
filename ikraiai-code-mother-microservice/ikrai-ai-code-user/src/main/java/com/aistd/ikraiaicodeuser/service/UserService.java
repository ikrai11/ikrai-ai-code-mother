package com.aistd.ikraiaicodeuser.service;

import com.aistd.ikraiaicodeuser.model.dto.user.UserQueryRequest;
import com.aistd.ikraiaicodeuser.model.vo.LoginUserVO;
import com.aistd.ikraiaicodeuser.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.aistd.ikraiaicodeuser.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author <a href="https://github.com/ikrai11">程序员ikrai11</a>
 */
public interface UserService extends IService<User> {
    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     *
     * @return 脱敏后的登录用户信息
     */
    LoginUserVO getUserLoginVO(User user);

    /**
     * 用户登录
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 获取用户视图（脱敏）
     *
     * @param user 用户实体
     * @return 脱敏后的用户视图
     */
    UserVO getUserVO(User user);

    /**
     * 获取用户视图（脱敏）列表
     *
     * @param userList 用户实体列表
     * @return 脱敏后的用户视图列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return 查询条件
     */
    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取加密密码
     *
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);
}
