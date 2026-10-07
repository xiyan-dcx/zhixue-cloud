package com.zhixue.system.service;

import com.zhixue.common.core.ResultCode;
import com.zhixue.common.exception.ServiceException;
import com.zhixue.common.security.LoginUser;
import com.zhixue.common.utils.JwtUtils;
import com.zhixue.system.dto.LoginBody;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public String login(LoginBody body) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(body.getUsername(), body.getPassword()));
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return jwtUtils.createToken(loginUser.getUsername(), loginUser.getTokenVersion());
        } catch (AuthenticationException e) {
            throw new ServiceException(
                    ResultCode.LOGIN_ERROR.getCode(),
                    ResultCode.LOGIN_ERROR.getMsg());
        }
    }
}
