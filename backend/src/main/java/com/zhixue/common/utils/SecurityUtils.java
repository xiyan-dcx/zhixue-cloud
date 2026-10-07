package com.zhixue.common.utils;

import com.zhixue.common.exception.ServiceException;
import com.zhixue.common.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser loginUser)) {
            throw new ServiceException(401, "未登录");
        }
        return loginUser;
    }
    public static Long getUserId() { return getLoginUser().getUserId(); }
}
