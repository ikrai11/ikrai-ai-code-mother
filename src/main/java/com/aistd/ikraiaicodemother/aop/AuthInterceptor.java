package com.aistd.ikraiaicodemother.aop;

import com.aistd.ikraiaicodemother.annotation.AuthCheck;
import com.aistd.ikraiaicodemother.exception.BusinessException;
import com.aistd.ikraiaicodemother.exception.ErrorCode;
import com.aistd.ikraiaicodemother.model.entity.User;
import com.aistd.ikraiaicodemother.model.enums.UserRoleEnum;
import com.aistd.ikraiaicodemother.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint  切入点
     * @param authCheck  权限校验注解
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)")// 拦截所有标注了AuthCheck注解的方法
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        //获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
        //不需要权限，直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }
        //以下为，需要权限的情况
        //获取当前用户具有的权限
        UserRoleEnum loginUserRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        //没有权限，拒绝
        if (loginUserRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求必须由管理员权限，但是用户并没有管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum) && !UserRoleEnum.ADMIN.equals(loginUserRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //校验通过，放行
        return joinPoint.proceed();
    }
}
