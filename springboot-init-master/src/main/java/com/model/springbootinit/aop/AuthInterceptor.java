package com.model.springbootinit.aop;

import com.model.springbootinit.common.ErrorCode;
import com.model.springbootinit.exception.BusinessException;
import com.model.springbootinit.model.entity.User;
import com.model.springbootinit.model.enums.UserRoleEnum;
import com.model.springbootinit.service.UserService;
import com.model.springbootinit.annotation.AuthCheck;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP
 *
 */
@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param authCheck
     * @return
     */
    @Around("@annotation(authCheck)")   // 拦截所有带有@AuthCheck注解的方法
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取注解中指定的必须角色（如"admin"）
        String mustRole = authCheck.mustRole();
        // 从请求上下文中获取当前HTTP请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 将注解中的角色字符串转换为枚举
        UserRoleEnum mustRoleEnum = UserRoleEnum.getEnumByValue(mustRole);

        // 如果注解未指定角色，直接放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        // 检查用户角色
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR); // 用户无角色，拒绝
        }

        // 封禁用户直接拒绝
        if (UserRoleEnum.BAN.equals(userRoleEnum)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 检查管理员权限
        if (UserRoleEnum.ADMIN.equals(mustRoleEnum)) {
            if (!UserRoleEnum.ADMIN.equals(userRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR); // 非管理员，拒绝
            }
        }

        // 权限校验通过，执行原方法
        return joinPoint.proceed();
    }
}

